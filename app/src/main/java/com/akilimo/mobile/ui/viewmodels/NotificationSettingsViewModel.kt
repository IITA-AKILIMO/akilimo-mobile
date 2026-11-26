package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.notifications.PushDeviceSync
import com.akilimo.mobile.notifications.PushTopicSync
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The push notifications switch and the one-time "get updates?" prompt, shared by Hub and Settings.
 * Notifications are opt-in: off until the farmer allows them (and, on Android 13+, grants the
 * permission, which the screen asks for and reports back through [onPermissionResult]).
 */
@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val appSettings: AppSettingsDataStore,
    private val topicSync: PushTopicSync,
    private val deviceSync: PushDeviceSync,
) : ViewModel() {

    data class UiState(
        val enabled: Boolean = false,
        /** The start-up prompt has been answered, either way; it is never shown again. */
        val promptAnswered: Boolean = true,
        /** Android currently lets the app post (permission granted on 13+, not turned off in settings). */
        val systemAllowed: Boolean = true,
        /** The last permission request was refused, so asking again would show nothing. */
        val permissionDenied: Boolean = false,
    ) {
        /** Offer a way to the phone's notification settings whenever Android blocks the app and the farmer wants updates. */
        val showAllowInSettings: Boolean get() = !systemAllowed && (enabled || permissionDenied)

        /** Offer the prompt only once, and only while notifications are off. */
        val showPrompt: Boolean get() = !promptAnswered && !enabled
    }

    sealed interface Effect {
        /** The screen should ask for POST_NOTIFICATIONS (when not yet granted) and report the result. */
        data object RequestPermission : Effect
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _effect = Channel<Effect>(Channel.BUFFERED)
    val effect: Flow<Effect> = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            appSettings.pushEnabledFlow.collect { value -> _uiState.update { it.copy(enabled = value) } }
        }
        viewModelScope.launch {
            appSettings.pushPromptAnsweredFlow.collect { value -> _uiState.update { it.copy(promptAnswered = value) } }
        }
    }

    /** Settings switch. Turning on asks the screen for the permission first; off applies at once. */
    fun setEnabled(enable: Boolean) {
        if (enable) {
            _effect.trySend(Effect.RequestPermission)
        } else {
            viewModelScope.launch { apply(false) }
        }
    }

    /** Start-up prompt: "Allow". */
    fun onPromptAccepted() {
        viewModelScope.launch { appSettings.setPushPromptAnswered(true) }
        _effect.trySend(Effect.RequestPermission)
    }

    /** Start-up prompt: "Not now". */
    fun onPromptDismissed() {
        viewModelScope.launch { appSettings.setPushPromptAnswered(true) }
    }

    /** Result of the permission request (always true below Android 13, where none is needed). */
    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(permissionDenied = !granted) }
        viewModelScope.launch { apply(granted) }
    }

    /** Reported by the screen each time it resumes, e.g. after the farmer came back from phone settings. */
    fun onSystemAllowedChanged(allowed: Boolean) {
        _uiState.update { it.copy(systemAllowed = allowed, permissionDenied = it.permissionDenied && !allowed) }
    }

    private suspend fun apply(enable: Boolean) {
        appSettings.setPushEnabled(enable)
        topicSync.sync()
        // Registers the phone for messages to one person when on, removes it from the API when off
        deviceSync.sync()
    }
}
