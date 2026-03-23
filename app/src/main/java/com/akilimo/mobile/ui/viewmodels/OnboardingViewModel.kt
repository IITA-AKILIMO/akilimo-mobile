package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.Locales
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.dto.OperationEntry
import com.akilimo.mobile.dto.OperationMethodOption
import com.akilimo.mobile.dto.OperationTypeOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumInvestmentPref
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.enums.EnumOperationType
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import com.akilimo.mobile.wizard.OnboardingStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val prefsRepo: UserPreferencesRepo,
    private val appSettings: AppSettingsDataStore,
    private val selectedFertilizerRepo: SelectedFertilizerRepo,
    private val currentPracticeRepo: CurrentPracticeRepo,
    private val adviceCompletionRepo: AdviceCompletionRepo,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val visibleSteps: List<OnboardingStep> = emptyList(),
        val currentStepIndex: Int = 0,
        // Welcome
        val languageCode: String = Locales.english.toLanguageTag(),
        // Disclaimer
        val disclaimerRead: Boolean = false,
        // Terms
        val termsAccepted: Boolean = false,
        val termsUrl: String = "",
        // BioData
        val firstName: String = "",
        val lastName: String = "",
        val email: String = "",
        val phone: String = "",
        val gender: String = "",
        val interest: String = "",
        // Country
        val country: EnumCountry = EnumCountry.Unsupported,
        // Location
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val altitude: Double = 0.0,
        val zoomLevel: Double = 12.0,
        // Area Unit
        val areaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val farmSize: Double = 1.0,
        val customFarmSize: Boolean = false,
        val rememberAreaUnit: Boolean = false,
        // Planting Dates
        val plantingDate: LocalDate? = null,
        val harvestDate: LocalDate? = null,
        val plantingFlex: Long = 0L,
        val harvestFlex: Long = 0L,
        val showFlexOptions: Boolean = false,
        // Tillage
        val tillageOperations: Map<EnumOperationType, EnumOperationMethod> = emptyMap(),
        val weedControlEnabled: Boolean = true,
        val weedControlMethod: EnumWeedControlMethod? = null,
        // Investment
        val investmentPref: EnumInvestmentPref = EnumInvestmentPref.Prompt,
        // Errors keyed by field name
        val errors: Map<String, String> = emptyMap(),
    ) {
        val currentStep: OnboardingStep
            get() = visibleSteps.getOrElse(currentStepIndex) { OnboardingStep.WELCOME }
        val isLastStep: Boolean get() = currentStepIndex == visibleSteps.lastIndex
        val isFirstStep: Boolean get() = currentStepIndex == 0
    }

    sealed interface Event {
        // Welcome
        data class LanguageSelected(val code: String) : Event
        // Disclaimer
        data class DisclaimerChecked(val checked: Boolean) : Event
        // Terms
        data class TermsChecked(val checked: Boolean) : Event
        // BioData
        data class FirstNameChanged(val value: String) : Event
        data class LastNameChanged(val value: String) : Event
        data class EmailChanged(val value: String) : Event
        data class PhoneChanged(val value: String) : Event
        data class GenderSelected(val value: String) : Event
        data class InterestSelected(val value: String) : Event
        // Country
        data class CountrySelected(val country: EnumCountry) : Event
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
        data class TillageOperationToggled(val type: EnumOperationType, val enabled: Boolean) : Event
        data class TillageMethodSelected(val type: EnumOperationType, val method: EnumOperationMethod) : Event
        data class WeedControlToggled(val enabled: Boolean) : Event
        data class WeedControlMethodSelected(val method: EnumWeedControlMethod) : Event
        // Investment
        data class InvestmentPrefSelected(val pref: EnumInvestmentPref) : Event
        // Navigation
        data object NextClicked : Event
        data object BackClicked : Event
        // Field error clear
        data class ClearError(val field: String) : Event
    }

    sealed interface Effect {
        data object NavigateToRecommendations : Effect
        data object ExitApp : Effect
        data class ShowSnackbar(val message: String) : Effect
        data class LanguageChangeRequested(val languageTag: String) : Effect
    }

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
            val languageCode = appSettings.languageTag

            val visibleSteps = buildList {
                add(OnboardingStep.WELCOME)
                if (!disclaimerRead) add(OnboardingStep.DISCLAIMER)
                if (!termsAccepted) add(OnboardingStep.TERMS)
                add(OnboardingStep.BIO_DATA)
                add(OnboardingStep.COUNTRY)
                add(OnboardingStep.LOCATION)
                if (!rememberAreaUnit) add(OnboardingStep.AREA_UNIT)
                add(OnboardingStep.PLANTING_DATE)
                add(OnboardingStep.TILLAGE)
                add(OnboardingStep.INVESTMENT_PREF)
                add(OnboardingStep.SUMMARY)
            }

            _state.update { s ->
                s.copy(
                    isLoading = false,
                    visibleSteps = visibleSteps,
                    languageCode = languageCode,
                    disclaimerRead = disclaimerRead,
                    termsAccepted = termsAccepted,
                    rememberAreaUnit = rememberAreaUnit,
                    termsUrl = termsUrl,
                    firstName = user?.firstName ?: prefs.firstName.orEmpty(),
                    lastName = user?.lastName ?: prefs.lastName.orEmpty(),
                    email = user?.email ?: prefs.email.orEmpty(),
                    phone = user?.mobileNumber ?: prefs.phoneNumber.orEmpty(),
                    gender = user?.gender ?: prefs.gender.orEmpty(),
                    interest = user?.akilimoInterest.orEmpty(),
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
                    harvestDate = user?.harvestDate,
                    plantingFlex = user?.plantingFlex ?: 0L,
                    harvestFlex = user?.harvestFlex ?: 0L,
                    showFlexOptions = (user?.plantingFlex ?: 0L) > 0 || (user?.harvestFlex ?: 0L) > 0,
                    tillageOperations = user?.tillageOperations
                        ?.associate { it.operation.valueOption to it.method.valueOption }
                        ?: emptyMap(),
                    weedControlMethod = user?.weedControlMethod,
                    investmentPref = user?.investmentPref ?: EnumInvestmentPref.Prompt,
                )
            }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.LanguageSelected -> {
                _state.update { it.copy(languageCode = event.code) }
                viewModelScope.launch {
                    val userName = appSettings.akilimoUser
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(user.copy(languageCode = event.code), userName)
                    val currentPrefs = prefsRepo.getOrDefault()
                    prefsRepo.save(currentPrefs.copy(languageCode = event.code))
                    appSettings.setLanguageTag(event.code)
                    _effect.send(Effect.LanguageChangeRequested(event.code))
                }
            }
            is Event.DisclaimerChecked -> {
                _state.update { it.copy(disclaimerRead = event.checked) }
                appSettings.disclaimerRead = event.checked
            }
            is Event.TermsChecked -> {
                _state.update { it.copy(termsAccepted = event.checked) }
                appSettings.termsAccepted = event.checked
            }
            is Event.FirstNameChanged -> _state.update { it.copy(firstName = event.value, errors = it.errors - "firstName") }
            is Event.LastNameChanged -> _state.update { it.copy(lastName = event.value, errors = it.errors - "lastName") }
            is Event.EmailChanged -> _state.update { it.copy(email = event.value, errors = it.errors - "email") }
            is Event.PhoneChanged -> _state.update { it.copy(phone = event.value, errors = it.errors - "phone") }
            is Event.GenderSelected -> _state.update { it.copy(gender = event.value, errors = it.errors - "gender") }
            is Event.InterestSelected -> _state.update { it.copy(interest = event.value, errors = it.errors - "interest") }
            is Event.CountrySelected -> {
                _state.update { it.copy(country = event.country, errors = it.errors - "country") }
                viewModelScope.launch {
                    val userName = appSettings.akilimoUser
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(user.copy(enumCountry = event.country), userName)
                    user.id?.let { selectedFertilizerRepo.deleteByUserId(it) }
                }
            }
            is Event.LocationUpdated -> _state.update {
                it.copy(latitude = event.lat, longitude = event.lng, zoomLevel = event.zoom)
            }
            is Event.AreaUnitSelected -> _state.update { it.copy(areaUnit = event.unit, errors = it.errors - "areaUnit") }
            is Event.FarmSizeSelected -> _state.update { it.copy(farmSize = event.size, customFarmSize = event.custom, errors = it.errors - "farmSize") }
            is Event.RememberAreaUnitChanged -> {
                _state.update { it.copy(rememberAreaUnit = event.checked) }
                appSettings.rememberAreaUnit = event.checked
            }
            is Event.PlantingDateSelected -> _state.update { it.copy(plantingDate = event.date, harvestDate = null, errors = it.errors - "plantingDate") }
            is Event.HarvestDateSelected -> _state.update { it.copy(harvestDate = event.date, errors = it.errors - "harvestDate") }
            is Event.PlantingFlexSelected -> _state.update { it.copy(plantingFlex = event.flex) }
            is Event.HarvestFlexSelected -> _state.update { it.copy(harvestFlex = event.flex) }
            is Event.ShowFlexOptionsChanged -> _state.update { it.copy(showFlexOptions = event.show) }
            is Event.TillageOperationToggled -> {
                _state.update { s ->
                    val ops = s.tillageOperations.toMutableMap()
                    if (!event.enabled) ops.remove(event.type) else ops[event.type] = ops[event.type] ?: EnumOperationMethod.MANUAL
                    s.copy(tillageOperations = ops)
                }
            }
            is Event.TillageMethodSelected -> _state.update { s ->
                s.copy(tillageOperations = s.tillageOperations + (event.type to event.method))
            }
            is Event.WeedControlToggled -> _state.update { it.copy(weedControlEnabled = event.enabled, weedControlMethod = if (!event.enabled) null else it.weedControlMethod) }
            is Event.WeedControlMethodSelected -> _state.update { it.copy(weedControlMethod = event.method, errors = it.errors - "weedControl") }
            is Event.InvestmentPrefSelected -> _state.update { it.copy(investmentPref = event.pref, errors = it.errors - "investmentPref") }
            is Event.NextClicked -> handleNext()
            is Event.BackClicked -> handleBack()
            is Event.ClearError -> _state.update { it.copy(errors = it.errors - event.field) }
        }
    }

    private fun handleBack() {
        val s = _state.value
        if (s.currentStepIndex > 0) {
            _state.update { it.copy(currentStepIndex = it.currentStepIndex - 1, errors = emptyMap()) }
        } else {
            viewModelScope.launch { _effect.send(Effect.ExitApp) }
        }
    }

    private fun handleNext() {
        val errors = validateCurrentStep()
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors) }
            return
        }
        val s = _state.value
        // Persist current step's data to DB
        persistCurrentStep(s)
        if (s.isLastStep) {
            viewModelScope.launch { _effect.send(Effect.NavigateToRecommendations) }
        } else {
            _state.update { it.copy(currentStepIndex = it.currentStepIndex + 1, errors = emptyMap()) }
        }
    }

    private fun validateCurrentStep(): Map<String, String> {
        val s = _state.value
        return when (s.currentStep) {
            OnboardingStep.DISCLAIMER -> {
                if (!s.disclaimerRead) mapOf("disclaimer" to "You must agree to the disclaimer to continue")
                else emptyMap()
            }
            OnboardingStep.TERMS -> {
                if (!s.termsAccepted) mapOf("terms" to "You must accept the terms and conditions to continue")
                else emptyMap()
            }
            OnboardingStep.BIO_DATA -> {
                val errs = mutableMapOf<String, String>()
                if (s.firstName.isBlank()) errs["firstName"] = "First name is required"
                if (s.lastName.isBlank()) errs["lastName"] = "Last name is required"
                if (s.gender.isBlank()) errs["gender"] = "Please select a gender"
                if (s.interest.isBlank()) errs["interest"] = "Please select your interest"
                if (s.email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(s.email).matches())
                    errs["email"] = "Please enter a valid email address"
                errs
            }
            OnboardingStep.COUNTRY -> {
                if (s.country == EnumCountry.Unsupported) mapOf("country" to "Please select your country")
                else emptyMap()
            }
            OnboardingStep.AREA_UNIT -> {
                val errs = mutableMapOf<String, String>()
                if (s.farmSize <= 0.0) errs["farmSize"] = "Please enter a valid farm size"
                errs
            }
            OnboardingStep.PLANTING_DATE -> {
                val errs = mutableMapOf<String, String>()
                if (s.plantingDate == null) errs["plantingDate"] = "Please select a planting date"
                if (s.harvestDate == null) errs["harvestDate"] = "Please select a harvest date"
                errs
            }
            OnboardingStep.TILLAGE -> {
                if (s.weedControlEnabled && s.weedControlMethod == null)
                    mapOf("weedControl" to "Please select a weed control method")
                else emptyMap()
            }
            OnboardingStep.INVESTMENT_PREF -> {
                if (s.investmentPref.riskLevel() < 0) mapOf("investmentPref" to "Please select an investment preference")
                else emptyMap()
            }
            else -> emptyMap()
        }
    }

    private fun persistCurrentStep(s: UiState) {
        viewModelScope.launch {
            val userName = appSettings.akilimoUser
            when (s.currentStep) {
                OnboardingStep.BIO_DATA -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(
                        user.copy(
                            firstName = s.firstName,
                            lastName = s.lastName,
                            email = s.email.ifBlank { null },
                            mobileNumber = s.phone.ifBlank { null },
                            gender = s.gender,
                            akilimoInterest = s.interest,
                            deviceToken = appSettings.deviceToken,
                        ),
                        userName,
                    )
                }
                OnboardingStep.COUNTRY -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(user.copy(enumCountry = s.country), userName)
                    user.id?.let { selectedFertilizerRepo.deleteByUserId(it) }
                }
                OnboardingStep.LOCATION -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(
                        user.copy(
                            latitude = s.latitude,
                            longitude = s.longitude,
                            zoomLevel = s.zoomLevel,
                            altitude = s.altitude,
                        ),
                        userName,
                    )
                }
                OnboardingStep.AREA_UNIT -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(
                        user.copy(enumAreaUnit = s.areaUnit, farmSize = s.farmSize, customFarmSize = s.customFarmSize),
                        userName,
                    )
                }
                OnboardingStep.PLANTING_DATE -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(
                        user.copy(
                            plantingDate = s.plantingDate,
                            harvestDate = s.harvestDate,
                            plantingFlex = s.plantingFlex,
                            harvestFlex = s.harvestFlex,
                        ),
                        userName,
                    )
                }
                OnboardingStep.TILLAGE -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    val ops = s.tillageOperations.entries.map { (type, method) ->
                        OperationEntry(
                            OperationTypeOption(type.name, type),
                            OperationMethodOption(method.name, method),
                        )
                    }
                    val weedMethod = if (s.weedControlEnabled) s.weedControlMethod else null
                    userRepo.saveOrUpdateUser(user.copy(tillageOperations = ops, weedControlMethod = weedMethod), userName)
                    user.id?.let { uid ->
                        val existing = currentPracticeRepo.getPracticeForUser(uid)
                        val ploughing = s.tillageOperations[EnumOperationType.PLOUGHING]
                        val ridging = s.tillageOperations[EnumOperationType.RIDGING]
                        val updated = existing?.copy(
                            performPloughing = ploughing != null,
                            ploughingMethod = ploughing?.name,
                            performRidging = ridging != null,
                            ridgingMethod = ridging?.name,
                            weedControlMethod = weedMethod ?: existing.weedControlMethod,
                        ) ?: CurrentPractice(
                            userId = uid,
                            performPloughing = ploughing != null,
                            ploughingMethod = ploughing?.name,
                            performRidging = ridging != null,
                            ridgingMethod = ridging?.name,
                            weedControlMethod = weedMethod,
                        )
                        currentPracticeRepo.savePractice(updated)
                        val hasManual = s.tillageOperations.values.any { it == EnumOperationMethod.MANUAL }
                        val hasTractor = s.tillageOperations.values.any { it == EnumOperationMethod.TRACTOR }
                        if (hasManual) adviceCompletionRepo.markInProgressIfNotCompleted(EnumAdviceTask.MANUAL_TILLAGE_COST)
                        if (hasTractor) adviceCompletionRepo.markInProgressIfNotCompleted(EnumAdviceTask.TRACTOR_ACCESS)
                        if (weedMethod != null) adviceCompletionRepo.markInProgressIfNotCompleted(EnumAdviceTask.COST_OF_WEED_CONTROL)
                    }
                }
                OnboardingStep.INVESTMENT_PREF -> {
                    val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
                    userRepo.saveOrUpdateUser(user.copy(investmentPref = s.investmentPref), userName)
                }
                else -> Unit
            }
        }
    }
}
