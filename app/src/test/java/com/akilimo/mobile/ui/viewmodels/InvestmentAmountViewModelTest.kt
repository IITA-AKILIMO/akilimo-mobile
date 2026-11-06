package com.akilimo.mobile.ui.viewmodels

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.enums.EnumAreaUnit
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InvestmentAmountViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val investmentRepo: InvestmentRepo = mockk(relaxed = true)
    private val selectedRepo: SelectedInvestmentRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)

    private fun amount(perUnit: Double, unit: String, id: Int = 1) = InvestmentAmount(
        id = id, countryCode = "NG", currencyCode = "NGN", currencySymbol = "₦",
        investmentAmount = perUnit, exactAmount = false, active = true,
        areaUnit = unit, sortOrder = id
    )

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        every { selectedRepo.observeSelected(any()) } returns flowOf(null)
    }

    private fun userWith(size: Double, unit: EnumAreaUnit) =
        AkilimoUser(id = 9, userName = "user1", farmSize = size, enumAreaUnit = unit, enumCountry = EnumCountry.NG)

    private fun buildViewModel() =
        InvestmentAmountViewModel(userRepo, investmentRepo, selectedRepo, appSettings)

    private fun buildFor(user: AkilimoUser, vararg items: InvestmentAmount): InvestmentAmountViewModel {
        coEvery { userRepo.getUser("user1") } returns user
        every { investmentRepo.observeAllByCountry(EnumCountry.NG) } returns flowOf(items.toList())
        return buildViewModel()
    }

    private fun InvestmentAmountViewModel.total() = uiState.value.investments.single().investmentAmount

    @Test
    fun `amount per hectare times farm size in hectares`() = runTest {
        val vm = buildFor(userWith(2.0, EnumAreaUnit.HA), amount(100.0, "ha"))
        advanceUntilIdle()
        assertEquals(200.0, vm.total(), 0.001)
    }

    @Test
    fun `amount per acre with farm size in hectares converts the farm to acres`() = runTest {
        // 2 ha = 4.942 acres; previously this produced 2 * 0.404686 = 0.81 acres
        val vm = buildFor(userWith(2.0, EnumAreaUnit.HA), amount(100.0, "acre"))
        advanceUntilIdle()
        assertEquals(494.21, vm.total(), 0.01)
    }

    @Test
    fun `amount per acre with farm size in acres is unchanged`() = runTest {
        val vm = buildFor(userWith(3.0, EnumAreaUnit.ACRE), amount(50.0, "acre"))
        advanceUntilIdle()
        assertEquals(150.0, vm.total(), 0.001)
    }

    @Test
    fun `unknown area unit falls back to the user's own unit`() = runTest {
        val vm = buildFor(userWith(2.0, EnumAreaUnit.HA), amount(100.0, "plot"))
        advanceUntilIdle()
        assertEquals(200.0, vm.total(), 0.001)
    }

    @Test
    fun `saveInvestment stores the chosen amount for the user`() = runTest {
        coEvery { userRepo.getUser("user1") } returns userWith(1.0, EnumAreaUnit.ACRE)
        every { investmentRepo.observeAllByCountry(any()) } returns flowOf(emptyList())
        coEvery { selectedRepo.getSelectedSync(9) } returns null
        val vm = buildViewModel()
        advanceUntilIdle()

        vm.saveInvestment(amount(75.0, "acre", id = 4), selectedAmount = 75.0)
        advanceUntilIdle()

        coVerify { selectedRepo.saveOrUpdate(SelectedInvestment(userId = 9, investmentId = 4, chosenAmount = 75.0)) }
    }
}
