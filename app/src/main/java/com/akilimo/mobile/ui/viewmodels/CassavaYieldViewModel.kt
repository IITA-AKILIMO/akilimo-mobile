package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.entities.SelectedCassavaMarket
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CassavaYieldViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val yieldRepo: CassavaYieldRepo,
    private val selectedRepo: SelectedCassavaMarketRepo,
    private val appSettings: AppSettingsDataStore
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val yields: List<CassavaYield> = emptyList(),
        /** The option highlighted on screen; starts at the saved yield, changed by taps. */
        val selectedYieldId: Int? = null,
        val isGridLayout: Boolean = true,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val saved: Boolean = false,
        /** Non-null when DB is empty and the screen must supply a seed list. */
        val seedRequest: EnumAreaUnit? = null,
        val areaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val useCase: EnumAdvice = EnumAdvice.FERTILIZER_RECOMMENDATIONS
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** True once the user tapped an option, so a later DB emission does not override it. */
    private var userPicked = false

    init {
        viewModelScope.launch { loadData(appSettings.akilimoUser) }
    }

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return@launch
        }
        val userId = user.id ?: 0
        val areaUnit = EnumAreaUnit.entries.firstOrNull { it == user.enumAreaUnit } ?: EnumAreaUnit.ACRE
        val useCase = user.activeAdvise ?: EnumAdvice.FERTILIZER_RECOMMENDATIONS

        _uiState.update { it.copy(areaUnit = areaUnit, useCase = useCase) }

        combine(
            yieldRepo.observeAll(),
            selectedRepo.observeSelected(userId)
        ) { repoList, details ->
            Pair(repoList, details?.selectedCassavaMarket?.yieldId)
        }.collectLatest { (repoList, selectedYieldId) ->
            if (repoList.isEmpty()) {
                // Signal the screen to produce the seed list (requires getString context)
                _uiState.update { it.copy(seedRequest = areaUnit) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        yields = repoList,
                        selectedYieldId = if (userPicked) it.selectedYieldId else selectedYieldId,
                        seedRequest = null
                    )
                }
            }
        }
    }

    /** Called by the screen after it builds the seed list using string resources. */
    fun seedYields(seeds: List<CassavaYield>) = viewModelScope.launch {
        yieldRepo.saveAll(seeds)
        // observeAll() will re-emit automatically once saved
    }

    /** Highlights an option; nothing is written until [saveSelection]. */
    fun selectYield(cassavaYield: CassavaYield) {
        userPicked = true
        _uiState.update { it.copy(selectedYieldId = cassavaYield.id) }
    }

    fun toggleLayout() = _uiState.update { it.copy(isGridLayout = !it.isGridLayout) }

    /** Persists the highlighted yield; the screen pops once [UiState.saved] is set. */
    fun saveSelection() {
        val state = _uiState.value
        val yieldId = state.selectedYieldId ?: return
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                val user = userRepo.getUser(appSettings.akilimoUser) ?: error("No profile for current user")
                val userId = user.id ?: 0
                val current = selectedRepo.getSelectedByUser(userId)?.selectedCassavaMarket
                val updated = current?.copy(yieldId = yieldId)
                    ?: SelectedCassavaMarket(userId = userId, yieldId = yieldId)
                selectedRepo.select(updated)
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
