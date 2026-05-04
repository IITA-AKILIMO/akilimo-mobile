package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
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
    private val costsRepo: FieldOperationCostsRepo,
    private val appSettings: AppSettingsDataStore
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

    init {
        viewModelScope.launch { loadData(appSettings.akilimoUser) }
    }

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
        val existing = costsRepo.getCostForUser(userId)
        val merged = (existing ?: FieldOperationCost(userId = userId)).copy(
            tractorAvailable = tractorAvailable,
            tractorRidgeCost = if (tractorAvailable) (ridingCost ?: existing?.tractorRidgeCost ?: 0.0) else 0.0,
            tractorPloughCost = if (tractorAvailable) (ploughingCost ?: existing?.tractorPloughCost ?: 0.0) else 0.0,
            tractorHarrowCost = if (tractorAvailable) (harrowingCost ?: existing?.tractorHarrowCost ?: 0.0) else 0.0
        )
        costsRepo.saveCost(merged)
        _uiState.update { it.copy(saved = true) }
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false) }

}
