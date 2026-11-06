package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.repos.MaizePerformanceRepo
import com.akilimo.mobile.data.AppSettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.dto.MaizePerfOption
import com.akilimo.mobile.entities.MaizePerformance
import com.akilimo.mobile.enums.EnumMaizePerformance
import com.akilimo.mobile.repos.AkilimoUserRepo
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MaizePerformanceViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val maizeRepo: MaizePerformanceRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(
        /** Every option; [MaizePerfOption.isSelected] reflects the saved value in the DB. */
        val options: List<MaizePerfOption> = EnumMaizePerformance.entries.map { MaizePerfOption(valueOption = it) },
        /** The option highlighted on screen; starts at the saved value, changed by taps. */
        val selected: EnumMaizePerformance? = null,
        val isGridLayout: Boolean = true,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var userPicked = false

    init {
        viewModelScope.launch { loadOptions(appSettings.akilimoUser) }
    }

    fun loadOptions(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val allOptions = EnumMaizePerformance.entries.map { MaizePerfOption(valueOption = it) }
        maizeRepo.observeByUserId(user.id ?: 0).collect { savedPerf ->
            val saved = savedPerf?.maizePerformance
            val options = allOptions.map { option ->
                option.copy(isSelected = option.valueOption == saved)
            }
            _uiState.update {
                it.copy(options = options, selected = if (userPicked) it.selected else saved)
            }
        }
    }

    fun select(option: EnumMaizePerformance) {
        userPicked = true
        _uiState.update { it.copy(selected = option) }
    }

    fun toggleLayout() = _uiState.update { it.copy(isGridLayout = !it.isGridLayout) }

    /** Saves the highlighted option; the screen pops once [UiState.saved] is set. */
    fun saveSelection() {
        val state = _uiState.value
        val selected = state.selected ?: return
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                val user = userRepo.getUser(appSettings.akilimoUser) ?: error("No profile for current user")
                maizeRepo.saveOrUpdatePerformance(
                    MaizePerformance(userId = user.id ?: 0, maizePerformance = selected)
                )
                _uiState.update { s ->
                    s.copy(
                        options = s.options.map { it.copy(isSelected = it.valueOption == selected) },
                        saved = true
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false, isSaving = false) }

    fun onSaveErrorShown() = _uiState.update { it.copy(saveFailed = false) }
}
