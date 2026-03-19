package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.dto.LanguageOption
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val userRepo: AkilimoUserRepo,
    private val prefsRepo: UserPreferencesRepo
) : ViewModel() {

    data class UiState(val currentLanguageCode: String = "en")

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadLanguage(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName)
        val prefs = prefsRepo.getOrDefault()
        val code = user?.languageCode?.takeIf { it.isNotBlank() } ?: prefs.languageCode
        _uiState.update { it.copy(currentLanguageCode = code) }
    }

    fun saveLanguage(selected: LanguageOption, userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: AkilimoUser(userName = userName)
        userRepo.saveOrUpdateUser(user.copy(languageCode = selected.valueOption), userName)

        val currentPrefs = prefsRepo.getOrDefault()
        prefsRepo.save(currentPrefs.copy(languageCode = selected.valueOption))

        _uiState.update { it.copy(currentLanguageCode = selected.valueOption) }
    }

    companion object {
        fun factory(db: AppDatabase) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return WelcomeViewModel(
                    AkilimoUserRepo(db.akilimoUserDao()),
                    UserPreferencesRepo(db.userPreferencesDao())
                ) as T
            }
        }
    }
}
