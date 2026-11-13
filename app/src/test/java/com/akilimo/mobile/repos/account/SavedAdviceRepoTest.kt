package com.akilimo.mobile.repos.account

import com.akilimo.mobile.dao.SavedAdviceDao
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.network.AccountApi
import com.akilimo.mobile.network.dto.HistoryDetailResponse
import com.akilimo.mobile.network.dto.HistoryIdsResponse
import com.akilimo.mobile.network.dto.HistoryItemDto
import com.akilimo.mobile.network.dto.HistoryResultDto
import com.akilimo.mobile.network.dto.IdsMeta
import com.akilimo.mobile.network.dto.MessageResponse
import com.akilimo.mobile.network.dto.PagedResponse
import com.akilimo.mobile.network.dto.PaginationMeta
import com.akilimo.mobile.network.dto.RecommendationData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/** A phone database in memory. */
class FakeSavedAdviceDao : SavedAdviceDao {
    val rows = MutableStateFlow<List<SavedAdvice>>(emptyList())

    private fun matching(accountId: Long, useCase: String?, country: String?) = rows.map { list ->
        list.filter {
            it.accountId == accountId &&
                (useCase == null || it.useCase == useCase) &&
                (country == null || it.countryCode == country)
        }.sortedWith(compareByDescending<SavedAdvice> { it.createdAt }.thenByDescending { it.requestId })
    }

    override fun observePage(accountId: Long, useCase: String?, country: String?, limit: Int): Flow<List<SavedAdvice>> =
        matching(accountId, useCase, country).map { it.take(limit) }

    override fun observeCount(accountId: Long, useCase: String?, country: String?): Flow<Int> =
        matching(accountId, useCase, country).map { it.size }

    override fun observeUseCases(accountId: Long): Flow<List<String>> =
        rows.map { list -> list.filter { it.accountId == accountId }.mapNotNull { it.useCase }.distinct().sorted() }

    override fun observeCountries(accountId: Long): Flow<List<String>> =
        rows.map { list -> list.filter { it.accountId == accountId }.mapNotNull { it.countryCode }.distinct().sorted() }

    override suspend fun find(accountId: Long, requestId: String) =
        rows.value.firstOrNull { it.accountId == accountId && it.requestId == requestId }

    override fun observeOne(accountId: Long, requestId: String): Flow<SavedAdvice?> =
        rows.map { list -> list.firstOrNull { it.accountId == accountId && it.requestId == requestId } }

    override suspend fun findMany(accountId: Long, requestIds: List<String>) =
        rows.value.filter { it.accountId == accountId && it.requestId in requestIds }

    override suspend fun allIds(accountId: Long) = rows.value.filter { it.accountId == accountId }.map { it.requestId }

    override suspend fun upsert(advice: SavedAdvice) {
        rows.value = rows.value.filterNot { it.requestId == advice.requestId } + advice
    }

    override suspend fun upsertAll(advice: List<SavedAdvice>) = advice.forEach { upsert(it) }

    override suspend fun delete(accountId: Long, requestId: String) {
        rows.value = rows.value.filterNot { it.accountId == accountId && it.requestId == requestId }
    }

    override suspend fun deleteMany(accountId: Long, requestIds: List<String>) {
        rows.value = rows.value.filterNot { it.accountId == accountId && it.requestId in requestIds }
    }

    override suspend fun deleteForAccount(accountId: Long) {
        rows.value = rows.value.filterNot { it.accountId == accountId }
    }

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }
}

class SavedAdviceRepoTest {

    private val api = mockk<AccountApi>()
    private val factory = mockk<AccountApiFactory> { every { create() } returns api }
    private val dao = FakeSavedAdviceDao()
    private lateinit var store: AccountSessionStore
    private lateinit var repo: SavedAdviceRepo

    private val all = AdviceFilter()

    private fun signIn(consentRequired: Boolean = false, accountId: Long = 5) = store.save(
        AccountSession("tok", Long.MAX_VALUE, accountId, "Amina", "a@b.co", true, consentRequired)
    )

    private fun <T> error(code: Int): Response<T> = Response.error(code, "{}".toResponseBody("application/json".toMediaType()))

    private fun item(id: String, text: String? = null, at: String = "2026-11-12T10:00:00+00:00") = HistoryItemDto(
        requestId = id, countryCode = "NG", useCase = "FR", createdAt = at,
        result = text?.let { HistoryResultDto("success", RecommendationData("FR", it)) },
    )

    private fun page(items: List<HistoryItemDto>, current: Int = 1, last: Int = 1, total: Int = items.size) =
        Response.success(PagedResponse(items, null, PaginationMeta(current, last, 100, total)))

    private fun idsOf(vararg ids: String, truncated: Boolean = false) =
        Response.success(HistoryIdsResponse(ids.toList(), IdsMeta(ids.size, truncated)))

    private fun advice(id: String, at: Long, account: Long = 5, useCase: String = "FR", country: String = "NG", text: String? = "t") =
        SavedAdvice(id, account, country, useCase, useCase, text, at)

