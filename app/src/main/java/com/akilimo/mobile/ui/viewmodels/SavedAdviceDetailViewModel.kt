package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.navigation.SavedAdviceDetailRoute
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

/** One saved advice: its text (fetched once if the phone does not have it), and deleting it. */
@HiltViewModel
class SavedAdviceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SavedAdviceRepo,
) : ViewModel() {

    private val requestId: String = savedStateHandle.toRoute<SavedAdviceDetailRoute>().requestId

    data class UiState(
        val loadingText: Boolean = false,
        val deleting: Boolean = false,
        /** Set once the item is gone (deleted here, or no longer on the server); the screen goes back. */
        val gone: Boolean = false,
        @param:StringRes val messageRes: Int? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val advice: StateFlow<SavedAdvice?> = repo.observe(requestId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    init {
        loadText()
    }

    fun loadText() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadingText = true, messageRes = null) }
            val result = repo.loadText(requestId)
            _uiState.update {
                it.copy(
                    loadingText = false,
                    messageRes = (result as? AccountResult.Failure)?.let { failure -> failure.error.messageRes() },
                )
            }
        }
    }

    fun delete() {
        if (_uiState.value.deleting) return
        viewModelScope.launch {
            _uiState.update { it.copy(deleting = true, messageRes = null) }
            when (val result = repo.delete(requestId)) {
                is AccountResult.Failure ->
                    _uiState.update { it.copy(deleting = false, messageRes = result.error.messageRes()) }
                is AccountResult.Success -> _uiState.update { it.copy(deleting = false, gone = true) }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
