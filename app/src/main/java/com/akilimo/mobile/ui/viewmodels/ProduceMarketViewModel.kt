package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.dao.ProduceMarketRepo
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.repos.AkilimoUserRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProduceMarketViewModel.Factory::class)
class ProduceMarketViewModel @AssistedInject constructor(
    private val userRepo: AkilimoUserRepo,
    private val marketRepo: ProduceMarketRepo,
    private val appSettings: AppSettingsDataStore,
    @Assisted private val marketType: EnumMarketType
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(marketType: EnumMarketType): ProduceMarketViewModel
    }

    data class UiState(
        val userId: Int = 0,
        val currencyCode: String = "",
        val lastEntry: ProduceMarket? = null,
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
}
