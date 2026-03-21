package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.CurrentPractice
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeedControlCostsViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val costsRepo: FieldOperationCostsRepo,
    private val currentPracticeRepo: CurrentPracticeRepo
) : ViewModel() {

    data class UiState(
        val userId: Int = 0,
        val farmSize: Double = 1.0,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val firstWeedingCost: Double = 0.0,
        val secondWeedingCost: Double = 0.0,
        val weedControlMethod: EnumWeedControlMethod? = null,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

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
                firstWeedingCost = cost?.firstWeedingOperationCost ?: 0.0,
                secondWeedingCost = cost?.secondWeedingOperationCost ?: 0.0,
                weedControlMethod = practice?.weedControlMethod
            )
        }
    }

    fun saveCosts(firstCost: Double?, secondCost: Double?, method: EnumWeedControlMethod?) = viewModelScope.launch {
        val userId = _uiState.value.userId.takeIf { it != 0 } ?: return@launch
        val newCosts = FieldOperationCost(
            userId = userId,
            firstWeedingOperationCost = firstCost ?: 0.0,
            secondWeedingOperationCost = secondCost ?: 0.0
        )
        val existing = costsRepo.getCostForUser(userId)
        val merged = existing?.copy(
            firstWeedingOperationCost = if (firstCost != null) newCosts.firstWeedingOperationCost else existing.firstWeedingOperationCost,
            secondWeedingOperationCost = if (secondCost != null) newCosts.secondWeedingOperationCost else existing.secondWeedingOperationCost,
        ) ?: newCosts
        costsRepo.saveCost(merged)

        if (method != null) {
            val practice = currentPracticeRepo.getPracticeForUser(userId)
            val updatedPractice = practice?.copy(weedControlMethod = method)
                ?: CurrentPractice(userId = userId, weedControlMethod = method)
            currentPracticeRepo.savePractice(updatedPractice)
            _uiState.update { it.copy(weedControlMethod = method) }
        }

        _uiState.update { it.copy(saved = true) }
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false) }

}
