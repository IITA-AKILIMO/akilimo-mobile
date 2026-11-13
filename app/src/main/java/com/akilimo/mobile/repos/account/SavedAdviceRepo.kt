package com.akilimo.mobile.repos.account

import com.akilimo.mobile.dao.SavedAdviceDao
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.network.dto.HistoryItemDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** Narrows the saved-advice list; null means "any". */
data class AdviceFilter(val useCase: String? = null, val country: String? = null)

/**
 * The farmer's saved advice: a copy on the phone (so the list opens offline) kept in step with the account on the server.
 * Nothing is read or written unless a farmer who has given consent is signed in.
 *
 * Built for a long history (thousands of items): the list is read from the phone a screenful at a time, and a refresh costs
 * one small request when nothing changed.
 */
@Singleton
class SavedAdviceRepo(
    private val dao: SavedAdviceDao,
    private val calls: AccountCalls,
    private val sessionStore: AccountSessionStore,
    private val clock: () -> Long,
) {
    @Inject
    constructor(dao: SavedAdviceDao, calls: AccountCalls, sessionStore: AccountSessionStore) :
        this(dao, calls, sessionStore, System::currentTimeMillis)

    /** The signed-in farmer's id, or null while nobody is signed in or consent is still missing. */
    private fun activeAccount(): Long? = sessionStore.session.value?.takeIf { !it.consentRequired }?.accountId

    /** The first [limit] items that match [filter], newest first. The screen raises [limit] as the farmer scrolls. */
    fun observePage(filter: AdviceFilter, limit: Int): Flow<List<SavedAdvice>> = whenActive(emptyList()) { account ->
        dao.observePage(account, filter.useCase, filter.country, limit)
    }

    /** How many items match [filter] in all, not only the ones on screen. */
    fun observeCount(filter: AdviceFilter): Flow<Int> = whenActive(0) { account ->
        dao.observeCount(account, filter.useCase, filter.country)
    }

    fun observeUseCases(): Flow<List<String>> = whenActive(emptyList()) { dao.observeUseCases(it) }

    fun observeCountries(): Flow<List<String>> = whenActive(emptyList()) { dao.observeCountries(it) }

    fun observe(requestId: String): Flow<SavedAdvice?> = whenActive(null) { dao.observeOne(it, requestId) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T> whenActive(none: T, source: (Long) -> Flow<T>): Flow<T> =
        sessionStore.session.flatMapLatest { session ->
            if (session == null || session.consentRequired) flowOf(none) else source(session.accountId)
        }

    /** Keeps the advice just computed (the answer carries the id of the row the server saved). */
    suspend fun saveComputed(
        requestId: String,
        useCase: String?,
        countryCode: String?,
        recType: String?,
        recommendation: String?,
    ) {
        val account = activeAccount() ?: return
        dao.upsert(
            SavedAdvice(
                requestId = requestId,
                accountId = account,
                countryCode = countryCode,
                useCase = useCase,
                recType = recType,
                recommendation = recommendation,
                createdAt = clock(),
            )
        )
    }

    /**
     * Brings the phone in step with the account, cheaply:
     *  1. one small request for the ids of everything on the server;
     *  2. items the server no longer has are dropped here (deleted from another phone);
     *  3. only the items the phone is missing are downloaded, newest first, and paging stops as soon as they are all found.
     * With nothing new that is the whole refresh. A new phone with a long history pages through all of it, 100 at a time.
     * Text already on the phone is kept.
     */
    suspend fun refresh(): AccountResult<Unit> {
        val account = activeAccount() ?: return AccountResult.Failure(AccountError.SESSION_EXPIRED)

        val idsResult = calls.execute(authenticated = true) { it.historyIds() }
        if (idsResult is AccountResult.Failure) return idsResult
        val ids = (idsResult as AccountResult.Success).value
        val serverIds = ids.data.toSet()
        val truncated = ids.meta?.truncated == true

        val localIds = dao.allIds(account).toSet()
        if (!truncated) {
            // The list is complete, so anything the phone has beyond it was deleted elsewhere
            (localIds - serverIds).chunked(DELETE_CHUNK).forEach { dao.deleteMany(account, it) }
        }
        // null: the id list was cut (a very long history), so what is missing is not known up front
        var missing: Set<String>? = if (truncated) null else serverIds - localIds
        if (missing?.isEmpty() == true) return AccountResult.Success(Unit)

        var page = 1
        while (page <= MAX_PAGES) {
            val result = calls.execute(authenticated = true) { it.history(page = page, perPage = PAGE_SIZE) }
            if (result is AccountResult.Failure) return result
            val body = (result as AccountResult.Success).value

            val onPhone = dao.findMany(account, body.data.map { it.requestId }).associateBy { it.requestId }
            dao.upsertAll(body.data.map { item -> item.toEntity(account, onPhone[item.requestId]) })

            val last = body.meta?.lastPage ?: page
            if (page >= last) break
            val stillMissing = missing?.minus(body.data.map { it.requestId }.toSet())
            missing = stillMissing
            // Stop when everything missing was found; with a cut id list, when a page was already all on the phone
            val done = stillMissing?.isEmpty() ?: body.data.all { it.requestId in onPhone }
            if (done) break
            page++
        }
        return AccountResult.Success(Unit)
    }

    /** Fetches the recommendation text of one item when the phone does not have it yet (the list carries none). */
    suspend fun loadText(requestId: String): AccountResult<Unit> {
        val account = activeAccount() ?: return AccountResult.Failure(AccountError.SESSION_EXPIRED)
        val existing = dao.find(account, requestId)
        if (existing?.recommendation != null) return AccountResult.Success(Unit)

        return when (val result = calls.execute(authenticated = true) { it.historyItem(requestId) }) {
            is AccountResult.Failure -> result
            is AccountResult.Success -> {
                dao.upsert(result.value.data.toEntity(account, existing))
                AccountResult.Success(Unit)
            }
        }
    }

    /** Removes one item from the account and from the phone. An item the server no longer has is simply dropped here. */
    suspend fun delete(requestId: String): AccountResult<Unit> {
        val account = activeAccount() ?: return AccountResult.Failure(AccountError.SESSION_EXPIRED)
        return when (val result = calls.execute(authenticated = true) { it.deleteHistoryItem(requestId) }) {
            is AccountResult.Success -> deleteLocal(account, requestId)
            is AccountResult.Failure ->
                if (result.error == AccountError.UNAVAILABLE) deleteLocal(account, requestId) else result
        }
    }

    /** Removes all saved advice from the account and from the phone; the account itself stays. */
    suspend fun deleteAll(): AccountResult<Unit> {
        val account = activeAccount() ?: return AccountResult.Failure(AccountError.SESSION_EXPIRED)
        return when (val result = calls.execute(authenticated = true) { it.deleteAllHistory() }) {
            is AccountResult.Failure -> result
            is AccountResult.Success -> {
                dao.deleteForAccount(account)
                AccountResult.Success(Unit)
            }
        }
    }

    /** Wipes every saved item from the phone (sign-out, account deletion). */
    suspend fun clearAll() = dao.deleteAll()

    private suspend fun deleteLocal(account: Long, requestId: String): AccountResult<Unit> {
        dao.delete(account, requestId)
        return AccountResult.Success(Unit)
    }

    private fun HistoryItemDto.toEntity(account: Long, existing: SavedAdvice?) = SavedAdvice(
        requestId = requestId,
        accountId = account,
        countryCode = countryCode ?: existing?.countryCode,
        useCase = useCase ?: existing?.useCase,
        recType = result?.data?.recType ?: existing?.recType,
        recommendation = result?.data?.recommendation ?: existing?.recommendation,
        createdAt = parseMillis(createdAt) ?: existing?.createdAt ?: clock(),
    )

    private fun parseMillis(value: String?): Long? =
        value?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }

    private companion object {
        /** The API's largest page. */
        const val PAGE_SIZE = 100

        /** A safety stop at 20,000 items. */
        const val MAX_PAGES = 200

        const val DELETE_CHUNK = 500
    }
}
