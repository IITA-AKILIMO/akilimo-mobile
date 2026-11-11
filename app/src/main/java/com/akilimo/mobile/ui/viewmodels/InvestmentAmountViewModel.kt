package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.InvestmentAmount
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.utils.MathHelper
import com.akilimo.mobile.workers.DataRefresher
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class InvestmentAmountViewModel @Inject constructor(
    private val userRepo: AkilimoUserRepo,
    private val investmentRepo: InvestmentRepo,
    private val selectedInvestmentRepo: SelectedInvestmentRepo,
    private val appSettings: AppSettingsDataStore,
    private val dataRefresher: DataRefresher
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val investments: List<InvestmentAmount> = emptyList(),
        val selectedInvestment: SelectedInvestment? = null,
        /** The option highlighted on screen; starts at the saved choice, changed by taps. */
        val selectedId: Int? = null,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val farmSize: Double = 0.0,
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val saved: Boolean = false
    ) {
        val selectedItem: InvestmentAmount? get() = investments.firstOrNull { it.id == selectedId }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** True once the user tapped an option, so a late DB emission does not override it. */
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
        val country = user.enumCountry
        val areaUnit = EnumAreaUnit.entries.firstOrNull { it == user.enumAreaUnit } ?: EnumAreaUnit.ACRE
        val farmSize = user.farmSize

        _uiState.update { it.copy(enumAreaUnit = areaUnit, farmSize = farmSize) }

        launch {
            investmentRepo.observeAllByCountry(country).collectLatest { list ->
                val computed = list.map { item ->
                    val base = MathHelper.convertFromAcres(farmSize, areaUnit)
                    item.copy(investmentAmount = base * item.investmentAmount)
                }
                _uiState.update { it.copy(investments = computed, isLoading = false) }
            }
        }

        launch {
            selectedInvestmentRepo.observeSelected(userId).collectLatest { selected ->
                _uiState.update {
                    it.copy(
                        selectedInvestment = selected,
                        selectedId = if (userPicked) it.selectedId else selected?.investmentId
                    )
                }
            }
        }
    }

    fun select(item: InvestmentAmount) {
        userPicked = true
        _uiState.update { it.copy(selectedId = item.id) }
    }

    /** Saves the highlighted option; the screen pops once [UiState.saved] is set. */
    fun saveSelected() {
        val state = _uiState.value
        val item = state.selectedItem ?: return
        if (state.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                persist(item, item.investmentAmount)
                _uiState.update { it.copy(saved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Sentry.captureException(e)
                _uiState.update { it.copy(isSaving = false, saveFailed = true) }
            }
        }
    }

    private suspend fun persist(item: InvestmentAmount, selectedAmount: Double) {
        val user = userRepo.getUser(appSettings.akilimoUser) ?: error("No profile for current user")
        val userId = user.id ?: error("Profile has no id")
        val investment = selectedInvestmentRepo.getSelectedSync(userId)?.copy(
            investmentId = item.id,
            chosenAmount = selectedAmount,
            isExactAmount = item.exactAmount
        ) ?: SelectedInvestment(
            userId = userId,
            investmentId = item.id,
            chosenAmount = selectedAmount,
            isExactAmount = item.exactAmount
        )
        selectedInvestmentRepo.saveOrUpdate(investment)
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false, isSaving = false) }

    /** Pull to refresh: fetch the investment amounts again. */
    fun refresh() {
        if (_uiState.value.isRefreshing) return
        launchRefresh({ r -> _uiState.update { it.copy(isRefreshing = r) } }) { dataRefresher.investmentAmounts() }
    }

    fun onSaveErrorShown() = _uiState.update { it.copy(saveFailed = false) }
}
