package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ManualTillageCostViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val costsRepo: FieldOperationCostsRepo,
    private val currentPracticeRepo: CurrentPracticeRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(
        val userId: Int = 0,
        val farmSize: Double = 1.0,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val currencyCode: String = "",
        val manualPloughCost: Double? = null,
        val manualRidgeCost: Double? = null,
        val performPloughing: Boolean = true,
        val performRidging: Boolean = true,
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
        val userId = user.id ?: return@launch
        val cost = costsRepo.getCostForUser(userId)
        val practice = currentPracticeRepo.getPracticeForUser(userId)
        _uiState.update {
            it.copy(
                userId = userId,
                farmSize = user.farmSize,
                enumAreaUnit = user.enumAreaUnit,
                currencyCode = user.enumCountry.currencyCode,
                manualPloughCost = cost?.manualPloughCost,
                manualRidgeCost = cost?.manualRidgeCost,
                performPloughing = practice?.performPloughing ?: true,
                performRidging = practice?.performRidging ?: true
            )
        }
    }

    fun saveCosts(
        ridingCost: Double?,
        ploughingCost: Double?,
        performPloughing: Boolean,
        performRidging: Boolean
    ) {
        val state = _uiState.value
        val userId = state.userId.takeIf { it != 0 } ?: return
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }

        viewModelScope.launch {
            try {
                val newCosts = FieldOperationCost(
                    userId = userId,
                    manualRidgeCost = ridingCost ?: 0.0,
                    manualPloughCost = ploughingCost ?: 0.0
                )
                val existing = costsRepo.getCostForUser(userId)
                val merged = existing?.copy(
                    manualRidgeCost = newCosts.manualRidgeCost,
                    manualPloughCost = newCosts.manualPloughCost
                ) ?: newCosts
                costsRepo.saveCost(merged)

                val existingPractice = currentPracticeRepo.getPracticeForUser(userId)
                val updatedPractice = existingPractice?.copy(
                    performPloughing = performPloughing,
                    performRidging = performRidging
                ) ?: CurrentPractice(
                    userId = userId,
                    performPloughing = performPloughing,
                    performRidging = performRidging
                )
                currentPracticeRepo.savePractice(updatedPractice)

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
