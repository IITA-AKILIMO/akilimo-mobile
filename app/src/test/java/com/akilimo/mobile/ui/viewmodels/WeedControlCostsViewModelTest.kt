package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeedControlCostsViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val costsRepo: FieldOperationCostsRepo = mockk(relaxed = true)
    private val practiceRepo: CurrentPracticeRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    private val user = AkilimoUser(id = 7, userName = "user1", farmSize = 2.5, enumAreaUnit = EnumAreaUnit.HA)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns user
    }

    private fun buildViewModel() =
        WeedControlCostsViewModel(userRepo, costsRepo, practiceRepo, appSettings)

    @Test
    fun `init loads costs, method and farm details`() = runTest {
        coEvery { costsRepo.getCostForUser(7) } returns FieldOperationCost(
            userId = 7, firstWeedingOperationCost = 100.0, secondWeedingOperationCost = 50.0
        )
        coEvery { practiceRepo.getPracticeForUser(7) } returns
                CurrentPractice(userId = 7, weedControlMethod = EnumWeedControlMethod.HERBICIDE)

        val vm = buildViewModel()
        advanceUntilIdle()

        with(vm.uiState.value) {
            assertEquals(7, userId)
            assertEquals(2.5, farmSize, 0.0)
            assertEquals(EnumAreaUnit.HA, enumAreaUnit)
            assertEquals(100.0, firstWeedingCost, 0.0)
            assertEquals(50.0, secondWeedingCost, 0.0)
            assertEquals(EnumWeedControlMethod.HERBICIDE, weedControlMethod)
        }
    }

    @Test
    fun `saveCosts writes costs and method then flags saved`() = runTest {
        coEvery { costsRepo.getCostForUser(7) } returns null
        coEvery { practiceRepo.getPracticeForUser(7) } returns null
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.saveCosts(firstCost = 120.0, secondCost = 0.0, method = EnumWeedControlMethod.MANUAL)
        advanceUntilIdle()

        coVerify {
            costsRepo.saveCost(match {
                it.userId == 7 && it.firstWeedingOperationCost == 120.0 && it.secondWeedingOperationCost == 0.0
            })
        }
        coVerify { practiceRepo.savePractice(match { it.weedControlMethod == EnumWeedControlMethod.MANUAL }) }
        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun `saveCosts with 0 clears a previously saved cost`() = runTest {
        coEvery { costsRepo.getCostForUser(7) } returns FieldOperationCost(
            userId = 7, firstWeedingOperationCost = 100.0, secondWeedingOperationCost = 50.0
        )
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.saveCosts(firstCost = 100.0, secondCost = 0.0, method = null)
        advanceUntilIdle()

        coVerify { costsRepo.saveCost(match { it.secondWeedingOperationCost == 0.0 }) }
    }

    @Test
    fun `onSaveHandled resets saved`() = runTest {
        val vm = buildViewModel()
        advanceUntilIdle()
        vm.saveCosts(1.0, 1.0, null)
        advanceUntilIdle()

        vm.onSaveHandled()

        assertFalse(vm.uiState.value.saved)
    }
}
