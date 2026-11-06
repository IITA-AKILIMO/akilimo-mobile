package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import com.akilimo.mobile.repos.AkilimoUserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class DatesViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(
        val user: AkilimoUser? = null,
        val plantingDate: LocalDate? = null,
        val harvestDate: LocalDate? = null,
        val plantingFlex: Long = 0L,
        val harvestFlex: Long = 0L,
        val alternativeDate: Boolean = false,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadData(appSettings.akilimoUser) }
    }

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        _uiState.update {
            it.copy(
                user = user,
                plantingDate = user.plantingDate,
                harvestDate = user.harvestDate,
                plantingFlex = user.plantingFlex,
                harvestFlex = user.harvestFlex,
                alternativeDate = user.providedAlterNativeDate
            )
        }
    }

    fun saveSchedule(
        plantingDate: LocalDate,
        harvestDate: LocalDate,
        plantingFlex: Long,
        harvestFlex: Long,
        alternativeDate: Boolean
    ) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                val userName = appSettings.akilimoUser
                val user = userRepo.getUser(userName) ?: error("No profile for current user")
                userRepo.saveOrUpdateUser(
                    user.copy(
                        plantingDate = plantingDate,
                        harvestDate = harvestDate,
                        plantingFlex = plantingFlex,
                        harvestFlex = harvestFlex,
                        providedAlterNativeDate = alternativeDate
                    ), userName
                )
                _uiState.update { it.copy(saved = true) }
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
