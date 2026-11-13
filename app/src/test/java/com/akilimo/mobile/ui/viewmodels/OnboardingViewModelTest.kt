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
import com.akilimo.mobile.repos.UserSelectionResetter
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.rules.TestDispatcherRule
import com.akilimo.mobile.ui.screens.onboarding.OnboardingSection
import com.akilimo.mobile.ui.screens.onboarding.OnboardingStep
import com.akilimo.mobile.utils.PhoneCountry
import com.akilimo.mobile.utils.PhoneNumberHelper
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
    private val selectionResetter: UserSelectionResetter = mockk(relaxed = true)
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
        coEvery { selectionResetter.hasAny(any()) } returns false
        // DO NOT create the ViewModel here — init coroutine runs on build
    }

    private val phoneNumbers = FakePhoneNumbers()

    private fun buildViewModel() = OnboardingViewModel(
        userRepo, prefsRepo, appSettings,
        selectionResetter, currentPracticeRepo, adviceCompletionRepo, coverageRepo,
        phoneNumbers, context,
    )

    /**
     * Stands in for libphonenumber (which needs the app's assets): TZ (+255) and KE (+254)
     * numbers are valid when they have 9 digits after dropping a leading 0.
     */
    private class FakePhoneNumbers : PhoneNumberHelper {
        private val codes = mapOf("TZ" to 255, "KE" to 254)
        override fun allCountries() = codes.map { (region, code) -> PhoneCountry(region, code) }
        override fun dialCode(region: String) = codes[region] ?: 0
        override fun defaultRegion() = "TZ"
        private fun digits(number: String) = number.filter(Char::isDigit).removePrefix("0")
        override fun isValid(number: String, region: String) = region in codes && digits(number).length == 9
        override fun toE164(number: String, region: String) = codes[region]?.let { "+$it${digits(number)}" }
        override fun split(stored: String): Pair<String, String>? {
            if (!stored.startsWith("+")) return null
            return codes.entries.firstOrNull { stored.startsWith("+${it.value}") }
                ?.let { it.key to stored.removePrefix("+${it.value}") }
        }
    }

    private fun fillValidForm(viewModel: OnboardingViewModel, country: EnumCountry = EnumCountry.TZ) {
        viewModel.onEvent(OnboardingViewModel.Event.FirstNameChanged("Alice"))
        viewModel.onEvent(OnboardingViewModel.Event.LastNameChanged("Smith"))
        viewModel.onEvent(OnboardingViewModel.Event.GenderSelected("F"))
        viewModel.onEvent(OnboardingViewModel.Event.InterestSelected("farmer"))
        viewModel.onEvent(OnboardingViewModel.Event.CountrySelected(country))
        viewModel.onEvent(OnboardingViewModel.Event.PlantingDateSelected(LocalDate.of(2025, 3, 1)))
        viewModel.onEvent(OnboardingViewModel.Event.HarvestDateSelected(LocalDate.of(2026, 1, 1)))
        viewModel.onEvent(OnboardingViewModel.Event.InvestmentPrefSelected(EnumInvestmentPref.Sometimes))
    }

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
    fun `remembered area unit keeps the farm size section and only hides the unit picker`() = runTest {
        every { appSettings.rememberAreaUnit } returns true
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.state.value.visibleSections.contains(OnboardingSection.AREA_UNIT))
        assertTrue(viewModel.state.value.areaUnitRemembered)
    }

    @Test
    fun `ticking remember area unit is saved only on submit`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.RememberAreaUnitChanged(true))

        assertTrue(viewModel.state.value.rememberAreaUnit)
        assertFalse(viewModel.state.value.areaUnitRemembered)
        verify(exactly = 0) { appSettings.rememberAreaUnit = any() }

        fillValidForm(viewModel)
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        advanceUntilIdle()

        verify { appSettings.rememberAreaUnit = true }
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

    @Test
    fun `checking terms or disclaimer clears its required error`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.ShowError("terms", "required"))
        viewModel.onEvent(OnboardingViewModel.Event.ShowError("disclaimer", "required"))
        viewModel.onEvent(OnboardingViewModel.Event.TermsChecked(true))
        viewModel.onEvent(OnboardingViewModel.Event.DisclaimerChecked(true))

        assertNull(viewModel.state.value.errors["terms"])
        assertNull(viewModel.state.value.errors["disclaimer"])
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
    fun `CountrySelected saves and deletes nothing until submit`() = runTest {
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

        assertEquals(EnumCountry.NG, viewModel.state.value.country)
        coVerify(exactly = 0) { selectionResetter.clear(any()) }
        coVerify(exactly = 0) { userRepo.saveOrUpdateUser(any(), any()) }
    }

    @Test
    fun `a changed country clears all choices and completed steps on submit`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            id = 42,
            userName = "user1",
            enumCountry = EnumCountry.TZ,
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel, country = EnumCountry.NG)
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        advanceUntilIdle()

        coVerify { selectionResetter.clear(42) }
    }

    @Test
    fun `an unchanged country keeps the choices and completed steps on submit`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            id = 42,
            userName = "user1",
            enumCountry = EnumCountry.TZ,
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel, country = EnumCountry.TZ)
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        advanceUntilIdle()

        coVerify(exactly = 0) { selectionResetter.clear(any()) }
    }

    @Test
    fun `changing country with saved choices asks for confirmation first`() = runTest {
        coEvery { userRepo.getUser("user1") } returns AkilimoUser(
            id = 42,
            userName = "user1",
            enumCountry = EnumCountry.TZ,
        )
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        coEvery { selectionResetter.hasAny(42) } returns true

        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.onEvent(OnboardingViewModel.Event.CountrySelected(EnumCountry.NG))
        assertEquals(EnumCountry.NG, viewModel.state.value.pendingCountry)
        assertEquals(EnumCountry.TZ, viewModel.state.value.country)

        viewModel.onEvent(OnboardingViewModel.Event.CountryChangeCancelled)
        assertNull(viewModel.state.value.pendingCountry)
        assertEquals(EnumCountry.TZ, viewModel.state.value.country)

        viewModel.onEvent(OnboardingViewModel.Event.CountrySelected(EnumCountry.NG))
        viewModel.onEvent(OnboardingViewModel.Event.CountryChangeConfirmed)
        assertNull(viewModel.state.value.pendingCountry)
        assertEquals(EnumCountry.NG, viewModel.state.value.country)
    }

    @Test
    fun `failed submit reports the first failing field and the error count`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel)
        viewModel.onEvent(OnboardingViewModel.Event.InvestmentPrefSelected(EnumInvestmentPref.Prompt))
        viewModel.onEvent(OnboardingViewModel.Event.FirstNameChanged(""))

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
            advanceUntilIdle()

            val effect = awaitItem() as OnboardingViewModel.Effect.ShowValidationErrors
            assertEquals("firstName", effect.firstField)
            assertEquals(viewModel.state.value.errors.size, effect.count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a custom farm size of zero fails validation`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel)
        viewModel.onEvent(OnboardingViewModel.Event.FarmSizeSelected(0.0, true))
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)

        assertTrue(viewModel.state.value.errors.containsKey("farmSize"))
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
    fun `clearing the harvest date tells the user`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        every { context.getString(R.string.msg_harvest_date_cleared) } returns "cleared"

        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.HarvestDateSelected(LocalDate.of(2026, 1, 1)))

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.PlantingFlexSelected(1L))
            advanceUntilIdle()

            val effect = awaitItem() as OnboardingViewModel.Effect.ShowSnackbar
            assertEquals("cleared", effect.message)
            cancelAndIgnoreRemainingEvents()
        }
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
    fun `BackClicked on the first page while editing closes without saving`() = runTest {
        every { appSettings.isFirstRun } returns false
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
            advanceUntilIdle()

            assertTrue(awaitItem() is OnboardingViewModel.Effect.Close)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { userRepo.saveOrUpdateUser(any(), any()) }
    }

    @Test
    fun `BackClicked on the first page of first run sends ExitApp effect`() = runTest {
        every { appSettings.isFirstRun } returns true
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

    // ── phone number with country code ────────────────────────────────────────

    @Test
    fun `a new profile starts on the phone's own country code`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("TZ", viewModel.state.value.phoneRegion)
        assertEquals(255, viewModel.state.value.phoneDialCode)
        assertEquals("TZ", viewModel.suggestedPhoneRegions.first())
    }

    @Test
    fun `a saved international number is split into country code and number`() = runTest {
        coEvery { userRepo.getUser("user1") } returns
            AkilimoUser(userName = "user1", mobileNumber = "+254712345678")
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()

        val viewModel = buildViewModel()
        advanceUntilIdle()

        assertEquals("KE", viewModel.state.value.phoneRegion)
        assertEquals(254, viewModel.state.value.phoneDialCode)
        assertEquals("712345678", viewModel.state.value.phone)
    }

    @Test
    fun `an invalid number for the chosen country blocks submit, blank does not`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel)

        viewModel.onEvent(OnboardingViewModel.Event.PhoneChanged("1234"))
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        assertTrue("phone" in viewModel.state.value.errors)

        // Picking another country clears the error until the next check
        viewModel.onEvent(OnboardingViewModel.Event.PhoneRegionSelected("KE"))
        assertFalse("phone" in viewModel.state.value.errors)
        assertEquals(254, viewModel.state.value.phoneDialCode)

        viewModel.onEvent(OnboardingViewModel.Event.PhoneChanged(""))
        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        assertFalse("phone" in viewModel.state.value.errors)
    }

    @Test
    fun `the phone number is saved with its country code`() = runTest {
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel)
        viewModel.onEvent(OnboardingViewModel.Event.PhoneRegionSelected("KE"))
        viewModel.onEvent(OnboardingViewModel.Event.PhoneChanged("0712345678"))

        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
        advanceUntilIdle()

        coVerify { userRepo.saveOrUpdateUser(match { it.mobileNumber == "+254712345678" }, "user1") }
    }

    // ── first-run wizard ──────────────────────────────────────────────────────

    private fun buildWizard(): OnboardingViewModel {
        every { appSettings.isFirstRun } returns true
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        return buildViewModel()
    }

    @Test
    fun `edit mode follows first run and both start on the first page`() = runTest {
        every { appSettings.isFirstRun } returns false
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        val editing = buildViewModel()
        advanceUntilIdle()
        assertTrue(editing.state.value.isEditMode)
        assertEquals(0, editing.state.value.stepIndex)

        val wizard = buildWizard()
        advanceUntilIdle()
        assertFalse(wizard.state.value.isEditMode)
        assertEquals(0, wizard.state.value.stepIndex)
    }

    @Test
    fun `Save from any page while editing saves a valid profile`() = runTest {
        every { appSettings.isFirstRun } returns false
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel)

        viewModel.effect.test {
            // Still on page 1: the top-bar Save submits the whole form
            viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)
            advanceUntilIdle()
            assertTrue(awaitItem() is OnboardingViewModel.Effect.NavigateToRecommendations)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Save while editing jumps to the page with the first problem`() = runTest {
        every { appSettings.isFirstRun } returns false
        coEvery { userRepo.getUser("user1") } returns null
        coEvery { prefsRepo.getOrDefault() } returns UserPreferences()
        val viewModel = buildViewModel()
        advanceUntilIdle()
        fillValidForm(viewModel)
        viewModel.onEvent(OnboardingViewModel.Event.NextClicked) // to page 2
        viewModel.onEvent(OnboardingViewModel.Event.NextClicked) // to page 3
        viewModel.onEvent(OnboardingViewModel.Event.FirstNameChanged(""))

        viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked)

        assertEquals(OnboardingStep.ABOUT_YOU.ordinal, viewModel.state.value.stepIndex)
        assertTrue("firstName" in viewModel.state.value.errors)
    }

    @Test
    fun `Next on an incomplete page stays there and flags only that page`() = runTest {
        val viewModel = buildWizard()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.NextClicked)
            advanceUntilIdle()

            val effect = awaitItem() as OnboardingViewModel.Effect.ShowValidationErrors
            assertEquals("firstName", effect.firstField)
            cancelAndIgnoreRemainingEvents()
        }
        val s = viewModel.state.value
        assertEquals(0, s.stepIndex)
        assertTrue("firstName" in s.errors)
        // Later pages are not judged before the user reaches them
        assertFalse("country" in s.errors)
        assertFalse("plantingDate" in s.errors)
    }

    @Test
    fun `Next moves through pages and Back returns to the previous one`() = runTest {
        val viewModel = buildWizard()
        advanceUntilIdle()
        fillValidForm(viewModel)

        viewModel.onEvent(OnboardingViewModel.Event.NextClicked)
        assertEquals(1, viewModel.state.value.stepIndex)
        viewModel.onEvent(OnboardingViewModel.Event.NextClicked)
        assertEquals(2, viewModel.state.value.stepIndex)

        viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
        assertEquals(1, viewModel.state.value.stepIndex)
    }

    @Test
    fun `Back on the first page asks to exit`() = runTest {
        val viewModel = buildWizard()
        advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
            advanceUntilIdle()
            assertTrue(awaitItem() is OnboardingViewModel.Effect.ExitApp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Next on the last page submits the whole form`() = runTest {
        val viewModel = buildWizard()
        advanceUntilIdle()
        fillValidForm(viewModel)
        repeat(OnboardingStep.entries.size - 1) {
            viewModel.onEvent(OnboardingViewModel.Event.NextClicked)
        }
        assertEquals(OnboardingStep.REVIEW.ordinal, viewModel.state.value.stepIndex)

        viewModel.effect.test {
            viewModel.onEvent(OnboardingViewModel.Event.NextClicked)
            advanceUntilIdle()
            assertTrue(awaitItem() is OnboardingViewModel.Effect.NavigateToRecommendations)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { userRepo.saveOrUpdateUser(any(), "user1") }
    }

    @Test
    fun `a submit error found later sends the user back to that page`() = runTest {
        coEvery { coverageRepo.check("TZ", 6.5, 3.4) } returns EnumCoverageStatus.INSIDE
        val viewModel = buildWizard()
        advanceUntilIdle()
        fillValidForm(viewModel)
        viewModel.onEvent(OnboardingViewModel.Event.LocationUpdated(6.5, 3.4, 12.0))
        advanceUntilIdle()
        repeat(OnboardingStep.entries.size - 1) {
            viewModel.onEvent(OnboardingViewModel.Event.NextClicked)
        }

        // The supported-area check changes its answer after the user moved on
        coEvery { coverageRepo.check("TZ", 6.5, 3.4) } returns EnumCoverageStatus.OUTSIDE
        viewModel.onEvent(OnboardingViewModel.Event.LocationUpdated(6.5, 3.4, 12.0))
        advanceUntilIdle()
        viewModel.onEvent(OnboardingViewModel.Event.NextClicked)

        assertEquals(OnboardingStep.FARM_LOCATION.ordinal, viewModel.state.value.stepIndex)
        assertTrue("location" in viewModel.state.value.errors)
    }
}
