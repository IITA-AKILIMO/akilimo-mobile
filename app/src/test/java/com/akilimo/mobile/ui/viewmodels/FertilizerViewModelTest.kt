package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.SelectedFertilizer
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumFertilizerFlow
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FertilizerViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val fertilizerRepo: FertilizerRepo = mockk(relaxed = true)
    private val selectedRepo: SelectedFertilizerRepo = mockk(relaxed = true)
    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val priceRepo: FertilizerPriceRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val testUser = AkilimoUser(id = 42, userName = "user1", enumCountry = EnumCountry.NG)

    private fun buildViewModel(flow: EnumFertilizerFlow = EnumFertilizerFlow.DEFAULT): FertilizerViewModel {
        every { appSettings.akilimoUser } returns "user1"
        coEvery { userRepo.getUser("user1") } returns testUser
        every { fertilizerRepo.observeByCountry(EnumCountry.NG) } returns flowOf(emptyList())
        every { fertilizerRepo.observeByCimAvailable(EnumCountry.NG) } returns flowOf(emptyList())
        every { fertilizerRepo.observeByCisAvailable(EnumCountry.NG) } returns flowOf(emptyList())
        coEvery { selectedRepo.getSelectedSync(42) } returns emptyList()
        every { selectedRepo.observeSelected(42) } returns flowOf(emptyList())
        return FertilizerViewModel(
            fertilizerRepo = fertilizerRepo,
            selectedRepo = selectedRepo,
            userRepo = userRepo,
            priceRepo = priceRepo,
            appSettings = appSettings,
            fertilizerFlow = flow
        )
    }

    @Before
    fun setUp() {
        // intentionally empty — each test builds its own ViewModel to control mock state
    }

    @Test
    fun `getStepStatus returns COMPLETED when fertilizers are selected`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        coEvery { selectedRepo.getSelectedSync(42) } returns listOf(
            SelectedFertilizer(userId = 42, fertilizerId = 1, fertilizerPriceId = null)
        )

        assertEquals(EnumStepStatus.COMPLETED, viewModel.getStepStatus())
    }

    @Test
    fun `getStepStatus returns IN_PROGRESS when no fertilizers are selected`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        coEvery { selectedRepo.getSelectedSync(42) } returns emptyList()

        assertEquals(EnumStepStatus.IN_PROGRESS, viewModel.getStepStatus())
    }

    @Test
    fun `selectFertilizer delegates to selectedRepo with correct values`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.selectFertilizer(
            fertilizerId = 7,
            fertilizerPriceId = 3,
            price = 120.0,
            displayPrice = "120 NGN",
            isExactPrice = true
        )
        advanceUntilIdle()

        coVerify {
            selectedRepo.select(match { sf ->
                sf.userId == 42 &&
                        sf.fertilizerId == 7 &&
                        sf.fertilizerPriceId == 3 &&
                        sf.fertilizerPrice == 120.0 &&
                        sf.displayPrice == "120 NGN" &&
                        sf.isExactPrice
            })
        }
    }

    @Test
    fun `selectFertilizer uses 0 when price is null`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.selectFertilizer(
            fertilizerId = 7,
            fertilizerPriceId = null,
            price = null,
            displayPrice = null,
            isExactPrice = false
        )
        advanceUntilIdle()

        coVerify { selectedRepo.select(match { it.fertilizerPrice == 0.0 }) }
    }

    @Test
    fun `deselectFertilizer delegates to selectedRepo`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.deselectFertilizer(7)
        advanceUntilIdle()

        coVerify { selectedRepo.deselect(42, 7) }
    }

    @Test
    fun `DEFAULT flow observes fertilizers by country`() = runTest {
        buildViewModel(EnumFertilizerFlow.DEFAULT)
        advanceUntilIdle()

        coVerify { fertilizerRepo.observeByCountry(EnumCountry.NG) }
    }

    @Test
    fun `CIM flow observes CIM-available fertilizers`() = runTest {
        buildViewModel(EnumFertilizerFlow.CIM)
        advanceUntilIdle()

        coVerify { fertilizerRepo.observeByCimAvailable(EnumCountry.NG) }
    }

    @Test
    fun `CIS flow observes CIS-available fertilizers`() = runTest {
        buildViewModel(EnumFertilizerFlow.CIS)
        advanceUntilIdle()

        coVerify { fertilizerRepo.observeByCisAvailable(EnumCountry.NG) }
    }
}
