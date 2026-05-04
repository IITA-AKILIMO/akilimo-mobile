package com.akilimo.mobile.ui.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.Locales
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    private val userRepo: AkilimoUserRepo,
    private val prefsRepo: UserPreferencesRepo,
    private val adviceRepo: AdviceCompletionRepo,
) : ViewModel() {

    data class UiState(
        val darkMode: Boolean = false,
        val languageTag: String = Locales.english.toLanguageTag(),
        val rememberAreaUnit: Boolean = false,
        val fertilizerGrid: Boolean = false,
        val lockAppLanguage: Boolean = false,
    )

    sealed interface Effect {
        data class LanguageChanged(val languageTag: String) : Effect
        data class LockAppLanguageChanged(val locked: Boolean, val languageTag: String) : Effect
        data class ShowSnackbar(val message: String) : Effect
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            appSettings.darkModeFlow.collect { dark ->
                _uiState.update { it.copy(darkMode = dark) }
            }
        }
        viewModelScope.launch {
            appSettings.languageTagFlow.collect { tag ->
                _uiState.update { it.copy(languageTag = tag) }
            }
        }
        viewModelScope.launch {
            appSettings.rememberAreaUnitFlow.collect { value ->
                _uiState.update { it.copy(rememberAreaUnit = value) }
            }
        }
        viewModelScope.launch {
            appSettings.fertilizerGridFlow.collect { value ->
                _uiState.update { it.copy(fertilizerGrid = value) }
            }
        }
        viewModelScope.launch {
            appSettings.lockAppLanguageFlow.collect { value ->
                _uiState.update { it.copy(lockAppLanguage = value) }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            appSettings.setDarkMode(enabled)
            AppCompatDelegate.setDefaultNightMode(
                if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
    }

    fun setLanguage(tag: String) {
        viewModelScope.launch {
            val userName = appSettings.akilimoUser
            val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
            userRepo.saveOrUpdateUser(user.copy(languageCode = tag), userName)
            val currentPrefs = prefsRepo.getOrDefault()
            prefsRepo.save(currentPrefs.copy(languageCode = tag))
            appSettings.setLanguageTag(tag)
            _effect.send(Effect.LanguageChanged(tag))
        }
    }

    fun setRememberAreaUnit(value: Boolean) {
        viewModelScope.launch { appSettings.setRememberAreaUnit(value) }
    }

    fun setFertilizerGrid(value: Boolean) {
        viewModelScope.launch { appSettings.setFertilizerGrid(value) }
    }

    fun setLockAppLanguage(locked: Boolean) {
        viewModelScope.launch {
            appSettings.setLockAppLanguage(locked)
            val tag = appSettings.languageTag
            _effect.send(Effect.LockAppLanguageChanged(locked, tag))
        }
    }

    fun clearRecommendations(clearedMessage: String) {
        viewModelScope.launch {
            adviceRepo.clearAll()
            _effect.send(Effect.ShowSnackbar(clearedMessage))
        }
    }

    fun resetNotificationCount(resetMessage: String) {
        viewModelScope.launch {
            appSettings.notificationCount = 3
            _effect.send(Effect.ShowSnackbar(resetMessage))
        }
    }
}