    private suspend fun shown(filter: AdviceFilter = all, limit: Int = 1000) = repo.observePage(filter, limit).first()

    @Before
    fun setUp() {
        store = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
        repo = SavedAdviceRepo(dao, AccountCalls(factory, store), store) { 1_000L }
    }

    // ── What the phone keeps ─────────────────────────────────────────────────

    @Test
    fun `an advice just computed is kept for the signed-in farmer`() = runTest {
        signIn()

        repo.saveComputed("r1", "FR", "NG", "FR", "Apply 50 kg of urea")

        val saved = shown().single()
        assertEquals("r1", saved.requestId)
        assertEquals(5L, saved.accountId)
        assertEquals("Apply 50 kg of urea", saved.recommendation)
    }

    @Test
    fun `nothing is kept when nobody is signed in or consent is missing`() = runTest {
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        signIn(consentRequired = true)
        repo.saveComputed("r2", "FR", "NG", "FR", "text")

        assertEquals(emptyList<SavedAdvice>(), dao.rows.value)
    }

    @Test
    fun `the list is empty while consent is missing, even with rows on the phone`() = runTest {
        signIn()
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        signIn(consentRequired = true)

        assertEquals(emptyList<SavedAdvice>(), shown())
        assertEquals(0, repo.observeCount(all).first())
        assertNull(repo.observe("r1").first())
    }

    @Test
    fun `another account on the same phone sees none of it`() = runTest {
        signIn(accountId = 5)
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        signIn(accountId = 6)

        assertEquals(emptyList<SavedAdvice>(), shown())
    }

    // ── A long list, a screenful at a time ───────────────────────────────────

    @Test
    fun `the list holds only as many items as asked for, newest first, and the count is the whole`() = runTest {
        signIn()
        dao.upsertAll((1..120).map { advice("r$it", at = it.toLong()) })

        val first = shown(limit = 50)

        assertEquals(50, first.size)
        assertEquals("r120", first.first().requestId)
        assertEquals("r71", first.last().requestId)
        assertEquals(120, repo.observeCount(all).first())
        assertEquals(120, shown(limit = 200).size)
    }

    @Test
    fun `the list can be narrowed by use case and country, and the filter values are listed`() = runTest {
        signIn()
        dao.upsertAll(
            listOf(
                advice("a", 1, useCase = "FR", country = "NG"),
                advice("b", 2, useCase = "IC", country = "NG"),
                advice("c", 3, useCase = "FR", country = "TZ"),
            )
        )

        assertEquals(listOf("c", "a"), shown(AdviceFilter(useCase = "FR")).map { it.requestId })
        assertEquals(listOf("c"), shown(AdviceFilter(useCase = "FR", country = "TZ")).map { it.requestId })
        assertEquals(2, repo.observeCount(AdviceFilter(country = "NG")).first())
        assertEquals(listOf("FR", "IC"), repo.observeUseCases().first())
        assertEquals(listOf("NG", "TZ"), repo.observeCountries().first())
    }

    // ── Refresh: cheap when nothing changed ──────────────────────────────────

    @Test
    fun `with nothing new a refresh is one small request and downloads no list`() = runTest {
        signIn()
        dao.upsertAll(listOf(advice("r1", 1), advice("r2", 2)))
        coEvery { api.historyIds() } returns idsOf("r2", "r1")

        assertEquals(AccountResult.Success(Unit), repo.refresh())

        coVerify(exactly = 1) { api.historyIds() }
        coVerify(exactly = 0) { api.history(any(), any()) }
    }

    @Test
    fun `items deleted from another phone are dropped here`() = runTest {
        signIn()
        dao.upsertAll(listOf(advice("r1", 1), advice("r2", 2), advice("r3", 3)))
        coEvery { api.historyIds() } returns idsOf("r3", "r1")

        repo.refresh()

        assertEquals(listOf("r3", "r1"), shown().map { it.requestId })
        coVerify(exactly = 0) { api.history(any(), any()) }
    }

    @Test
    fun `only the missing items are downloaded and paging stops once they are found`() = runTest {
        signIn()
        val newest = (1..250).map { "s$it" }          // newest first, as the server lists them
        dao.upsertAll(newest.drop(50).mapIndexed { i, id -> advice(id, at = (200 - i).toLong()) }) // the phone has the 200 older ones
        coEvery { api.historyIds() } returns Response.success(HistoryIdsResponse(newest, IdsMeta(250, false)))
        coEvery { api.history(1, any()) } returns page(newest.take(100).map { item(it) }, current = 1, last = 3, total = 250)

        repo.refresh()

        assertEquals(250, shown().size)
        coVerify(exactly = 1) { api.history(1, any()) }
        coVerify(exactly = 0) { api.history(2, any()) }
    }

