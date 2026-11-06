package com.akilimo.mobile.ui.viewmodels

import app.cash.turbine.test
import com.akilimo.mobile.R
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.enums.EnumInvestmentPref
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CoverageRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import com.akilimo.mobile.ui.screens.onboarding.OnboardingSection
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private val userRepo: AkilimoUserRepo = mockk(relaxed = true)
    private val prefsRepo: UserPreferencesRepo = mockk(relaxed = true)
    private val appSettings: AppSettingsDataStore = mockk(relaxed = true)
    private val selectedFertilizerRepo: SelectedFertilizerRepo = mockk(relaxed = true)
    private val currentPracticeRepo: CurrentPracticeRepo = mockk(relaxed = true)
    private val adviceCompletionRepo: AdviceCompletionRepo = mockk(relaxed = true)
    private val coverageRepo: CoverageRepo = mockk(relaxed = true)
    private val context: android.content.Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        clearAllMocks()
        every { appSettings.akilimoUser } returns "user1"
        every { appSettings.disclaimerRead } returns false
        every { appSettings.termsAccepted } returns false
        every { appSettings.rememberAreaUnit } returns false
        every { appSettings.termsLink } returns "https://example.com"
        every { appSettings.languageTag } returns "en"
        // DO NOT create the ViewModel here — init coroutine runs on build
    }

    private fun buildViewModel() = OnboardingViewModel(
        userRepo, prefsRepo, appSettings,
        selectedFertilizerRepo, currentPracticeRepo, adviceCompletionRepo, coverageRepo, context,
    )

    // ── supported area (coverage) ─────────────────────────────────────────────

    @Test
    fun `location outside the supported area is flagged and blocks submit`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.NG)
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        coEvery { coverageRepo.check("NG", 6.5, 3.4) } returns EnumCoverageStatus.OUTSIDE
        every { context.getString(R.string.error_location_outside_coverage, *anyVararg()) } returns "outside"

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.LocationUpdated(6.5, 3.4, 12.0))
        advanceUntilIdle()

        assertEquals(EnumCoverageStatus.OUTSIDE, viewModel.state.value.locationCoverage)
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        assertEquals("outside", viewModel.state.value.errors["location"])
    }

    @Test
    fun `moving the location inside the supported area clears the location error`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.NG)
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        coEvery { coverageRepo.check("NG", 6.5, 3.4) } returns EnumCoverageStatus.OUTSIDE
        coEvery { coverageRepo.check("NG", 7.4, 3.9) } returns EnumCoverageStatus.INSIDE

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.LocationUpdated(6.5, 3.4, 12.0))
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        viewModel.onEvent(OnboardingViewModel.Event.LocationUpdated(7.4, 3.9, 12.0))
        advanceUntilIdle()

        assertEquals(EnumCoverageStatus.INSIDE, viewModel.state.value.locationCoverage)
        assertNull(viewModel.state.value.errors["location"])
    }

    @Test
    fun `unknown coverage never blocks and an unset location is not checked`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1", enumCountry = EnumCountry.TZ)
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        coEvery { coverageRepo.check(any(), any(), any()) } returns EnumCoverageStatus.UNKNOWN

        val viewModel = buildViewModel()
        advanceUntilIdle()
        coVerify(exactly = 0) { coverageRepo.check(any(), any(), any()) } // still at 0, 0

        viewModel.onEvent(OnboardingViewModel.Event.LocationUpdated(-6.8, 39.2, 12.0))
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)

        assertEquals(EnumCoverageStatus.UNKNOWN, viewModel.state.value.locationCoverage)
        assertNull(viewModel.state.value.errors["location"])
    }

    // ── init / loadInitialState ───────────────────────────────────────────────

    @Test
    fun `loadInitialState populates state from persisted user`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            userName = "user1",
            firstName = "Alice",
            lastName = "Smith",
            enumCountry = EnumCountry.TZ,
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertFalse(isLoading)
            assertEquals("Alice", firstName)
            assertEquals("Smith", lastName)
            assertEquals(EnumCountry.TZ, country)
        }
    }

    @Test
    fun `loadInitialState enables weed control when user has a saved method`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            userName = "user1",
            weedControlMethod = EnumWeedControlMethod.HERBICIDE,
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        with(viewModel.state.value) {
            assertTrue(weedControlEnabled)
            assertEquals(EnumWeedControlMethod.HERBICIDE, weedControlMethod)
        }
    }

    @Test
    fun `loadInitialState leaves weed control disabled when no method is saved`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(userName = "user1")
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.weedControlEnabled)
    }

    @Test
    fun `loadInitialState falls back to prefs when user is null`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences(
            firstName = "Bob",
            country = EnumCountry.NG,
        )

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("Bob", viewModel.state.value.firstName)
        assertEquals(EnumCountry.NG, viewModel.state.value.country)
    }

    @Test
    fun `loadInitialState includes AREA_UNIT section when rememberAreaUnit is false`() = runTest {
        every { appSettings.rememberAreaUnit } returns false
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.visibleSections.contains(OnboardingSection.AREA_UNIT))
    }

    @Test
    fun `loadInitialState excludes AREA_UNIT section when rememberAreaUnit is true`() = runTest {
        every { appSettings.rememberAreaUnit } returns true
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.state.value.visibleSections.contains(OnboardingSection.AREA_UNIT))
    }

    // ── Disclaimer / Terms ────────────────────────────────────────────────────

    @Test
    fun `DisclaimerChecked updates disclaimerRead in state`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        viewModel.onEvent(OnboardingViewModel.Event.DisclaimerChecked(true))

        assertTrue(viewModel.state.value.disclaimerRead)
    }

    @Test
    fun `TermsChecked updates termsAccepted in state`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        viewModel.onEvent(OnboardingViewModel.Event.TermsChecked(true))

        assertTrue(viewModel.state.value.termsAccepted)
    }

    // ── Field events ──────────────────────────────────────────────────────────

    @Test
    fun `FirstNameChanged updates firstName and clears its error`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        // Trigger validation to produce an error first
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        assertTrue(viewModel.state.value.errors.containsKey("firstName"))

        viewModel.onEvent(OnboardingViewModel.Event.FirstNameChanged("Alice"))

        assertEquals("Alice", viewModel.state.value.firstName)
        assertNull(viewModel.state.value.errors["firstName"])
    }

    // ── CountrySelected ───────────────────────────────────────────────────────

    @Test
    fun `CountrySelected deletes fertilizers when country changes`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            id = 42,
            userName = "user1",
            enumCountry = EnumCountry.TZ,
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(OnboardingViewModel.Event.CountrySelected(EnumCountry.NG))
        advanceUntilIdle()

        coVerify { selectedFertilizerRepo.deleteByUserId(42) }
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun `SubmitClicked sets errors for all blank required fields`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)

        val errors = viewModel.state.value.errors
        assertTrue(errors.containsKey("firstName"))
        assertTrue(errors.containsKey("lastName"))
        assertTrue(errors.containsKey("gender"))
        assertTrue(errors.containsKey("interest"))
        assertTrue(errors.containsKey("country"))
        assertTrue(errors.containsKey("plantingDate"))
        assertTrue(errors.containsKey("harvestDate"))
        assertTrue(errors.containsKey("investmentPref"))
    }

    @Test
    fun `SubmitClicked sends NavigateToRecommendations when all required fields are valid`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(OnboardingViewModel.Event.FirstNameChanged("Alice"))
        viewModel.onEvent(OnboardingViewModel.Event.LastNameChanged("Smith"))
        viewModel.onEvent(OnboardingViewModel.Event.GenderSelected("F"))
        viewModel.onEvent(OnboardingViewModel.Event.InterestSelected("CASSAVA"))
        viewModel.onEvent(OnboardingViewModel.Event.CountrySelected(EnumCountry.TZ))
        viewModel.onEvent(OnboardingViewModel.Event.PlantingDateSelected(LocalDate.of(2025, 3, 1)))
        viewModel.onEvent(OnboardingViewModel.Event.HarvestDateSelected(LocalDate.of(2026, 1, 1)))
        viewModel.onEvent(OnboardingViewModel.Event.WeedControlToggled(false))
        viewModel.onEvent(OnboardingViewModel.Event.InvestmentPrefSelected(EnumInvestmentPref.Sometimes))
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.errors.isEmpty())
            assertTrue(awaitItem() is OnboardingViewModel.Effect.NavigateToRecommendations)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Flex windows ──────────────────────────────────────────────────────────

    @Test
    fun `changing harvest flex clears the selected harvest date`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.HarvestDateSelected(LocalDate.of(2026, 1, 1)))
        viewModel.onEvent(OnboardingViewModel.Event.HarvestFlexSelected(2L))

        assertNull(viewModel.state.value.harvestDate)
        assertEquals(2L, viewModel.state.value.harvestFlex)
    }

    @Test
    fun `re-selecting the same flex keeps the harvest date`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        val date = LocalDate.of(2026, 1, 1)
        viewModel.onEvent(OnboardingViewModel.Event.HarvestDateSelected(date))
        viewModel.onEvent(OnboardingViewModel.Event.PlantingFlexSelected(0L))

        assertEquals(date, viewModel.state.value.harvestDate)
    }

    // ── BackClicked ───────────────────────────────────────────────────────────

    @Test
    fun `BackClicked sends ExitApp effect`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
            advanceUntilIdle()

            assertTrue(awaitItem() is OnboardingViewModel.Effect.ExitApp)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
