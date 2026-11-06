package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.FieldOperationCost
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.repos.AkilimoUserRepo
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
class TractorAccessViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val costsRepo: FieldOperationCostsRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(
        val userId: Int = 0,
        val farmSize: Double = 1.0,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val currencyCode: String = "",
        val tractorAvailable: Boolean = false,
        val tractorPloughCost: Double = 0.0,
        val tractorRidgeCost: Double = 0.0,
        val tractorHarrowCost: Double = 0.0,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val saved: Boolean = false
    )

    /** Why Save is disabled, or [NONE] when the form can be saved. */
    enum class SaveBlocker { NONE, NO_IMPLEMENT, MISSING_COST }

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
                currencyCode = user.enumCountry.currencyCode,
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
    ) {
        val state = _uiState.value
        val userId = state.userId.takeIf { it != 0 } ?: return
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }

        viewModelScope.launch {
            try {
                val existing = costsRepo.getCostForUser(userId)
                val merged = (existing ?: FieldOperationCost(userId = userId)).copy(
                    tractorAvailable = tractorAvailable,
                    tractorRidgeCost = if (tractorAvailable) (ridingCost ?: existing?.tractorRidgeCost ?: 0.0) else 0.0,
                    tractorPloughCost = if (tractorAvailable) (ploughingCost ?: existing?.tractorPloughCost ?: 0.0) else 0.0,
                    tractorHarrowCost = if (tractorAvailable) (harrowingCost ?: existing?.tractorHarrowCost ?: 0.0) else 0.0
                )
                costsRepo.saveCost(merged)
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

    companion object {
        /**
         * "No tractor" can always be saved. "Yes" needs at least one implement, and each
         * selected implement needs a positive cost (0 would read back as "not used").
         */
        fun saveBlocker(
            tractorAvailable: Boolean,
            implements: List<Pair<Boolean, String>>,
        ): SaveBlocker {
            if (!tractorAvailable) return SaveBlocker.NONE
            val selected = implements.filter { it.first }
            if (selected.isEmpty()) return SaveBlocker.NO_IMPLEMENT
            val allCosted = selected.all { (_, text) -> (AmountInput.parse(text) ?: 0.0) > 0.0 }
            return if (allCosted) SaveBlocker.NONE else SaveBlocker.MISSING_COST
        }
    }
}
