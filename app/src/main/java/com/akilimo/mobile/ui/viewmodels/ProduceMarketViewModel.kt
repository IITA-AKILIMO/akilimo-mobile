package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.dao.ProduceMarketRepo
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.repos.AkilimoUserRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProduceMarketViewModel(
    private val userRepo: AkilimoUserRepo,
    private val marketRepo: ProduceMarketRepo,
    private val marketType: EnumMarketType
) : ViewModel() {

    data class UiState(
        val userId: Int = 0,
        val currencyCode: String = "",
        val lastEntry: ProduceMarket? = null,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun loadData(userName: String) = viewModelScope.launch {
        val user = userRepo.getUser(userName) ?: return@launch
        val userId = user.id ?: return@launch
        val lastEntry = marketRepo.getLastEntryForUser(userId, marketType)
        _uiState.update {
            it.copy(
                userId = userId,
                currencyCode = user.enumCountry.currencyCode,
                lastEntry = lastEntry
            )
        }
    }

    fun saveMarketEntry(entry: ProduceMarket) = viewModelScope.launch {
        marketRepo.saveMarketEntry(entry)
        _uiState.update { it.copy(saved = true) }
    }

    fun onSaveHandled() = _uiState.update { it.copy(saved = false) }

    companion object {
        fun factory(db: AppDatabase, marketType: EnumMarketType) =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    @Suppress("UNCHECKED_CAST")
                    return ProduceMarketViewModel(
                        AkilimoUserRepo(db.akilimoUserDao()),
                        ProduceMarketRepo(db.produceMarketDao()),
                        marketType
                    ) as T
                }
            }
    }
}
