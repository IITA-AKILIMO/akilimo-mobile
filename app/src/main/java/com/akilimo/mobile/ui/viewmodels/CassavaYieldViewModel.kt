package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CassavaYieldViewModel(
    private val userRepo: AkilimoUserRepo,
    private val yieldRepo: CassavaYieldRepo,
    private val selectedRepo: SelectedCassavaMarketRepo
) : ViewModel() {

    data class UiState(
        val yields: List<CassavaYield> = emptyList(),
        /** Non-null when DB is empty and the Activity must supply a seed list. */
        val seedRequest: EnumAreaUnit? = null,
        val areaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val useCase: EnumAdvice = EnumAdvice.FERTILIZER_RECOMMENDATIONS
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val userId = user.id ?: 0
        val areaUnit = EnumAreaUnit.entries.firstOrNull { it == user.enumAreaUnit } ?: EnumAreaUnit.ACRE
        val useCase = user.activeAdvise ?: EnumAdvice.FERTILIZER_RECOMMENDATIONS

        val yieldId = selectedRepo.getSelectedByUser(userId)?.selectedCassavaMarket?.yieldId

        _uiState.update { it.copy(areaUnit = areaUnit, useCase = useCase) }

        yieldRepo.observeAll().collectLatest { repoList ->
            if (repoList.isEmpty()) {
                // Signal the Activity to produce the seed list (requires getString context)
                _uiState.update { it.copy(seedRequest = areaUnit) }
            } else {
                _uiState.update {
                    it.copy(
                        yields = repoList.map { y -> y.apply { isSelected = (y.id == yieldId) } },
                        seedRequest = null
                    )
                }
            }
        }
    }

    /** Called by the Activity after it builds the seed list using string resources. */
    fun seedYields(seeds: List<CassavaYield>) = viewModelScope.launch {
        yieldRepo.saveAll(seeds)
        // observeAll() will re-emit automatically once saved
    }

    fun selectYield(userName: String, cassavaYield: CassavaYield) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val userId = user.id ?: 0
        val current = selectedRepo.getSelectedByUser(userId)?.selectedCassavaMarket
        val updated = current?.copy(yieldId = cassavaYield.id)
            ?: com.akilimo.mobile.entities.SelectedCassavaMarket(
                userId = userId,
                yieldId = cassavaYield.id
            )
        selectedRepo.select(updated)
    }

    companion object {
        fun factory(db: AppDatabase) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CassavaYieldViewModel(
                    AkilimoUserRepo(db.akilimoUserDao()),
                    CassavaYieldRepo(db.cassavaYieldDao()),
                    SelectedCassavaMarketRepo(db.selectedCassavaMarketDao())
                ) as T
            }
        }
    }
}
