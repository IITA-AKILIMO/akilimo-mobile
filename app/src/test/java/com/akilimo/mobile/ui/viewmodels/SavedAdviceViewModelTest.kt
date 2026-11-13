package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.R
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.FakeSessionStorage
import com.akilimo.mobile.data.account.FakeTokenCipher
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.network.AccountApi
import com.akilimo.mobile.network.dto.HistoryIdsResponse
import com.akilimo.mobile.network.dto.IdsMeta
import com.akilimo.mobile.network.dto.MessageResponse
import com.akilimo.mobile.repos.account.AccountApiFactory
import com.akilimo.mobile.repos.account.AccountCalls
import com.akilimo.mobile.repos.account.AdviceFilter
import com.akilimo.mobile.repos.account.FakeSavedAdviceDao
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SavedAdviceViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val api = mockk<AccountApi>()
    private val factory = mockk<AccountApiFactory> { every { create() } returns api }
    private val dao = FakeSavedAdviceDao()
    private lateinit var repo: SavedAdviceRepo

    private fun advice(i: Int, useCase: String = "FR", country: String = "NG") =
        SavedAdvice("r$i", 5, country, useCase, useCase, "t", i.toLong())

    /** The server agrees with the phone, so a refresh changes nothing. */
    private fun serverHasExactly(rows: List<SavedAdvice>) {
        coEvery { api.historyIds() } returns
            Response.success(HistoryIdsResponse(rows.map { it.requestId }, IdsMeta(rows.size, false)))
    }

    private fun TestScope.watch(vm: SavedAdviceViewModel) {
        backgroundScope.launch { vm.items.collect { } }
        backgroundScope.launch { vm.count.collect { } }
        backgroundScope.launch { vm.useCases.collect { } }
        backgroundScope.launch { vm.countries.collect { } }
    }

    @Before
    fun setUp() {
        val store = AccountSessionStore(FakeSessionStorage(), FakeTokenCipher())
        store.save(AccountSession("tok", Long.MAX_VALUE, 5, "Amina", "a@b.co", true, false))
        repo = SavedAdviceRepo(dao, AccountCalls(factory, store), store) { 1_000L }
    }

    @Test
    fun `a long history shows a screenful and grows as the farmer reaches the end`() = runTest {
        val rows = (1..120).map { advice(it) }
        dao.upsertAll(rows)
        serverHasExactly(rows)
        val vm = SavedAdviceViewModel(repo)
        watch(vm)
        advanceUntilIdle()

        assertEquals(50, vm.items.value?.size)
        assertEquals(120, vm.count.value)
        assertEquals("r120", vm.items.value?.first()?.requestId)

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(100, vm.items.value?.size)

        vm.loadMore()
        advanceUntilIdle()
        assertEquals(120, vm.items.value?.size)

        vm.loadMore() // everything is shown: nothing more to ask for
        advanceUntilIdle()
        assertEquals(SavedAdviceViewModel.PAGE_SIZE * 3, vm.uiState.value.limit)
    }

    @Test
    fun `a short list is not grown`() = runTest {
        val rows = (1..10).map { advice(it) }
        dao.upsertAll(rows)
        serverHasExactly(rows)
        val vm = SavedAdviceViewModel(repo)
        watch(vm)
        advanceUntilIdle()

        vm.loadMore()
        advanceUntilIdle()

        assertEquals(SavedAdviceViewModel.PAGE_SIZE, vm.uiState.value.limit)
        assertEquals(10, vm.items.value?.size)
    }

    @Test
    fun `choosing a use case or a country narrows the list and starts again from one screenful`() = runTest {
        val rows = (1..60).map { advice(it, useCase = "FR") } + (61..70).map { advice(it, useCase = "IC", country = "TZ") }
        dao.upsertAll(rows)
        serverHasExactly(rows)
        val vm = SavedAdviceViewModel(repo)
        watch(vm)
        advanceUntilIdle()
        vm.loadMore()
        advanceUntilIdle()
        assertEquals(70, vm.items.value?.size)

        vm.setUseCase("IC")
        advanceUntilIdle()
        assertEquals(10, vm.items.value?.size)
        assertEquals(10, vm.count.value)
        assertEquals(SavedAdviceViewModel.PAGE_SIZE, vm.uiState.value.limit)
        assertEquals(listOf("FR", "IC"), vm.useCases.value)
        assertEquals(listOf("NG", "TZ"), vm.countries.value)

        vm.setUseCase(null)
        vm.setCountry("TZ")
        advanceUntilIdle()
        assertEquals(10, vm.items.value?.size)
        assertEquals(AdviceFilter(country = "TZ"), vm.uiState.value.filter)
    }

    @Test
    fun `the list shows the phone copy and a quiet note when the refresh fails`() = runTest {
        dao.upsert(advice(1))
        coEvery { api.historyIds() } throws IOException("offline")
        val vm = SavedAdviceViewModel(repo)
        watch(vm)
        advanceUntilIdle()

        assertEquals(R.string.saved_advice_refresh_failed, vm.uiState.value.messageRes)
        assertEquals(false, vm.uiState.value.refreshing)
        assertEquals("r1", vm.items.value?.single()?.requestId)
    }

    @Test
    fun `a good refresh leaves no message`() = runTest {
        val rows = listOf(advice(1))
        dao.upsertAll(rows)
        serverHasExactly(rows)
        val vm = SavedAdviceViewModel(repo)
        advanceUntilIdle()

        assertNull(vm.uiState.value.messageRes)
    }

    @Test
    fun `delete all empties the list and clears the filter`() = runTest {
        val rows = (1..5).map { advice(it) }
        dao.upsertAll(rows)
        serverHasExactly(rows)
        coEvery { api.deleteAllHistory() } returns Response.success(MessageResponse("done", deleted = 5))
        val vm = SavedAdviceViewModel(repo)
        watch(vm)
        advanceUntilIdle()
        vm.setUseCase("FR")

        vm.deleteAll()
        advanceUntilIdle()

        assertEquals(0, vm.count.value)
        assertEquals(emptyList<SavedAdvice>(), vm.items.value)
        assertEquals(AdviceFilter(), vm.uiState.value.filter)
        assertEquals(false, vm.uiState.value.deleting)
    }

    @Test
    fun `a failed delete all keeps the list and says why`() = runTest {
        val rows = (1..5).map { advice(it) }
        dao.upsertAll(rows)
        serverHasExactly(rows)
        coEvery { api.deleteAllHistory() } returns
            Response.error(500, "{}".toResponseBody("application/json".toMediaType()))
        val vm = SavedAdviceViewModel(repo)
        watch(vm)
        advanceUntilIdle()

        vm.deleteAll()
        advanceUntilIdle()

        assertEquals(5, vm.count.value)
        assertEquals(R.string.account_err_server, vm.uiState.value.messageRes)
    }
}
