package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.utils.AmountInput
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
class WeedControlCostsViewModel @Inject constructor(
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
        val firstWeedingCost: Double = 0.0,
        val secondWeedingCost: Double = 0.0,
        val weedControlMethod: EnumWeedControlMethod? = null,
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
                firstWeedingCost = cost?.firstWeedingOperationCost ?: 0.0,
                secondWeedingCost = cost?.secondWeedingOperationCost ?: 0.0,
                weedControlMethod = practice?.weedControlMethod ?: user.weedControlMethod
            )
        }
    }

    fun saveCosts(firstCost: Double?, secondCost: Double?, method: EnumWeedControlMethod?) {
        val state = _uiState.value
        val userId = state.userId.takeIf { it != 0 } ?: return
        // A method is required (it is what the recommendation uses); ignore double taps
        if (method == null || state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }

        viewModelScope.launch {
            try {
                val newCosts = FieldOperationCost(
                    userId = userId,
                    firstWeedingOperationCost = firstCost ?: 0.0,
                    secondWeedingOperationCost = secondCost ?: 0.0
                )
                val existing = costsRepo.getCostForUser(userId)
                val merged = existing?.copy(
                    firstWeedingOperationCost = if (firstCost != null)
                        newCosts.firstWeedingOperationCost else existing.firstWeedingOperationCost,
                    secondWeedingOperationCost = if (secondCost != null)
                        newCosts.secondWeedingOperationCost else existing.secondWeedingOperationCost,
                ) ?: newCosts
                costsRepo.saveCost(merged)

                val practice = currentPracticeRepo.getPracticeForUser(userId)
                val updatedPractice = practice?.copy(weedControlMethod = method)
                    ?: CurrentPractice(userId = userId, weedControlMethod = method)
                currentPracticeRepo.savePractice(updatedPractice)

                _uiState.update { it.copy(weedControlMethod = method, saved = true) }
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

    companion object {
        /**
         * Save rule: the weeding method is required because it drives the advice; both
         * costs stay optional (blank clears them) but anything typed must be a valid amount.
         */
        fun canSave(method: EnumWeedControlMethod?, firstCost: String, secondCost: String): Boolean =
            method != null &&
                !AmountInput.isInvalid(firstCost, requirePositive = false) &&
                !AmountInput.isInvalid(secondCost, requirePositive = false)
    }
}
