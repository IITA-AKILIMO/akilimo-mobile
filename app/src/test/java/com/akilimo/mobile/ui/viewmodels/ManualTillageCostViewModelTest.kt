package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManualTillageCostViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val costsRepo: FieldOperationCostsRepo = mockk(relaxed = true)
    private val practiceRepo: CurrentPracticeRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(id = 3, userName = "user1")
    }

    private fun buildViewModel() =
        ManualTillageCostViewModel(userRepo, costsRepo, practiceRepo, appSettings)

    @Test
    fun `init loads costs and operations`() = runTest {
        coEvery { costsRepo.getCostForUser(3) } returns
                FieldOperationCost(userId = 3, manualPloughCost = 80.0, manualRidgeCost = 60.0)
        coEvery { practiceRepo.getPracticeForUser(3) } returns
                CurrentPractice(userId = 3, performPloughing = true, performRidging = false)

        val vm = buildViewModel()
        advanceUntilIdle()

        with(vm.uiState.value) {
            assertEquals(3, userId)
            assertEquals(80.0, manualPloughCost!!, 0.0)
            assertEquals(60.0, manualRidgeCost!!, 0.0)
            assertTrue(performPloughing)
            assertFalse(performRidging)
        }
    }

    @Test
    fun `init defaults to both operations when no practice saved`() = runTest {
        coEvery { costsRepo.getCostForUser(3) } returns null
        coEvery { practiceRepo.getPracticeForUser(3) } returns null

        val vm = buildViewModel()
        advanceUntilIdle()

        with(vm.uiState.value) {
            assertNull(manualPloughCost)
            assertTrue(performPloughing)
            assertTrue(performRidging)
        }
    }

    @Test
    fun `saveCosts persists costs and practice then flags saved`() = runTest {
        coEvery { costsRepo.getCostForUser(3) } returns null
        coEvery { practiceRepo.getPracticeForUser(3) } returns null
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.saveCosts(ridingCost = 0.0, ploughingCost = 90.0, performPloughing = true, performRidging = false)
        advanceUntilIdle()

        coVerify { costsRepo.saveCost(match { it.manualPloughCost == 90.0 && it.manualRidgeCost == 0.0 }) }
        coVerify { practiceRepo.savePractice(match { it.performPloughing && !it.performRidging }) }
        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun `saveCosts does nothing before the user is loaded`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.saveCosts(1.0, 1.0, performPloughing = true, performRidging = true)
        advanceUntilIdle()

        coVerify(exactly = 0) { costsRepo.saveCost(any()) }
        assertFalse(vm.uiState.value.saved)
    }
}