    @Test
    fun `a new phone with a long history pages through all of it, with no cap`() = runTest {
        signIn()
        val ids = (1..1200).map { "x$it" }
        coEvery { api.historyIds() } returns Response.success(HistoryIdsResponse(ids, IdsMeta(1200, false)))
        (1..12).forEach { p ->
            coEvery { api.history(p, any()) } returns page(
                ids.drop((p - 1) * 100).take(100).map { item(it) }, current = p, last = 12, total = 1200,
            )
        }

        assertEquals(AccountResult.Success(Unit), repo.refresh())

        assertEquals(1200, repo.observeCount(all).first())
        coVerify(exactly = 12) { api.history(any(), any()) }
    }

    @Test
    fun `text already on the phone is kept when the list is merged`() = runTest {
        signIn()
        repo.saveComputed("r1", "FR", "NG", "FR", "kept text")
        coEvery { api.historyIds() } returns idsOf("r2", "r1")
        coEvery { api.history(1, any()) } returns page(listOf(item("r2", at = "2026-11-12T11:00:00+00:00"), item("r1")))

        repo.refresh()

        val rows = shown()
        assertEquals("kept text", rows.first { it.requestId == "r1" }.recommendation)
        assertNull(rows.first { it.requestId == "r2" }.recommendation)
    }

    @Test
    fun `when the id list was cut a page already on the phone ends the paging and nothing is dropped`() = runTest {
        signIn()
        dao.upsertAll((1..100).map { advice("k$it", at = it.toLong()) })
        coEvery { api.historyIds() } returns Response.success(HistoryIdsResponse(listOf("n1"), IdsMeta(1, true)))
        coEvery { api.history(1, any()) } returns page(
            (1..100).map { item("k$it") }, current = 1, last = 60, total = 6000,
        )

        repo.refresh()

        assertEquals(100, repo.observeCount(all).first())          // not dropped: the id list was incomplete
        coVerify(exactly = 1) { api.history(any(), any()) }
    }

    @Test
    fun `refresh with an ended session signs out and says so`() = runTest {
        signIn()
        coEvery { api.historyIds() } returns error(401)

        val result = repo.refresh()

        assertEquals(AccountResult.Failure(AccountError.SESSION_EXPIRED), result)
        assertNull(store.session.value)
    }

    @Test
    fun `a failed page leaves what was already merged`() = runTest {
        signIn()
        coEvery { api.historyIds() } returns idsOf("a", "b")
        coEvery { api.history(1, any()) } returns error(500)

        assertEquals(AccountResult.Failure(AccountError.SERVER), repo.refresh())
        assertEquals(0, repo.observeCount(all).first())
    }

    // ── One item ─────────────────────────────────────────────────────────────

    @Test
    fun `the text of an item is fetched once, when the phone does not have it`() = runTest {
        signIn()
        dao.upsert(advice("r1", 1, text = null))
        coEvery { api.historyItem("r1") } returns Response.success(HistoryDetailResponse(item("r1", "Fetched text")))

        repo.loadText("r1")
        repo.loadText("r1")

        assertEquals("Fetched text", repo.observe("r1").first()?.recommendation)
        coVerify(exactly = 1) { api.historyItem("r1") }
    }

    @Test
    fun `deleting removes the item on the server and the phone`() = runTest {
        signIn()
        dao.upsert(advice("r1", 1))
        coEvery { api.deleteHistoryItem("r1") } returns Response.success(MessageResponse("deleted"))

        assertEquals(AccountResult.Success(Unit), repo.delete("r1"))

        assertEquals(emptyList<SavedAdvice>(), dao.rows.value)
    }

    @Test
    fun `an item the server no longer has is dropped from the phone`() = runTest {
        signIn()
        dao.upsert(advice("r1", 1))
        coEvery { api.deleteHistoryItem("r1") } returns error(404)

        assertEquals(AccountResult.Success(Unit), repo.delete("r1"))
        assertEquals(emptyList<SavedAdvice>(), dao.rows.value)
    }

    @Test
    fun `a failed delete leaves the item where it is`() = runTest {
        signIn()
        dao.upsert(advice("r1", 1))
        coEvery { api.deleteHistoryItem("r1") } returns error(500)

        assertEquals(AccountResult.Failure(AccountError.SERVER), repo.delete("r1"))
        assertEquals(1, dao.rows.value.size)
    }

    // ── Delete all ───────────────────────────────────────────────────────────

    @Test
    fun `delete all clears the account on the server and the phone, and only this account`() = runTest {
        signIn(accountId = 5)
        dao.upsertAll(listOf(advice("r1", 1), advice("r2", 2), advice("o1", 3, account = 6)))
        coEvery { api.deleteAllHistory() } returns Response.success(MessageResponse("done", deleted = 2))

        assertEquals(AccountResult.Success(Unit), repo.deleteAll())

        assertEquals(listOf("o1"), dao.rows.value.map { it.requestId })
    }

    @Test
    fun `a failed delete all leaves everything on the phone`() = runTest {
        signIn()
        dao.upsertAll(listOf(advice("r1", 1), advice("r2", 2)))
        coEvery { api.deleteAllHistory() } returns error(500)

        assertEquals(AccountResult.Failure(AccountError.SERVER), repo.deleteAll())
        assertEquals(2, dao.rows.value.size)
    }
}
