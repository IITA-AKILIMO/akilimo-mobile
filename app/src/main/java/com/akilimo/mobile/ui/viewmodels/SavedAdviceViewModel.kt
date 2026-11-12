package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The saved-advice list: what is on the phone, brought up to date from the account when there is a connection. */
@HiltViewModel
class SavedAdviceViewModel @Inject constructor(
    private val repo: SavedAdviceRepo,
) : ViewModel() {

    data class UiState(
        val refreshing: Boolean = false,
        @param:StringRes val messageRes: Int? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Null until the first read of the phone's copy, so the screen can tell "loading" from "empty". */
    val items: StateFlow<List<SavedAdvice>?> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

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

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
