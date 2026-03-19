package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.Locales
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.dto.LanguageOption
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val prefsRepo: UserPreferencesRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(val currentLanguageCode: String = "en")

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadLanguage(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName)
        val prefs = prefsRepo.getOrDefault()
        val raw = user?.languageCode?.takeIf { it.isNotBlank() } ?: prefs.languageCode
        _uiState.update { it.copy(currentLanguageCode = Locales.normalize(raw)) }
    }

    fun saveLanguage(selected: LanguageOption, userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
        userRepo.saveOrUpdateUser(user.copy(languageCode = selected.valueOption), userName)

        val currentPrefs = prefsRepo.getOrDefault()
        prefsRepo.save(currentPrefs.copy(languageCode = selected.valueOption))

        appSettings.setLanguageTag(selected.valueOption)

        _uiState.update { it.copy(currentLanguageCode = selected.valueOption) }
    }

}
