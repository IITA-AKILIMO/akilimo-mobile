package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.relations.SelectedCassavaMarketWithDetails
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CassavaYieldViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val yieldRepo: CassavaYieldRepo = mockk(relaxed = true)
    private val selectedRepo: SelectedCassavaMarketRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val testUser = AkilimoUser(id = 1, userName = "user1")
    private val yieldA = CassavaYield(id = 10, imageRes = 0, yieldAmount = 3.75)
    private val yieldB = CassavaYield(id = 11, imageRes = 0, yieldAmount = 11.25)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns testUser
        every { yieldRepo.observeAll() } returns flowOf(listOf(yieldA, yieldB))
        every { selectedRepo.observeSelected(1) } returns flowOf(null)
        coEvery { selectedRepo.getSelectedByUser(1) } returns null
    }

    private fun buildViewModel() = CassavaYieldViewModel(userRepo, yieldRepo, selectedRepo, appSettings)

    @Test
    fun `yields load and loading finishes`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.yields.size)
    }

    @Test
    fun `selectYield only highlights and does not write`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.selectYield(yieldB)
        advanceUntilIdle()

        assertEquals(11, viewModel.uiState.value.selectedYieldId)
        coVerify(exactly = 0) { selectedRepo.select(any()) }
    }

    @Test
    fun `saveSelection keeps the existing market choice and sets saved`() = runTest {
        val existing = SelectedCassavaMarket(id = 4, userId = 1, starchFactoryId = 2)
        coEvery { selectedRepo.getSelectedByUser(1) } returns
            mockk<SelectedCassavaMarketWithDetails>(relaxed = true) { every { selectedCassavaMarket } returns existing }
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.selectYield(yieldA)

        viewModel.saveSelection()
        advanceUntilIdle()

        coVerify { selectedRepo.select(match { it.yieldId == 10 && it.starchFactoryId == 2 }) }
        assertTrue(viewModel.uiState.value.saved)
    }

    @Test
    fun `saveSelection failure sets saveFailed`() = runTest {
        coEvery { selectedRepo.select(any()) } throws IllegalStateException("db error")
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.selectYield(yieldA)

        viewModel.saveSelection()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.saved)
    }
}
