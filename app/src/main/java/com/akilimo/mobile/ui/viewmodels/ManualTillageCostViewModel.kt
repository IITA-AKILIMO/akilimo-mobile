package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
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
class ManualTillageCostViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val costsRepo: FieldOperationCostsRepo,
    private val currentPracticeRepo: CurrentPracticeRepo
) : ViewModel() {

    data class UiState(
        val userId: Int = 0,
        val farmSize: Double = 1.0,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val manualPloughCost: Double? = null,
        val manualRidgeCost: Double? = null,
        val performPloughing: Boolean = true,
        val performRidging: Boolean = true,
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
                manualPloughCost = cost?.manualPloughCost,
                manualRidgeCost = cost?.manualRidgeCost,
                performPloughing = practice?.performPloughing ?: true,
                performRidging = practice?.performRidging ?: true
            )
        }
    }

    fun saveCosts(ridingCost: Double?, ploughingCost: Double?) = viewModelScope.launch {
        val userId = _uiState.value.userId.takeIf { it != 0 } ?: return@launch
        val newCosts = FieldOperationCost(
            userId = userId,
            manualRidgeCost = ridingCost ?: 0.0,
            manualPloughCost = ploughingCost ?: 0.0
        )
        val existing = costsRepo.getCostForUser(userId)
        val merged = existing?.copy(
            manualRidgeCost = if (ridingCost != null) newCosts.manualRidgeCost else existing.manualRidgeCost,
            manualPloughCost = if (ploughingCost != null) newCosts.manualPloughCost else existing.manualPloughCost,
        ) ?: newCosts
        costsRepo.saveCost(merged)
        _uiState.update { it.copy(saved = true) }
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false) }

}
