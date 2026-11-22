package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.entities.InboxMessage
import com.akilimo.mobile.notifications.MessageSync
import com.akilimo.mobile.repos.InboxRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** The farmer's message history, shared by the Hub card (unread badge) and the Inbox screen. */
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repo: InboxRepo,
    private val messageSync: MessageSync,
) : ViewModel() {

    data class UiState(
        /** Null until the first read from the phone, so the screen can show a skeleton instead of "empty". */
        val messages: List<InboxMessage>? = null,
        val unreadCount: Int = 0,
        /** Message whose full text is open. */
        val expandedId: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.messages().collect { list -> _uiState.update { it.copy(messages = list) } }
        }
        viewModelScope.launch {
            repo.unreadCount().collect { count -> _uiState.update { it.copy(unreadCount = count) } }
        }
        // Pick up what push did not deliver; failures are quiet because the history on the phone is still shown
        refresh()
    }

    /** Fetches messages the phone missed (offline, push off, new install) from the API. */
    fun refresh() {
        viewModelScope.launch { messageSync.sync() }
    }

    /** Opens a message (and marks it read) or closes it if it is already open. */
    fun toggle(id: String) {
        val opening = _uiState.value.expandedId != id
        _uiState.update { it.copy(expandedId = if (opening) id else null) }
        if (opening) viewModelScope.launch { repo.markRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { repo.markAllRead() }
    }

    fun delete(id: String) {
        _uiState.update { if (it.expandedId == id) it.copy(expandedId = null) else it }
        viewModelScope.launch { repo.delete(id) }
    }

    fun deleteAll() {
        _uiState.update { it.copy(expandedId = null) }
        viewModelScope.launch { repo.deleteAll() }
    }
}
