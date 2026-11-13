package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.account.AccountPromptState
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.data.account.PromptSnooze
import com.akilimo.mobile.repos.account.AccountRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The entry points to the optional account: the hub card, the Settings rows (account, sign out) and the prompt offered
 * once after the app starts.
 */
@HiltViewModel
class AccountEntryViewModel @Inject constructor(
    sessionStore: AccountSessionStore,
    private val promptState: AccountPromptState,
    private val accounts: AccountRepo,
) : ViewModel() {

    /** The signed-in farmer's name or email; null when nobody is signed in. */
    val signedInAs: StateFlow<String?> = sessionStore.session
        .map { it?.displayName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), sessionStore.session.value?.displayName)

    private val _showPrompt = MutableStateFlow(promptState.consumeIfDue(signedIn = sessionStore.isSignedIn))

    /** True while the start-up offer to sign in or create an account is showing. */
    val showPrompt: StateFlow<Boolean> = _showPrompt.asStateFlow()

    /** Closes the prompt; with [snooze] it also stays away for that long (across restarts). Without it, only this launch. */
    fun dismissPrompt(snooze: PromptSnooze? = null) {
        snooze?.let(promptState::snooze)
        _showPrompt.value = false
    }

    /** Signs out (the phone forgets the session and the saved copies). */
    fun signOut() {
        viewModelScope.launch { accounts.signOut() }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
