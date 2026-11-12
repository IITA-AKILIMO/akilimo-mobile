package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akilimo.mobile.R
import com.akilimo.mobile.repos.account.AccountRepo
import com.akilimo.mobile.repos.account.AccountResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Forgot password: ask for a code by email, then set a new password with it. */
@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val accounts: AccountRepo,
) : ViewModel() {

    enum class Step { EMAIL, CODE, DONE }

    data class UiState(
        val step: Step = Step.EMAIL,
        val email: String = "",
        val code: String = "",
        val password: String = "",
        val busy: Boolean = false,
        @param:StringRes val emailError: Int? = null,
        @param:StringRes val passwordError: Int? = null,
        @param:StringRes val messageRes: Int? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun onEmail(value: String) = _uiState.update { it.copy(email = value, emailError = null, messageRes = null) }
    fun onCode(value: String) = _uiState.update { it.copy(code = value, messageRes = null) }
    fun onPassword(value: String) = _uiState.update { it.copy(password = value, passwordError = null, messageRes = null) }

    fun sendCode() {
        val state = _uiState.value
        if (state.busy) return
        if (!AccountInput.isEmail(state.email)) {
            _uiState.update { it.copy(emailError = R.string.account_err_email) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            when (val result = accounts.forgotPassword(state.email)) {
                is AccountResult.Failure ->
                    _uiState.update { it.copy(busy = false, messageRes = result.error.messageRes()) }
                // The server answers the same whether or not the address has an account
                is AccountResult.Success -> _uiState.update { it.copy(busy = false, step = Step.CODE) }
            }
        }
    }

    fun resetPassword() {
        val state = _uiState.value
        if (state.busy) return
        if (!AccountInput.isPassword(state.password)) {
            _uiState.update { it.copy(passwordError = R.string.account_err_password) }
            return
        }
        if (state.code.isBlank()) {
            _uiState.update { it.copy(messageRes = R.string.account_err_bad_code) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(busy = true, messageRes = null) }
            when (val result = accounts.resetPassword(state.email, state.code, state.password)) {
                is AccountResult.Failure ->
                    _uiState.update { it.copy(busy = false, messageRes = result.error.messageRes()) }
                is AccountResult.Success -> _uiState.update { it.copy(busy = false, step = Step.DONE, password = "") }
            }
        }
    }
}
