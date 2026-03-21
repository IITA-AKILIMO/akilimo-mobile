package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TractorAccessViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val costsRepo: FieldOperationCostsRepo = mockk(relaxed = true)
    private lateinit var viewModel: TractorAccessViewModel

    private val testUser = AkilimoUser(
        id = 1, userName = "user1",
        enumCountry = EnumCountry.NG,
        enumAreaUnit = EnumAreaUnit.ACRE,
        farmSize = 2.0
    )

    @Before
    fun setUp() {
        viewModel = TractorAccessViewModel(userRepo, costsRepo)
    }

    @Test
    fun `loadData populates uiState from user and existing costs`() = runTest {
        val existingCost = FieldOperationCost(
            userId = 1,
            tractorAvailable = true,
            tractorPloughCost = 50.0,
            tractorRidgeCost = 30.0,
            tractorHarrowCost = 20.0
        )
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns existingCost

        viewModel.loadData("user1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.userId)
        assertEquals(2.0, state.farmSize, 0.0)
        assertEquals(EnumAreaUnit.ACRE, state.enumAreaUnit)
        assertTrue(state.tractorAvailable)
        assertEquals(50.0, state.tractorPloughCost, 0.0)
        assertEquals(30.0, state.tractorRidgeCost, 0.0)
        assertEquals(20.0, state.tractorHarrowCost, 0.0)
    }

    @Test
    fun `loadData uses defaults when no existing costs`() = runTest {
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns null

        viewModel.loadData("user1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.tractorAvailable)
        assertEquals(0.0, state.tractorPloughCost, 0.0)
    }

    @Test
    fun `saveCosts when tractorAvailable true preserves provided costs`() = runTest {
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns null
        viewModel.loadData("user1")
        advanceUntilIdle()

        viewModel.saveCosts(
            tractorAvailable = true,
            ridingCost = 30.0,
            ploughingCost = 50.0,
            harrowingCost = 20.0
        )
        advanceUntilIdle()

        coVerify {
            costsRepo.saveCost(match { cost ->
                cost.tractorAvailable &&
                        cost.tractorRidgeCost == 30.0 &&
                        cost.tractorPloughCost == 50.0 &&
                        cost.tractorHarrowCost == 20.0
            })
        }
    }

    @Test
    fun `saveCosts when tractorAvailable false zeroes all tractor costs`() = runTest {
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns null
        viewModel.loadData("user1")
        advanceUntilIdle()

        viewModel.saveCosts(
            tractorAvailable = false,
            ridingCost = 100.0,
            ploughingCost = 200.0,
            harrowingCost = 300.0
        )
        advanceUntilIdle()

        coVerify {
            costsRepo.saveCost(match { cost ->
                !cost.tractorAvailable &&
                        cost.tractorRidgeCost == 0.0 &&
                        cost.tractorPloughCost == 0.0 &&
                        cost.tractorHarrowCost == 0.0
            })
        }
    }

    @Test
    fun `saveCosts merges with existing costs preserving unspecified fields`() = runTest {
        val existing = FieldOperationCost(
            userId = 1,
            manualPloughCost = 15.0,
            tractorAvailable = true,
            tractorPloughCost = 50.0,
            tractorRidgeCost = 30.0,
            tractorHarrowCost = 20.0
        )
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns existing
        viewModel.loadData("user1")
        advanceUntilIdle()

        // Only update ploughing, leave others null
        viewModel.saveCosts(
            tractorAvailable = true,
            ridingCost = null,
            ploughingCost = 99.0,
            harrowingCost = null
        )
        advanceUntilIdle()

        coVerify {
            costsRepo.saveCost(match { cost ->
                cost.tractorPloughCost == 99.0 &&
                        cost.tractorRidgeCost == 30.0 &&   // unchanged
                        cost.tractorHarrowCost == 20.0 &&  // unchanged
                        cost.manualPloughCost == 15.0       // manual cost preserved
            })
        }
    }

    @Test
    fun `saveCosts sets saved flag`() = runTest {
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns null
        viewModel.loadData("user1")
        advanceUntilIdle()

        viewModel.saveCosts(true, 10.0, 20.0, 30.0)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.saved)
    }

    @Test
    fun `onSaveHandled resets saved flag`() = runTest {
        coEvery { userRepo.getUser("user1") } returns testUser
        coEvery { costsRepo.getCostForUser(1) } returns null
        viewModel.loadData("user1")
        advanceUntilIdle()
        viewModel.saveCosts(true, 10.0, 20.0, 30.0)
        advanceUntilIdle()

        viewModel.onSaveHandled()

        assertFalse(viewModel.uiState.value.saved)
    }

    @Test
    fun `saveCosts does nothing when userId is 0`() = runTest {
        // Do not call loadData — userId stays 0
        viewModel.saveCosts(true, 10.0, 20.0, 30.0)
        advanceUntilIdle()

        coVerify(exactly = 0) { costsRepo.saveCost(any()) }
    }

    @Test
    fun `loadData does nothing when user not found`() = runTest {
        coEvery { userRepo.getUser("unknown") } returns null

        viewModel.loadData("unknown")
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.userId)
    }
}
