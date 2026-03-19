package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.SelectedInvestment
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.utils.MathHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvestmentAmountViewModel(
    private val userRepo: AkilimoUserRepo,
    private val investmentRepo: InvestmentRepo,
    private val selectedInvestmentRepo: SelectedInvestmentRepo
) : ViewModel() {

    data class UiState(
        val investments: List<InvestmentAmount> = emptyList(),
        val selectedInvestment: SelectedInvestment? = null,
        val enumAreaUnit: EnumAreaUnit = EnumAreaUnit.ACRE,
        val farmSize: Double = 0.0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
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
                _uiState.update { it.copy(investments = computed) }
            }
        }

        launch {
            selectedInvestmentRepo.observeSelected(userId).collectLatest { selected ->
                _uiState.update { it.copy(selectedInvestment = selected) }
            }
        }
    }

    fun saveInvestment(userName: String, item: InvestmentAmount, selectedAmount: Double) =
        viewModelScope.launch {
            val user = userRepo.getUser(userName) ?: return@launch
            val userId = user.id ?: return@launch
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

    companion object {
        fun factory(db: AppDatabase) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return InvestmentAmountViewModel(
                    AkilimoUserRepo(db.akilimoUserDao()),
                    InvestmentRepo(db.investmentAmountDao()),
                    SelectedInvestmentRepo(db.selectedInvestmentDao())
                ) as T
            }
        }
    }
}
