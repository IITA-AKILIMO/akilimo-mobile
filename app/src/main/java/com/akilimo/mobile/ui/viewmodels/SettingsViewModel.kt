package com.akilimo.mobile.ui.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettings: AppSettingsDataStore,
) : ViewModel() {

    data class UiState(
        val darkMode: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            appSettings.darkModeFlow.collect { dark ->
                _uiState.update { it.copy(darkMode = dark) }
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
}
