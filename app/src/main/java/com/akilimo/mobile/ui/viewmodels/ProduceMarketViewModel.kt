package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.repos.ProduceMarketRepo
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.entities.ProduceMarket
import com.akilimo.mobile.enums.EnumMarketType
import com.akilimo.mobile.entities.MaizeUnit
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.MaizeUnitRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ProduceMarketViewModel.Factory::class)
class ProduceMarketViewModel @AssistedInject constructor(
    private val userRepo: AkilimoUserRepo,
    private val marketRepo: ProduceMarketRepo,
    private val maizeUnitRepo: MaizeUnitRepo,
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
        val maizeUnits: List<MaizeUnit> = emptyList(),
        val isSaving: Boolean = false,
        val saveFailed: Boolean = false,
        val saved: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadData(appSettings.akilimoUser) }
        if (marketType == EnumMarketType.MAIZE_MARKET) {
            viewModelScope.launch {
                maizeUnitRepo.observeActive().collect { units -> _uiState.update { it.copy(maizeUnits = units) } }
            }
        }
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

    fun saveMarketEntry(entry: ProduceMarket) {
        if (_uiState.value.isSaving) return
        _uiState.update { it.copy(isSaving = true, saveFailed = false) }
        viewModelScope.launch {
            try {
                marketRepo.saveMarketEntry(entry)
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
