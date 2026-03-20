package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.Locales
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepo,
    private val userRepo: AkilimoUserRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(
        val preferences: UserPreferences? = null,
        val previousLanguageCode: String = "",
        val saved: Boolean = false,
        val languageChanged: Boolean = false,
        val newLanguageCode: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadPreferences() = viewModelScope.launch {
        val prefs = prefsRepo.getOrDefault()
        val normalized = prefs.copy(languageCode = Locales.normalize(prefs.languageCode))
        val currentLangTag = appSettings.languageTagFlow.first()
        _uiState.update { it.copy(preferences = normalized, previousLanguageCode = currentLangTag) }
    }

    fun savePreferences(preferences: UserPreferences, userName: String) =
        viewModelScope.launch {
            prefsRepo.save(preferences)

            val akilimoUser = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
            userRepo.saveOrUpdateUser(
                akilimoUser.copy(
                    firstName = preferences.firstName,
                    lastName = preferences.lastName,
                    email = preferences.email,
                    mobileNumber = preferences.phoneNumber,
                    mobileCountryCode = preferences.phoneCountryCode,
                    gender = preferences.gender,
                    enumCountry = preferences.country,
                    enumAreaUnit = preferences.preferredAreaUnit,
                    languageCode = preferences.languageCode,
                    sendEmail = preferences.notifyByEmail,
                    sendSms = preferences.notifyBySms
                ),
                userName
            )

            val previousLangCode = _uiState.value.previousLanguageCode
            appSettings.setLanguageTag(preferences.languageCode)
            appSettings.setDarkMode(preferences.darkMode)

            _uiState.update {
                it.copy(
                    saved = true,
                    languageChanged = preferences.languageCode != previousLangCode,
                    newLanguageCode = preferences.languageCode
                )
            }
        }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false, languageChanged = false) }

}
