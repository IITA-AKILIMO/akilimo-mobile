package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.data.account.AccountSessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Whether a farmer is signed in, for the hub and settings entries that lead to the account or the saved advice. */
@HiltViewModel
class AccountEntryViewModel @Inject constructor(
    sessionStore: AccountSessionStore,
) : ViewModel() {

    /** The signed-in farmer's name or email; null when nobody is signed in. */
    val signedInAs: StateFlow<String?> = sessionStore.session
        .map { it?.displayName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), sessionStore.session.value?.displayName)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
