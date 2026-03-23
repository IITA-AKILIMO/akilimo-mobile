package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.AkilimoUser
import dagger.hilt.android.lifecycle.HiltViewModel
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
    ) = viewModelScope.launch {
        val userName = appSettings.akilimoUser
        val user = userRepo.getUser(userName) ?: return@launch
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
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false) }

}
