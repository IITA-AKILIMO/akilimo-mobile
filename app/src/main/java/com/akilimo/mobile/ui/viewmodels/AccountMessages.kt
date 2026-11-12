package com.akilimo.mobile.ui.viewmodels

import androidx.annotation.StringRes
import com.akilimo.mobile.R
import com.akilimo.mobile.repos.account.AccountError

/** The message the farmer sees for each reason an account call did not work. */
@StringRes
fun AccountError.messageRes(): Int = when (this) {
    AccountError.NETWORK -> R.string.account_err_network
    AccountError.WRONG_CREDENTIALS -> R.string.account_err_wrong_credentials
    AccountError.VALIDATION -> R.string.account_err_email_used
    AccountError.CONSENT_REQUIRED -> R.string.account_err_consent
    AccountError.EMAIL_BELONGS_TO_ACCOUNT -> R.string.account_err_email_belongs
    AccountError.NO_EMAIL -> R.string.account_err_no_email
    AccountError.SESSION_EXPIRED -> R.string.account_err_session
    AccountError.TOO_MANY_ATTEMPTS -> R.string.account_err_too_many
    AccountError.UNAVAILABLE -> R.string.account_err_unavailable
    AccountError.BAD_CODE -> R.string.account_err_bad_code
    AccountError.SERVER -> R.string.account_err_server
}

/** The few checks worth doing on the phone before a call; the server decides everything else. */
object AccountInput {
    const val MIN_PASSWORD_LENGTH = 8

    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun isEmail(value: String): Boolean = EMAIL.matches(value.trim())

    fun isPassword(value: String): Boolean = value.length >= MIN_PASSWORD_LENGTH
}
