package com.akilimo.mobile.repos.account

import com.akilimo.mobile.network.dto.ApiErrorResponse
import com.squareup.moshi.Moshi
import retrofit2.Response

/** Why an account call did not work, in the terms the screens need (each maps to one message). */
enum class AccountError {
    /** No connection or a timeout. */
    NETWORK,

    /** Wrong email or password. */
    WRONG_CREDENTIALS,

    /** The input was refused (see [AccountResult.Failure.fieldErrors]). */
    VALIDATION,

    /** The farmer must agree to the consent text first. */
    CONSENT_REQUIRED,

    /** The social sign-in email belongs to an account the provider did not vouch for: sign in with the password. */
    EMAIL_BELONGS_TO_ACCOUNT,

    /** The provider shared no email address. */
    NO_EMAIL,

    /** The token is no longer valid: the farmer must sign in again. */
    SESSION_EXPIRED,

    /** Too many attempts; wait a minute. */
    TOO_MANY_ATTEMPTS,

    /** The feature is not switched on at the server yet (for example email, or a sign-in provider). */
    UNAVAILABLE,

    /** A wrong or expired code. */
    BAD_CODE,

    SERVER,
}

sealed interface AccountResult<out T> {
    data class Success<T>(val value: T) : AccountResult<T>

    data class Failure(
        val error: AccountError,
        /** Field name to messages, as the server sent them (email, password, consent, ...). */
        val fieldErrors: Map<String, List<String>> = emptyMap(),
    ) : AccountResult<Nothing>
}

internal object AccountErrors {
    private val adapter = Moshi.Builder().build().adapter(ApiErrorResponse::class.java)

    fun parse(response: Response<*>): ApiErrorResponse? =
        runCatching { response.errorBody()?.string()?.let(adapter::fromJson) }.getOrNull()

    /**
     * Maps an unsuccessful answer. [authenticated] says whether the call carried the farmer's token: a 401 then means the
     * session ended, and without a token it means wrong credentials.
     */
    fun failure(response: Response<*>, authenticated: Boolean): AccountResult.Failure {
        val body = parse(response)
        val error = when (response.code()) {
            401 -> if (authenticated) AccountError.SESSION_EXPIRED else AccountError.WRONG_CREDENTIALS
            403 -> if (body?.code == "consent_required") AccountError.CONSENT_REQUIRED else AccountError.SESSION_EXPIRED
            404, 503 -> AccountError.UNAVAILABLE
            409 -> AccountError.EMAIL_BELONGS_TO_ACCOUNT
            422 -> when (body?.code) {
                "consent_required" -> AccountError.CONSENT_REQUIRED
                "no_email" -> AccountError.NO_EMAIL
                else -> if (body?.errors.isNullOrEmpty()) AccountError.BAD_CODE else AccountError.VALIDATION
            }
            429 -> AccountError.TOO_MANY_ATTEMPTS
            else -> AccountError.SERVER
        }
        return AccountResult.Failure(error, body?.errors.orEmpty())
    }
}
