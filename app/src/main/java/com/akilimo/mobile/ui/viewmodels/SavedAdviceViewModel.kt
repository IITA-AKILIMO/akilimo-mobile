package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.repos.account.AdviceFilter
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The saved-advice list. It is built for a long history: the list is read from the phone a screenful at a time and grows as
 * the farmer scrolls, can be narrowed by use case and country, and is brought up to date from the account in the background.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SavedAdviceViewModel @Inject constructor(
    private val repo: SavedAdviceRepo,
) : ViewModel() {

    data class UiState(
        val refreshing: Boolean = false,
        val deleting: Boolean = false,
        @param:StringRes val messageRes: Int? = null,
        val filter: AdviceFilter = AdviceFilter(),
        /** How many items the list shows now; it grows by [PAGE_SIZE] each time the farmer reaches the end. */
        val limit: Int = PAGE_SIZE,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val view = _uiState.map { it.filter to it.limit }.distinctUntilChanged()

    /** Null until the first read of the phone's copy, so the screen can tell "loading" from "empty". */
    val items: StateFlow<List<SavedAdvice>?> = view
        .flatMapLatest { (filter, limit) -> repo.observePage(filter, limit) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    /** All items that match the filter, not only the ones on screen. */
    val count: StateFlow<Int> = _uiState.map { it.filter }.distinctUntilChanged()
        .flatMapLatest { repo.observeCount(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    val useCases: StateFlow<List<String>> = repo.observeUseCases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val countries: StateFlow<List<String>> = repo.observeCountries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    init {
        refresh()
    }

    fun refresh() {
        if (_uiState.value.refreshing) return
        viewModelScope.launch {
            _uiState.update { it.copy(refreshing = true, messageRes = null) }
            val failed = repo.refresh() is AccountResult.Failure
            // Offline is normal in the field: say so quietly, the phone's copy is still shown
            _uiState.update { it.copy(refreshing = false, messageRes = if (failed) R.string.saved_advice_refresh_failed else null) }
        }
    }

    /** The farmer reached the end of what is shown: show another [PAGE_SIZE], if there is more. */
    fun loadMore() {
        val state = _uiState.value
        val shown = items.value?.size ?: return
        if (shown >= state.limit && state.limit < count.value) {
            _uiState.update { it.copy(limit = it.limit + PAGE_SIZE) }
        }
    }

    fun setUseCase(useCase: String?) = _uiState.update { it.copy(filter = it.filter.copy(useCase = useCase), limit = PAGE_SIZE) }

    fun setCountry(country: String?) = _uiState.update { it.copy(filter = it.filter.copy(country = country), limit = PAGE_SIZE) }

    fun deleteAll() {
        if (_uiState.value.deleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(deleting = true, messageRes = null) }
            when (val result = repo.deleteAll()) {
                is AccountResult.Failure ->
                    _uiState.update { it.copy(deleting = false, messageRes = result.error.messageRes()) }
                is AccountResult.Success ->
                    _uiState.update { it.copy(deleting = false, filter = AdviceFilter(), limit = PAGE_SIZE) }
            }
        }
    }

    companion object {
        const val PAGE_SIZE = 50
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
