package com.akilimo.mobile.repos.account

import com.akilimo.mobile.dao.SavedAdviceDao
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.network.AccountApi
import com.akilimo.mobile.network.dto.HistoryDetailResponse
import com.akilimo.mobile.network.dto.HistoryItemDto
import com.akilimo.mobile.network.dto.HistoryResultDto
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

    override fun observe(accountId: Long): Flow<List<SavedAdvice>> =
        rows.map { list -> list.filter { it.accountId == accountId }.sortedByDescending { it.createdAt } }

    override suspend fun find(accountId: Long, requestId: String) =
        rows.value.firstOrNull { it.accountId == accountId && it.requestId == requestId }

    override fun observeOne(accountId: Long, requestId: String): Flow<SavedAdvice?> =
        rows.map { list -> list.firstOrNull { it.accountId == accountId && it.requestId == requestId } }

    override suspend fun upsert(advice: SavedAdvice) {
        rows.value = rows.value.filterNot { it.requestId == advice.requestId } + advice
    }

    override suspend fun upsertAll(advice: List<SavedAdvice>) = advice.forEach { upsert(it) }

    override suspend fun delete(accountId: Long, requestId: String) {
        rows.value = rows.value.filterNot { it.accountId == accountId && it.requestId == requestId }
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

    private fun signIn(consentRequired: Boolean = false, accountId: Long = 5) = store.save(
        AccountSession("tok", Long.MAX_VALUE, accountId, "Amina", "a@b.co", true, consentRequired)
    )

    private fun item(id: String, text: String? = null, at: String = "2026-11-12T10:00:00+00:00") = HistoryItemDto(
        requestId = id, countryCode = "NG", useCase = "FR", createdAt = at,
        result = text?.let { HistoryResultDto("success", RecommendationData("FR", it)) },
    )

    private fun page(vararg items: HistoryItemDto, current: Int = 1, last: Int = 1) =
        Response.success(PagedResponse(items.toList(), null, PaginationMeta(current, last, 50, items.size)))

    @Before
    fun setUp() {
        store = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
        repo = SavedAdviceRepo(dao, AccountCalls(factory, store), store) { 1_000L }
    }

    @Test
    fun `an advice just computed is kept for the signed-in farmer`() = runTest {
        signIn()

        repo.saveComputed("r1", "FR", "NG", "FR", "Apply 50 kg of urea")

        val saved = repo.observeAll().first().single()
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

        assertEquals(emptyList<SavedAdvice>(), repo.observeAll().first())
        assertNull(repo.observe("r1").first())
    }

    @Test
    fun `another account on the same phone sees none of it`() = runTest {
        signIn(accountId = 5)
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        signIn(accountId = 6)

        assertEquals(emptyList<SavedAdvice>(), repo.observeAll().first())
    }

    @Test
    fun `refresh merges the server list and keeps text already on the phone`() = runTest {
        signIn()
        repo.saveComputed("r1", "FR", "NG", "FR", "kept text")
        coEvery { api.history(1, any()) } returns page(item("r1"), item("r2", at = "2026-11-12T11:00:00+00:00"))

        val result = repo.refresh()

        assertEquals(AccountResult.Success(Unit), result)
        val rows = repo.observeAll().first()
        assertEquals(listOf("r2", "r1"), rows.map { it.requestId })       // newest first
        assertEquals("kept text", rows.first { it.requestId == "r1" }.recommendation)
        assertNull(rows.first { it.requestId == "r2" }.recommendation)
    }

    @Test
    fun `refresh follows the pages`() = runTest {
        signIn()
        coEvery { api.history(1, any()) } returns page(item("r1"), current = 1, last = 2)
        coEvery { api.history(2, any()) } returns page(item("r2"), current = 2, last = 2)

        repo.refresh()

        assertEquals(setOf("r1", "r2"), repo.observeAll().first().map { it.requestId }.toSet())
    }

    @Test
    fun `refresh with an ended session signs out and says so`() = runTest {
        signIn()
        coEvery { api.history(any(), any()) } returns Response.error(401, "{}".toResponseBody("application/json".toMediaType()))

        val result = repo.refresh()

        assertEquals(AccountResult.Failure(AccountError.SESSION_EXPIRED), result)
        assertNull(store.session.value)
    }

    @Test
    fun `the text of an item is fetched once, when the phone does not have it`() = runTest {
        signIn()
        coEvery { api.history(1, any()) } returns page(item("r1"))
        repo.refresh()
        coEvery { api.historyItem("r1") } returns Response.success(HistoryDetailResponse(item("r1", "Fetched text")))

        repo.loadText("r1")
        repo.loadText("r1")

        assertEquals("Fetched text", repo.observe("r1").first()?.recommendation)
        coVerify(exactly = 1) { api.historyItem("r1") }
    }

    @Test
    fun `deleting removes the item on the server and the phone`() = runTest {
        signIn()
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        coEvery { api.deleteHistoryItem("r1") } returns Response.success(MessageResponse("deleted"))

        assertEquals(AccountResult.Success(Unit), repo.delete("r1"))

        assertEquals(emptyList<SavedAdvice>(), dao.rows.value)
    }

    @Test
    fun `an item the server no longer has is dropped from the phone`() = runTest {
        signIn()
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        coEvery { api.deleteHistoryItem("r1") } returns Response.error(404, "{}".toResponseBody("application/json".toMediaType()))

        assertEquals(AccountResult.Success(Unit), repo.delete("r1"))
        assertEquals(emptyList<SavedAdvice>(), dao.rows.value)
    }

    @Test
    fun `a failed delete leaves the item where it is`() = runTest {
        signIn()
        repo.saveComputed("r1", "FR", "NG", "FR", "text")
        coEvery { api.deleteHistoryItem("r1") } returns Response.error(500, "{}".toResponseBody("application/json".toMediaType()))

        assertEquals(AccountResult.Failure(AccountError.SERVER), repo.delete("r1"))
        assertEquals(1, dao.rows.value.size)
    }
}
