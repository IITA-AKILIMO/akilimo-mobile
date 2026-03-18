package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.UserPreferences
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserSettingsViewModel(
    private val prefsRepo: UserPreferencesRepo,
    private val userRepo: AkilimoUserRepo
) : ViewModel() {

    data class UiState(
        val preferences: UserPreferences? = null,
        val saved: Boolean = false,
        val languageChanged: Boolean = false,
        val newLanguageCode: String = ""
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadPreferences() = viewModelScope.launch {
        val prefs = prefsRepo.getOrDefault()
        _uiState.update { it.copy(preferences = prefs) }
    }

    fun savePreferences(preferences: UserPreferences, userName: String, previousLangCode: String) =
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

            _uiState.update {
                it.copy(
                    saved = true,
                    languageChanged = preferences.languageCode != previousLangCode,
                    newLanguageCode = preferences.languageCode
                )
            }
        }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false, languageChanged = false) }

    companion object {
        fun factory(db: AppDatabase) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return UserSettingsViewModel(
                    UserPreferencesRepo(db.userPreferencesDao()),
                    AkilimoUserRepo(db.akilimoUserDao())
                ) as T
            }
        }
    }
}
