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

/**
 * The farmer's saved advice: a copy on the phone (so the list opens offline) kept in step with the account on the server.
 * Nothing is read or written unless a farmer who has given consent is signed in.
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

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeAll(): Flow<List<SavedAdvice>> = sessionStore.session.flatMapLatest { session ->
        if (session == null || session.consentRequired) flowOf(emptyList()) else dao.observe(session.accountId)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(requestId: String): Flow<SavedAdvice?> = sessionStore.session.flatMapLatest { session ->
        if (session == null || session.consentRequired) flowOf(null) else dao.observeOne(session.accountId, requestId)
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

    /** Pulls the account's list from the server and merges it in; text already on the phone is kept. */
    suspend fun refresh(): AccountResult<Unit> {
        val account = activeAccount() ?: return AccountResult.Failure(AccountError.SESSION_EXPIRED)
        var page = 1
        while (page <= MAX_PAGES) {
            val result = calls.execute(authenticated = true) { it.history(page = page, perPage = PAGE_SIZE) }
            if (result is AccountResult.Failure) return result
            val body = (result as AccountResult.Success).value
            val known = body.data.associate { it.requestId to dao.find(account, it.requestId) }
            dao.upsertAll(body.data.map { item -> item.toEntity(account, known[item.requestId]) })
            val last = body.meta?.lastPage ?: page
            if (page >= last) break
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
        const val PAGE_SIZE = 50
        const val MAX_PAGES = 10
    }
}
