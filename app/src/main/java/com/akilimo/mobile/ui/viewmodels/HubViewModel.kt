package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.workers.DataRefresher
import com.akilimo.mobile.workers.SyncStatus
import com.akilimo.mobile.workers.SyncStatusSource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The hub's status strip: connectivity, sync in progress, and the last result. */
@HiltViewModel
class HubViewModel @Inject constructor(
    syncStatusSource: SyncStatusSource,
    private val dataRefresher: DataRefresher,
) : ViewModel() {

    data class UiState(
        val syncStatus: SyncStatus? = null,
        val isRetrying: Boolean = false,
    ) {
        /** A retry counts as syncing before WorkManager reports the worker as RUNNING. */
        val displayStatus: SyncStatus?
            get() = syncStatus?.let { it.copy(isSyncing = it.isSyncing || isRetrying) }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            syncStatusSource.status.collect { status ->
                _uiState.update { it.copy(syncStatus = status) }
            }
        }
    }

    fun retrySyncs() {
        if (_uiState.value.isRetrying) return
        viewModelScope.launch {
            _uiState.update { it.copy(isRetrying = true) }
            try {
                dataRefresher.all()
            } finally {
                _uiState.update { it.copy(isRetrying = false) }
            }
        }
    }
}
