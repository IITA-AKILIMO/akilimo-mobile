package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.rules.TestDispatcherRule
import com.akilimo.mobile.workers.DataRefresher
import com.akilimo.mobile.workers.SyncStatus
import com.akilimo.mobile.workers.SyncStatusSource
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HubViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val syncStatusSource: SyncStatusSource = mockk(relaxed = true)
    private val dataRefresher: DataRefresher = mockk(relaxed = true)

    private val online: SyncStatus = SyncStatus(
        isOnline = true,
        isSyncing = false,
        lastSyncedAt = 1L,
        lastAttemptFailed = false,
    )

    @Before
    fun setUp() {
        clearAllMocks()
    }

    private fun buildViewModel() = HubViewModel(syncStatusSource, dataRefresher)

    @Test
    fun `the hub shows the sync status it is given`() = runTest {
        every { syncStatusSource.status } returns flowOf(online)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(online, viewModel.uiState.value.syncStatus)
        assertEquals(online, viewModel.uiState.value.displayStatus)
    }

    @Test
    fun `retry runs every sync and clears the retrying flag`() = runTest {
        every { syncStatusSource.status } returns flowOf(online)
        coEvery { dataRefresher.all() } returns Unit

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.retrySyncs()
        advanceUntilIdle()

        coVerify(exactly = 1) { dataRefresher.all() }
        assertFalse(viewModel.uiState.value.isRetrying)
    }

    @Test
    fun `a retry looks like syncing before work reports running`() = runTest {
        every { syncStatusSource.status } returns flowOf(online)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.uiState.value.let { state ->
            val retrying = state.copy(isRetrying = true)
            assertTrue(retrying.displayStatus?.isSyncing == true)
        }
    }
}
