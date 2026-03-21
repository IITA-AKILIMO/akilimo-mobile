package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TractorAccessViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val costsRepo: FieldOperationCostsRepo
) : ViewModel() {

    data class UiState(
        val userId: Int = 0,
        val farmSize: Double = 1.0,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val tractorAvailable: Boolean = false,
        val tractorPloughCost: Double = 0.0,
        val tractorRidgeCost: Double = 0.0,
        val tractorHarrowCost: Double = 0.0,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val userId = user.id ?: return@launch
        val cost = costsRepo.getCostForUser(userId)

        // Pre-fill tractorAvailable from onboarding if not yet set in BPP
        val tractorFromOnboarding = user.tillageOperations.any {
            it.method.valueOption == EnumOperationMethod.TRACTOR
        }
        val tractorAvailable = cost?.tractorAvailable ?: tractorFromOnboarding

        _uiState.update {
            it.copy(
                userId = userId,
                farmSize = user.farmSize,
                enumAreaUnit = user.enumAreaUnit,
                tractorAvailable = tractorAvailable,
                tractorPloughCost = cost?.tractorPloughCost ?: 0.0,
                tractorRidgeCost = cost?.tractorRidgeCost ?: 0.0,
                tractorHarrowCost = cost?.tractorHarrowCost ?: 0.0
            )
        }
    }

    fun saveCosts(
        tractorAvailable: Boolean,
        ridingCost: Double?,
        ploughingCost: Double?,
        harrowingCost: Double?
    ) = viewModelScope.launch {
        val userId = _uiState.value.userId.takeIf { it != 0 } ?: return@launch
        val newCosts = FieldOperationCost(
            userId = userId,
            tractorAvailable = tractorAvailable,
            tractorRidgeCost = if (tractorAvailable) (ridingCost ?: 0.0) else 0.0,
            tractorPloughCost = if (tractorAvailable) (ploughingCost ?: 0.0) else 0.0,
            tractorHarrowCost = if (tractorAvailable) (harrowingCost ?: 0.0) else 0.0
        )
        val existing = costsRepo.getCostForUser(userId)
        val merged = existing?.copy(
            tractorAvailable = newCosts.tractorAvailable,
            tractorRidgeCost = if (ridingCost != null) newCosts.tractorRidgeCost else existing.tractorRidgeCost,
            tractorPloughCost = if (ploughingCost != null) newCosts.tractorPloughCost else existing.tractorPloughCost,
            tractorHarrowCost = if (harrowingCost != null) newCosts.tractorHarrowCost else existing.tractorHarrowCost
        ) ?: newCosts
        costsRepo.saveCost(merged)
        _uiState.update { it.copy(saved = true) }
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false) }

}
