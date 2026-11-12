package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.repos.account.AccountError
import com.akilimo.mobile.repos.account.AccountRepo
import com.akilimo.mobile.repos.account.AccountResult
import com.akilimo.mobile.repos.account.SavedAdviceRepo
import com.akilimo.mobile.repos.account.SignInOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The account screen: sign in, create an account, Google, the consent step, and (once signed in) confirming the email,
 * signing out and deleting the account.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accounts: AccountRepo,
    private val savedAdvice: SavedAdviceRepo,
    sessionStore: AccountSessionStore,
) : ViewModel() {

    enum class Mode { SIGN_IN, SIGN_UP }

    /** Which consent step is showing: for an older account that is signed in, or before a first Google sign-in. */
    enum class ConsentStep { NONE, EXISTING_ACCOUNT, GOOGLE_FIRST_USE }

    data class UiState(
        val mode: Mode = Mode.SIGN_IN,
        val email: String = "",
        val password: String = "",
        val name: String = "",
        val consent: Boolean = false,
        val busy: Boolean = false,
        @param:StringRes val emailError: Int? = null,
        @param:StringRes val passwordError: Int? = null,
        @param:StringRes val consentError: Int? = null,
        /** A message for the whole screen: [messageIsError] tells a failure from a note. */
        @param:StringRes val messageRes: Int? = null,
        val messageIsError: Boolean = true,
        val googleConsent: Boolean = false,
        /** Set after a sign-in that is complete (consent given); the screen goes back and calls [onDoneHandled]. */
        val done: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val session: StateFlow<AccountSession?> = sessionStore.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), sessionStore.session.value)

    /** The access token of a Google sign-in that is waiting for the farmer's consent. Kept in memory only. */
    private var pendingGoogleToken: String? = null

    fun consentStep(session: AccountSession?, state: UiState): ConsentStep = when {
        state.googleConsent -> ConsentStep.GOOGLE_FIRST_USE
        session?.consentRequired == true -> ConsentStep.EXISTING_ACCOUNT
        else -> ConsentStep.NONE
    }

    fun setMode(mode: Mode) = _uiState.update {
        it.copy(mode = mode, emailError = null, passwordError = null, consentError = null, messageRes = null)
    }

    fun onEmail(value: String) = _uiState.update { it.copy(email = value, emailError = null, messageRes = null) }
    fun onPassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null, messageRes = null) }
    fun onName(value: String) = _uiState.update { it.copy(name = value) }
    fun onConsent(value: Boolean) = _uiState.update { it.copy(consent = value, consentError = null) }
    fun dismissMessage() = _uiState.update { it.copy(messageRes = null) }
    fun onDoneHandled() = _uiState.update { it.copy(done = false) }

    /** Sign in or create the account, whichever tab is showing. */
    fun submit() {
        val state = _uiState.value
        if (state.busy) return
        val signUp = state.mode == Mode.SIGN_UP

        val emailError = if (AccountInput.isEmail(state.email)) null else R.string.account_err_email
        val passwordError = when {
            state.password.isEmpty() -> R.string.account_err_password_required
            signUp && !AccountInput.isPassword(state.password) -> R.string.account_err_password
            else -> null
        }
        val consentError = if (signUp && !state.consent) R.string.account_err_consent else null
        if (emailError != null || passwordError != null || consentError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, consentError = consentError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            val result = if (signUp) {
                accounts.register(state.email, state.password, state.name, consent = true)
            } else {
                accounts.login(state.email, state.password)
            }
            handleSignIn(result)
        }
    }

    /** A Google access token arrived from the sign-in sheet. */
    fun onGoogleToken(accessToken: String) {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            // A farmer who already ticked "I agree" on the sign-up tab does not have to be asked again
            val result = accounts.signInWithProvider(GOOGLE, accessToken, consent = _uiState.value.consent)
            if (result is AccountResult.Failure && result.error == AccountError.CONSENT_REQUIRED) {
                pendingGoogleToken = accessToken
                _uiState.update { it.copy(busy = false, googleConsent = true) }
            } else {
                handleSignIn(result)
            }
        }
    }

    fun onGoogleFailed() = _uiState.update { it.copy(messageRes = R.string.account_err_google, messageIsError = true) }

    /** The farmer agreed: for an older account, record it; for a first Google sign-in, finish creating the account. */
    fun agree() {
        val state = _uiState.value
        if (state.busy) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            val token = pendingGoogleToken
            if (state.googleConsent && token != null) {
                handleSignIn(accounts.signInWithProvider(GOOGLE, token, consent = true))
            } else {
                when (val result = accounts.acceptConsent()) {
                    is AccountResult.Failure -> fail(result.error)
                    is AccountResult.Success -> {
                        savedAdvice.refresh()
                        _uiState.update { it.copy(busy = false, done = true) }
                    }
                }
            }
        }
    }

    /** "Not now" on the consent step: nothing is created or linked, and the farmer is signed out again. */
    fun declineConsent() {
        pendingGoogleToken = null
        viewModelScope.launch {
            accounts.signOut()
            _uiState.update { it.copy(googleConsent = false, busy = false) }
        }
    }

    fun verifyEmail(code: String, onDone: () -> Unit) {
        if (_uiState.value.busy || code.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            when (val result = accounts.verifyEmail(code)) {
                is AccountResult.Failure -> fail(result.error)
                is AccountResult.Success -> {
                    _uiState.update { it.copy(busy = false, messageRes = R.string.account_verified_done, messageIsError = false) }
                    onDone()
                }
            }
        }
    }

    fun resendCode() {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            when (val result = accounts.resendVerification()) {
                is AccountResult.Failure -> fail(result.error)
                is AccountResult.Success ->
                    _uiState.update { it.copy(busy = false, messageRes = R.string.account_verify_sent, messageIsError = false) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            accounts.signOut()
            _uiState.update { UiState() }
        }
    }

    fun deleteAccount() {
        if (_uiState.value.busy) return
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            when (val result = accounts.deleteAccount()) {
                is AccountResult.Failure -> fail(result.error)
                is AccountResult.Success -> _uiState.update { UiState() }
            }
        }
    }

    private suspend fun handleSignIn(result: AccountResult<SignInOutcome>) {
        when (result) {
            is AccountResult.Failure -> fail(result.error)
            is AccountResult.Success -> {
                pendingGoogleToken = null
                val complete = !result.value.consentRequired
                if (complete) savedAdvice.refresh()
                _uiState.update {
                    it.copy(busy = false, googleConsent = false, password = "", done = complete)
                }
            }
        }
    }

    private fun fail(error: AccountError) {
        _uiState.update { state ->
            when (error) {
                AccountError.VALIDATION -> state.copy(busy = false, emailError = R.string.account_err_email_used)
                else -> state.copy(busy = false, messageRes = error.messageRes(), messageIsError = true)
            }
        }
    }

    private companion object {
        const val GOOGLE = "google"
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
