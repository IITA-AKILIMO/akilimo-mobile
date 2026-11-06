package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WeedControlCostsViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val costsRepo: FieldOperationCostsRepo = mockk(relaxed = true)
    private val practiceRepo: CurrentPracticeRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val testUser = AkilimoUser(id = 1, userName = "user1", enumCountry = EnumCountry.NG)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns null
        coEvery { practiceRepo.getPracticeForUser(1) } returns null
    }

    private fun buildViewModel() = WeedControlCostsViewModel(userRepo, costsRepo, practiceRepo, appSettings)

    @Test
    fun `canSave requires a weeding method`() {
        assertFalse(WeedControlCostsViewModel.canSave(null, "", ""))
        assertFalse(WeedControlCostsViewModel.canSave(null, "100", "200"))
        assertTrue(WeedControlCostsViewModel.canSave(EnumWeedControlMethod.entries.first(), "", ""))
    }

    @Test
    fun `canSave rejects an unparseable cost`() {
        assertFalse(WeedControlCostsViewModel.canSave(EnumWeedControlMethod.entries.first(), "abc", ""))
    }

    @Test
    fun `loadData exposes the country currency`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals(EnumCountry.NG.currencyCode, viewModel.uiState.value.currencyCode)
    }

    @Test
    fun `saveCosts without a method writes nothing`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.saveCosts(10.0, 20.0, method = null)
        advanceUntilIdle()

        coVerify(exactly = 0) { costsRepo.saveCost(any()) }
        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun `saveCosts saves costs and method then sets saved`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        val method = EnumWeedControlMethod.entries.first()

        viewModel.saveCosts(10.0, 20.0, method)
        advanceUntilIdle()

        coVerify { costsRepo.saveCost(match { it.firstWeedingOperationCost == 10.0 && it.secondWeedingOperationCost == 20.0 }) }
        coVerify { practiceRepo.savePractice(match { it.weedControlMethod == method }) }
        assertTrue(viewModel.uiState.value.saved)
        assertTrue(viewModel.uiState.value.isSaving)

        viewModel.onSaveHandled()
        assertFalse(viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `saveCosts failure reports saveFailed and allows retry`() = runTest {
        coEvery { costsRepo.saveCost(any()) } throws IllegalStateException("disk full")
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.saveCosts(10.0, 20.0, EnumWeedControlMethod.entries.first())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.saveFailed)
        assertFalse(state.isSaving)
        assertFalse(state.saved)

        viewModel.onSaveErrorShown()
        assertFalse(viewModel.uiState.value.saveFailed)
    }
}
