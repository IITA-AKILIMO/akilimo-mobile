package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import com.akilimo.mobile.workers.DataRefresher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class InvestmentAmountViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val investmentRepo: InvestmentRepo = mockk(relaxed = true)
    private val selectedRepo: SelectedInvestmentRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val testUser = AkilimoUser(id = 7, userName = "user1", enumCountry = EnumCountry.NG)

    private fun amount(id: Int) = InvestmentAmount(
        id = id, countryCode = "NG", currencyCode = "NGN", currencySymbol = "₦",
        investmentAmount = 1000.0 * id, exactAmount = false, active = true,
        areaUnit = "acre", sortOrder = id
    )

    private val savedSelection = MutableStateFlow<SelectedInvestment?>(null)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns testUser
        every { investmentRepo.observeAllByCountry(EnumCountry.NG) } returns flowOf(listOf(amount(1), amount(2)))
        savedSelection.value = null
        every { selectedRepo.observeSelected(7) } returns savedSelection
        coEvery { selectedRepo.getSelectedSync(7) } returns null
    }

    private val dataRefresher: DataRefresher = mockk(relaxed = true)

    private fun buildViewModel() =
        InvestmentAmountViewModel(userRepo, investmentRepo, selectedRepo, appSettings, dataRefresher)

    @Test
    fun `loading finishes and the saved choice is pre-selected`() = runTest {
        savedSelection.value = SelectedInvestment(userId = 7, investmentId = 2, chosenAmount = 2000.0, isExactAmount = false)
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.investments.size)
        assertEquals(2, state.selectedId)
    }

    @Test
    fun `a tap is not overridden by a later DB emission`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.select(amount(1))
        savedSelection.value = SelectedInvestment(userId = 7, investmentId = 2, chosenAmount = 2000.0, isExactAmount = false)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.selectedId)
    }

    @Test
    fun `saveSelected without a selection writes nothing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.saveSelected()
        advanceUntilIdle()

        coVerify(exactly = 0) { selectedRepo.saveOrUpdate(any()) }
    }

    @Test
    fun `saveSelected persists and sets saved only after the write`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.select(amount(1))

        viewModel.saveSelected()
        advanceUntilIdle()

        coVerify { selectedRepo.saveOrUpdate(match { it.investmentId == 1 && it.userId == 7 }) }
        assertTrue(viewModel.uiState.value.saved)
    }

    @Test
    fun `saveSelected failure sets saveFailed`() = runTest {
        coEvery { selectedRepo.saveOrUpdate(any()) } throws IllegalStateException("db error")
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.select(amount(1))

        viewModel.saveSelected()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.saveFailed)
        assertFalse(state.saved)
        assertFalse(state.isSaving)
    }

    @Test
    fun `refresh fetches the investment amounts again and stops the spinner`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.refresh()
        advanceUntilIdle()

        coVerify(exactly = 1) { dataRefresher.investmentAmounts() }
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}
