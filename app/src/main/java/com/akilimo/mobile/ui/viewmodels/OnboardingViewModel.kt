package com.akilimo.mobile.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.utils.PlantingSchedule
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.dto.OperationEntry
import com.akilimo.mobile.dto.OperationMethodOption
import com.akilimo.mobile.dto.OperationTypeOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.enums.EnumInvestmentPref
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.enums.EnumOperationType
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CoverageRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.UserSelectionResetter
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.ui.screens.onboarding.OnboardingSection
import com.akilimo.mobile.ui.screens.onboarding.OnboardingStep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.akilimo.mobile.utils.PhoneCountry
import com.akilimo.mobile.utils.PhoneNumberHelper
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val prefsRepo: UserPreferencesRepo,
    private val appSettings: AppSettingsDataStore,
    private val selectionResetter: UserSelectionResetter,
    private val currentPracticeRepo: CurrentPracticeRepo,
    private val adviceCompletionRepo: AdviceCompletionRepo,
    private val coverageRepo: CoverageRepo,
    private val phoneNumbers: PhoneNumberHelper,
    @param:ApplicationContext private val context: Context,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val visibleSections: List<OnboardingSection> = emptyList(),
        /** Editing an existing profile (not first run): Back on page 1 closes, Save works on any page */
        val isEditMode: Boolean = false,
        /** Index into [OnboardingStep.entries] of the page being shown */
        val stepIndex: Int = 0,
        // Disclaimer
        val disclaimerRead: Boolean = false,
        // Terms
        val termsAccepted: Boolean = false,
        val termsUrl: String = "",
        // BioData
        val firstName: String = "",
        val recommendationLanguage: String = "en",
        val lastName: String = "",
        val email: String = "",
        /** Phone number as typed, without the country code */
        val phone: String = "",
        /** ISO region of the phone number's country code ("TZ" → +255); any country in the world */
        val phoneRegion: String = "",
        /** Dialling code of [phoneRegion] (255 for TZ) */
        val phoneDialCode: Int = 0,
        val gender: String = "",
        val interest: String = "",
        // Country
        val country: EnumCountry = EnumCountry.Unsupported,
        /** Country picked while fertilizers are selected, waiting for the user to confirm the change */
        val pendingCountry: EnumCountry? = null,
        // Location
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val altitude: Double = 0.0,
        val zoomLevel: Double = 12.0,
        /** Is the farm inside the area AKILIMO supports for [country]? UNKNOWN never blocks. */
        val locationCoverage: EnumCoverageStatus = EnumCoverageStatus.UNKNOWN,
        // Area Unit
        val areaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val farmSize: Double = 1.0,
        val customFarmSize: Boolean = false,
        /** Saved with the form; the setting is only written on submit */
        val rememberAreaUnit: Boolean = false,
        /** The unit was remembered earlier: hide the unit picker (farm size stays visible) */
        val areaUnitRemembered: Boolean = false,
        // Planting Dates
        val plantingDate: LocalDate? = null,
        val harvestDate: LocalDate? = null,
        val plantingFlex: Long = 0L,
        val harvestFlex: Long = 0L,
        val showFlexOptions: Boolean = false,
        // Tillage
        val tillageOperations: Map<EnumOperationType, EnumOperationMethod> = emptyMap(),
        val weedControlEnabled: Boolean = false,
        val weedControlMethod: EnumWeedControlMethod? = null,
        // Investment
        val investmentPref: EnumInvestmentPref = EnumInvestmentPref.Prompt,
        // Errors keyed by field name
        val errors: Map<String, String> = emptyMap(),
    )

    sealed interface Event {
        // Disclaimer
        data class DisclaimerChecked(val checked: Boolean) : Event

        // Terms
        data class TermsChecked(val checked: Boolean) : Event

        // BioData
        data class FirstNameChanged(val value: String) : Event
        data class LastNameChanged(val value: String) : Event
        data class EmailChanged(val value: String) : Event
        data class PhoneChanged(val value: String) : Event
        data class PhoneRegionSelected(val region: String) : Event
        data class GenderSelected(val value: String) : Event
        data class InterestSelected(val value: String) : Event
        data class RecLanguageSelected(val value: String) : Event

        // Country
        data class CountrySelected(val country: EnumCountry) : Event
        object CountryChangeConfirmed : Event
        object CountryChangeCancelled : Event

        // Location
        data class LocationUpdated(val lat: Double, val lng: Double, val zoom: Double) : Event

        // Area Unit
        data class AreaUnitSelected(val unit: EnumAreaUnit) : Event
        data class FarmSizeSelected(val size: Double, val custom: Boolean) : Event
        data class RememberAreaUnitChanged(val checked: Boolean) : Event

        // Planting dates
        data class PlantingDateSelected(val date: LocalDate) : Event
        data class HarvestDateSelected(val date: LocalDate) : Event
        data class PlantingFlexSelected(val flex: Long) : Event
        data class HarvestFlexSelected(val flex: Long) : Event
        data class ShowFlexOptionsChanged(val show: Boolean) : Event

        // Tillage
        data class TillageOperationToggled(val type: EnumOperationType, val enabled: Boolean) :
            Event

        data class TillageMethodSelected(
            val type: EnumOperationType,
            val method: EnumOperationMethod
        ) : Event

        data class WeedControlToggled(val enabled: Boolean) : Event
        data class WeedControlMethodSelected(val method: EnumWeedControlMethod) : Event

        // Investment
        data class InvestmentPrefSelected(val pref: EnumInvestmentPref) : Event

        data class ShowError(val field: String, val message: String) : Event

        object SubmitClicked : Event
        object BackClicked : Event

        /** Wizard Next: checks only the current page; on the last page it submits */
        object NextClicked : Event

        // Field error clear
        data class ClearError(val field: String) : Event
    }

    sealed interface Effect {
        data object NavigateToRecommendations : Effect
        data object ExitApp : Effect

        /** Back on the first page while editing a profile: leave without saving */
        data object Close : Effect
        data class ShowSnackbar(val message: String) : Effect

        /** Submit failed: scroll [firstField] (an error key) into view and report [count] errors */
        data class ShowValidationErrors(val firstField: String, val count: Int) : Effect
    }

    companion object {
        /** Error keys in the order their fields appear on screen, used to find the first one */
        val FIELD_ORDER = listOf(
            "firstName", "lastName", "email", "phone", "gender", "interest", "rec_language",
            "country", "location",
            "areaUnit", "farmSize",
            "plantingDate", "harvestDate",
            "weedControl", "investmentPref",
        )
    }

    /** Every country the phone number can be from (fixed list, so built once) */
    val phoneCountries: List<PhoneCountry> by lazy { phoneNumbers.allCountries() }

    /**
     * Shown first in the country code picker: the phone's own country, then the countries AKILIMO
     * works in. Everything else follows A–Z.
     */
    val suggestedPhoneRegions: List<String> by lazy {
        (listOf(phoneNumbers.defaultRegion()) +
            EnumCountry.entries.filter { it != EnumCountry.Unsupported }.map { it.name }).distinct()
    }

    /** Has the farmer any choice or completed step for the saved country? Changing country clears them on save. */
    private var hasSelections = false

    /** Country stored for the user when the screen opened */
    private var savedCountry: EnumCountry = EnumCountry.Unsupported

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val userName = appSettings.akilimoUser
            val user = userRepo.getUser(userName)
            val prefs = prefsRepo.getOrDefault()
            val disclaimerRead = appSettings.disclaimerRead
            val termsAccepted = appSettings.termsAccepted
            val rememberAreaUnit = appSettings.rememberAreaUnit
            val termsUrl = appSettings.termsLink

            val visibleSteps = buildList {
                add(OnboardingSection.WELCOME)
                add(OnboardingSection.BIO_DATA)
                add(OnboardingSection.COUNTRY)
                add(OnboardingSection.LOCATION)
                // Always shown: a remembered unit hides only the unit picker, never the farm size
                add(OnboardingSection.AREA_UNIT)
                add(OnboardingSection.PLANTING_DATE)
                add(OnboardingSection.TILLAGE)
                add(OnboardingSection.INVESTMENT_PREF)
                add(OnboardingSection.SUMMARY)
            }

            // Saved numbers are +<code><number>; older ones without + are kept as typed under
            // the phone's own country so the user can fix them
            val storedPhone = user?.mobileNumber ?: prefs.phoneNumber.orEmpty()
            val (phoneRegion, phone) = phoneNumbers.split(storedPhone)
                ?: (phoneNumbers.defaultRegion() to storedPhone)

            savedCountry = user?.enumCountry ?: EnumCountry.Unsupported
            hasSelections = user?.id?.let { selectionResetter.hasAny(it) } ?: false

            _state.update { s ->
                s.copy(
                    isLoading = false,
                    visibleSections = visibleSteps,
                    isEditMode = !appSettings.isFirstRun,
                    disclaimerRead = disclaimerRead,
                    termsAccepted = termsAccepted,
                    rememberAreaUnit = rememberAreaUnit,
                    areaUnitRemembered = rememberAreaUnit,
                    termsUrl = termsUrl,
                    firstName = user?.firstName ?: prefs.firstName.orEmpty(),
                    lastName = user?.lastName ?: prefs.lastName.orEmpty(),
                    email = user?.email ?: prefs.email.orEmpty(),
                    phone = phone,
                    phoneRegion = phoneRegion,
                    phoneDialCode = phoneNumbers.dialCode(phoneRegion),
                    gender = user?.gender ?: prefs.gender.orEmpty(),
                    interest = user?.akilimoInterest.orEmpty(),
                    recommendationLanguage = user?.recommendationLanguage ?: "en",
                    country = user?.enumCountry?.takeIf { it != EnumCountry.Unsupported }
                        ?: prefs.country.takeIf { it != EnumCountry.Unsupported }
                        ?: EnumCountry.Unsupported,
                    latitude = user?.latitude ?: 0.0,
                    longitude = user?.longitude ?: 0.0,
                    altitude = user?.altitude ?: 0.0,
                    zoomLevel = user?.zoomLevel?.takeIf { it > 0 } ?: 12.0,
                    areaUnit = user?.enumAreaUnit ?: prefs.preferredAreaUnit,
                    farmSize = user?.farmSize ?: 1.0,
                    customFarmSize = user?.customFarmSize ?: false,
                    plantingDate = user?.plantingDate,
                    // A date saved under the older, wider range may be refused by the engine: pick it again
                harvestDate = user?.harvestDate?.takeIf { h -> user.plantingDate?.let { PlantingSchedule.isValid(it, h) } == true },
                    plantingFlex = user?.plantingFlex ?: 0L,
                    harvestFlex = user?.harvestFlex ?: 0L,
                    showFlexOptions = (user?.plantingFlex ?: 0L) > 0 || (user?.harvestFlex
                        ?: 0L) > 0,
                    tillageOperations = user?.tillageOperations
                        ?.associate { it.operation.valueOption to it.method.valueOption }
                        ?: emptyMap(),
                    weedControlEnabled = user?.weedControlMethod != null,
                    weedControlMethod = user?.weedControlMethod,
                    investmentPref = user?.investmentPref ?: EnumInvestmentPref.Prompt,
                )
            }
            refreshLocationCoverage()
        }
    }

    private var coverageJob: Job? = null

    /**
     * Re-checks the saved farm location against the supported area of the selected country. A
     * location that was never set (0, 0) or a country without a known area stays UNKNOWN.
     */
    private fun refreshLocationCoverage() {
        coverageJob?.cancel()
        val s = _state.value
        val hasLocation = s.latitude != 0.0 || s.longitude != 0.0
        if (!hasLocation || s.country == EnumCountry.Unsupported) {
            _state.update { it.copy(locationCoverage = EnumCoverageStatus.UNKNOWN) }
            return
        }
        coverageJob = viewModelScope.launch {
            val status = coverageRepo.check(s.country.name, s.latitude, s.longitude)
            _state.update {
                it.copy(
                    locationCoverage = status,
                    errors = if (status == EnumCoverageStatus.OUTSIDE) it.errors else it.errors - "location",
                )
            }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.DisclaimerChecked -> {
                _state.update { it.copy(disclaimerRead = event.checked, errors = it.errors - "disclaimer") }
                appSettings.disclaimerRead = event.checked
            }

            is Event.TermsChecked -> {
                _state.update { it.copy(termsAccepted = event.checked, errors = it.errors - "terms") }
                appSettings.termsAccepted = event.checked
            }

            is Event.FirstNameChanged -> _state.update {
                it.copy(
                    firstName = event.value,
                    errors = it.errors - "firstName"
                )
            }

            is Event.LastNameChanged -> _state.update {
                it.copy(
                    lastName = event.value,
                    errors = it.errors - "lastName"
                )
            }

            is Event.EmailChanged -> _state.update {
                it.copy(
                    email = event.value,
                    errors = it.errors - "email"
                )
            }

            is Event.PhoneChanged -> _state.update {
                it.copy(
                    phone = event.value,
                    errors = it.errors - "phone"
                )
            }

            is Event.PhoneRegionSelected -> _state.update {
                it.copy(
                    phoneRegion = event.region,
                    phoneDialCode = phoneNumbers.dialCode(event.region),
                    errors = it.errors - "phone"
                )
            }

            is Event.GenderSelected -> _state.update {
                it.copy(
                    gender = event.value,
                    errors = it.errors - "gender"
                )
            }

            is Event.InterestSelected -> _state.update {
                it.copy(
                    interest = event.value,
                    errors = it.errors - "interest"
                )
            }

            is Event.RecLanguageSelected -> _state.update {
                it.copy(
                    recommendationLanguage = event.value,
                    errors = it.errors - "rec_language"
                )
            }

            // Nothing is saved (or deleted) until Submit; persistAll clears the choices and progress then
            is Event.CountrySelected -> {
                val current = _state.value.country
                val clearsSelections = hasSelections &&
                    event.country != savedCountry && event.country != current
                if (clearsSelections) {
                    _state.update { it.copy(pendingCountry = event.country) }
                } else {
                    applyCountry(event.country)
                }
            }

            is Event.CountryChangeConfirmed -> {
                val pending = _state.value.pendingCountry
                _state.update { it.copy(pendingCountry = null) }
                pending?.let { applyCountry(it) }
            }

            is Event.CountryChangeCancelled -> _state.update { it.copy(pendingCountry = null) }

            is Event.LocationUpdated -> {
                _state.update {
                    it.copy(latitude = event.lat, longitude = event.lng, zoomLevel = event.zoom)
                }
                refreshLocationCoverage()
            }

            is Event.AreaUnitSelected -> _state.update {
                it.copy(
                    areaUnit = event.unit,
                    errors = it.errors - "areaUnit"
                )
            }

            is Event.FarmSizeSelected -> _state.update {
                it.copy(
                    farmSize = event.size,
                    customFarmSize = event.custom,
                    errors = it.errors - "farmSize"
                )
            }

            // Persisted with the rest of the form in persistAll, not on tick
            is Event.RememberAreaUnitChanged -> _state.update { it.copy(rememberAreaUnit = event.checked) }

            is Event.PlantingDateSelected -> {
                val hadHarvest = _state.value.harvestDate != null &&
                    _state.value.plantingDate != event.date
                _state.update {
                    it.copy(
                        plantingDate = event.date,
                        harvestDate = if (it.plantingDate == event.date) it.harvestDate else null,
                        errors = it.errors - "plantingDate"
                    )
                }
                if (hadHarvest) notifyHarvestCleared()
            }

            is Event.HarvestDateSelected -> _state.update {
                it.copy(
                    harvestDate = event.date,
                    errors = it.errors - "harvestDate"
                )
            }

            // Changing a window changes the valid harvest range; force a re-pick (as DatesScreen does)
            is Event.PlantingFlexSelected -> {
                val s = _state.value
                if (s.plantingFlex != event.flex) {
                    _state.update { it.copy(plantingFlex = event.flex, harvestDate = null) }
                    if (s.harvestDate != null) notifyHarvestCleared()
                }
            }
            is Event.HarvestFlexSelected -> {
                val s = _state.value
                if (s.harvestFlex != event.flex) {
                    _state.update { it.copy(harvestFlex = event.flex, harvestDate = null) }
                    if (s.harvestDate != null) notifyHarvestCleared()
                }
            }
            is Event.ShowFlexOptionsChanged -> _state.update { it.copy(showFlexOptions = event.show) }
            is Event.TillageOperationToggled -> {
                _state.update { s ->
                    val ops = s.tillageOperations.toMutableMap()
                    if (!event.enabled) ops.remove(event.type) else ops[event.type] =
                        ops[event.type] ?: EnumOperationMethod.MANUAL
                    s.copy(tillageOperations = ops)
                }
            }

            is Event.TillageMethodSelected -> _state.update { s ->
                s.copy(tillageOperations = s.tillageOperations + (event.type to event.method))
            }

            is Event.WeedControlToggled -> _state.update {
                it.copy(
                    weedControlEnabled = event.enabled,
                    weedControlMethod = if (!event.enabled) null else it.weedControlMethod
                )
            }

            is Event.WeedControlMethodSelected -> _state.update {
                it.copy(
                    weedControlMethod = event.method,
                    errors = it.errors - "weedControl"
                )
            }

            is Event.InvestmentPrefSelected -> _state.update {
                it.copy(
                    investmentPref = event.pref,
                    errors = it.errors - "investmentPref"
                )
            }

            is Event.ShowError -> _state.update { it.copy(errors = it.errors + (event.field to event.message)) }
            is Event.SubmitClicked -> handleSubmit()
            is Event.BackClicked -> handleBack()
            is Event.NextClicked -> handleNext()
            is Event.ClearError -> _state.update { it.copy(errors = it.errors - event.field) }
        }
    }

    private fun applyCountry(country: EnumCountry) {
        _state.update { it.copy(country = country, errors = it.errors - "country") }
        refreshLocationCoverage()
    }

    /** The harvest date no longer fits the planting date / windows: tell the user to pick it again. */
    private fun notifyHarvestCleared() {
        viewModelScope.launch {
            _effect.send(Effect.ShowSnackbar(context.getString(R.string.msg_harvest_date_cleared)))
        }
    }

    private fun handleBack() {
        val s = _state.value
        when {
            s.stepIndex > 0 -> _state.update { it.copy(stepIndex = it.stepIndex - 1) }
            s.isEditMode -> viewModelScope.launch { _effect.send(Effect.Close) }
            else -> viewModelScope.launch { _effect.send(Effect.ExitApp) }
        }
    }

    private fun handleNext() {
        val s = _state.value
        val steps = OnboardingStep.entries
        if (s.stepIndex >= steps.lastIndex) {
            handleSubmit()
            return
        }
        // Only this page's fields: later pages haven't been seen yet, so no errors there
        val stepErrors = validateAll().filterKeys { it in steps[s.stepIndex].fields }
        if (stepErrors.isNotEmpty()) {
            _state.update { it.copy(errors = it.errors + stepErrors) }
            val first = FIELD_ORDER.firstOrNull { it in stepErrors } ?: stepErrors.keys.first()
            viewModelScope.launch { _effect.send(Effect.ShowValidationErrors(first, stepErrors.size)) }
            return
        }
        _state.update { it.copy(stepIndex = it.stepIndex + 1) }
    }

    private fun handleSubmit() {
        val errors = validateAll()
        if (errors.isNotEmpty()) {
            val first = FIELD_ORDER.firstOrNull { it in errors } ?: errors.keys.first()
            // Go to the page with the first problem (e.g. the farm turned out to be outside the
            // supported area after the user moved on, or Save was tapped on another page)
            _state.update {
                it.copy(errors = errors, stepIndex = OnboardingStep.forField(first).ordinal)
            }
            viewModelScope.launch { _effect.send(Effect.ShowValidationErrors(first, errors.size)) }
            return
        }
        val s = _state.value
        viewModelScope.launch {
            persistAll(s)
            _effect.send(Effect.NavigateToRecommendations)
        }
    }

    private fun validateAll(): Map<String, String> {
        val s = _state.value
        val errs = mutableMapOf<String, String>()

        // BioData
        if (s.firstName.isBlank()) errs["firstName"] =
            context.getString(R.string.error_first_name_required)
        if (s.lastName.isBlank()) errs["lastName"] =
            context.getString(R.string.error_last_name_required)
        if (s.gender.isBlank()) errs["gender"] = context.getString(R.string.error_gender_required)
        if (s.interest.isBlank()) errs["interest"] =
            context.getString(R.string.error_interest_required)
        if (s.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches())
            errs["email"] = context.getString(R.string.error_email_invalid)
        // Optional, but a number that is typed must be real for the chosen country
        if (s.phone.isNotBlank() && !phoneNumbers.isValid(s.phone, s.phoneRegion)) {
            val countryName = Locale("", s.phoneRegion).displayCountry
            errs["phone"] = context.getString(R.string.error_phone_invalid, countryName)
        }

        // Country
        if (s.country == EnumCountry.Unsupported) errs["country"] =
            context.getString(R.string.error_country_required)

        // Location: the API refuses to compute outside the supported area, so stop here instead
        if (s.locationCoverage == EnumCoverageStatus.OUTSIDE) errs["location"] =
            context.getString(R.string.error_location_outside_coverage, s.country.countryName)

        // Area Unit
        if (s.farmSize <= 0.0) {
            errs["farmSize"] = context.getString(R.string.error_farm_size_invalid)
        }

        // Planting Date
        if (s.plantingDate == null) errs["plantingDate"] =
            context.getString(R.string.error_planting_date_required)
        if (s.harvestDate == null) errs["harvestDate"] =
            context.getString(R.string.error_harvest_date_required)

        // Tillage
        if (s.weedControlEnabled && s.weedControlMethod == null)
            errs["weedControl"] = context.getString(R.string.error_weed_control_required)

        // Investment
        if (s.investmentPref.riskLevel() < 0) errs["investmentPref"] =
            context.getString(R.string.error_investment_preference_required)

        return errs
    }

    private suspend fun persistAll(s: UiState) {
        val userName = appSettings.akilimoUser
        val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)

        val tillageOps = s.tillageOperations.entries.map { (type, method) ->
            OperationEntry(
                OperationTypeOption(type.name, type),
                OperationMethodOption(method.name, method),
            )
        }
        val weedMethod = if (s.weedControlEnabled) s.weedControlMethod else null

        val updatedUser = user.copy(
            firstName = s.firstName,
            lastName = s.lastName,
            email = s.email.ifBlank { null },
            // One format for every country: +<code><number>
            mobileNumber = s.phone.ifBlank { null }?.let { phoneNumbers.toE164(it, s.phoneRegion) ?: it },
            gender = s.gender,
            akilimoInterest = s.interest,
            recommendationLanguage = s.recommendationLanguage,
            deviceToken = appSettings.deviceToken,
            enumCountry = s.country,
            latitude = s.latitude,
            longitude = s.longitude,
            zoomLevel = s.zoomLevel,
            altitude = s.altitude,
            enumAreaUnit = s.areaUnit,
            farmSize = s.farmSize,
            customFarmSize = s.customFarmSize,
            plantingDate = s.plantingDate,
            harvestDate = s.harvestDate,
            plantingFlex = s.plantingFlex,
            harvestFlex = s.harvestFlex,
            tillageOperations = tillageOps,
            weedControlMethod = weedMethod,
            investmentPref = s.investmentPref,
        )

        userRepo.saveOrUpdateUser(updatedUser, userName)
        appSettings.isFirstRun = false

        // Update settings
        if (s.disclaimerRead) appSettings.disclaimerRead = true
        if (s.termsAccepted) appSettings.termsAccepted = true
        appSettings.rememberAreaUnit = s.rememberAreaUnit

        val uid = updatedUser.id ?: userRepo.getUser(userName)?.id

        // Choices, prices and progress depend on the country: a saved country change starts them over
        if (user.enumCountry != s.country) {
            uid?.let { selectionResetter.clear(it) }
        }

        // Update current practice
        uid?.let { id ->
            val existing = currentPracticeRepo.getPracticeForUser(id)
            val ploughing = s.tillageOperations[EnumOperationType.PLOUGHING]
            val ridging = s.tillageOperations[EnumOperationType.RIDGING]
            val practice = existing?.copy(
                performPloughing = ploughing != null,
                ploughingMethod = ploughing?.name,
                performRidging = ridging != null,
                ridgingMethod = ridging?.name,
                weedControlMethod = weedMethod ?: existing.weedControlMethod,
            ) ?: CurrentPractice(
                userId = id,
                performPloughing = ploughing != null,
                ploughingMethod = ploughing?.name,
                performRidging = ridging != null,
                ridgingMethod = ridging?.name,
                weedControlMethod = weedMethod,
            )
            currentPracticeRepo.savePractice(practice)

            val hasManual = s.tillageOperations.values.any { it == EnumOperationMethod.MANUAL }
            val hasTractor = s.tillageOperations.values.any { it == EnumOperationMethod.TRACTOR }
            if (hasManual) adviceCompletionRepo.markInProgressIfNotCompleted(EnumAdviceTask.MANUAL_TILLAGE_COST)
            if (hasTractor) adviceCompletionRepo.markInProgressIfNotCompleted(EnumAdviceTask.TRACTOR_ACCESS)
            if (weedMethod != null) {
                adviceCompletionRepo.markInProgressIfNotCompleted(EnumAdviceTask.COST_OF_WEED_CONTROL)
            }
        }
    }
}
