package com.akilimo.mobile.repos.account

import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.network.dto.AccountLoginRequest
import com.akilimo.mobile.network.dto.AuthResponse
import com.akilimo.mobile.network.dto.ConsentRequest
import com.akilimo.mobile.network.dto.ForgotPasswordRequest
import com.akilimo.mobile.network.dto.RegisterRequest
import com.akilimo.mobile.network.dto.ResetPasswordRequest
import com.akilimo.mobile.network.dto.SocialSignInRequest
import com.akilimo.mobile.network.dto.VerifyEmailRequest
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** What a successful sign-in tells the screens. */
data class SignInOutcome(
    /** The farmer must still agree to the consent text (an older account): nothing is saved or shown until then. */
    val consentRequired: Boolean,
    /** Earlier advice of this phone that was linked to the account. */
    val claimedRequests: Int,
)

/**
 * Optional farmer accounts: sign up, sign in (password or a provider such as Google), consent, email confirmation,
 * password reset, sign out and deleting the account. The app works without any of it.
 */
@Singleton
class AccountRepo @Inject constructor(
    private val calls: AccountCalls,
    private val sessionStore: AccountSessionStore,
    private val settings: AppSettingsDataStore,
    private val savedAdvice: SavedAdviceRepo,
) {
    suspend fun register(email: String, password: String, name: String?, consent: Boolean): AccountResult<SignInOutcome> =
        calls.execute(authenticated = false) {
            it.register(RegisterRequest(email.trim(), password, name?.trim()?.ifBlank { null }, consent, settings.deviceToken))
        }.signedIn()

    suspend fun login(email: String, password: String): AccountResult<SignInOutcome> =
        calls.execute(authenticated = false) {
            it.login(AccountLoginRequest(email.trim(), password, settings.deviceToken))
        }.signedIn()

    /** [accessToken] is the provider's OAuth access token (not an ID token). [consent] is needed on a first sign-in. */
    suspend fun signInWithProvider(provider: String, accessToken: String, consent: Boolean): AccountResult<SignInOutcome> =
        calls.execute(authenticated = false) {
            it.social(provider, SocialSignInRequest(accessToken, consent, settings.deviceToken))
        }.signedIn()

    /** Records the farmer's agreement (after a sign-in that answered consent required) and links this phone's advice. */
    suspend fun acceptConsent(): AccountResult<Int> =
        when (val result = calls.execute(authenticated = true) { it.consent(ConsentRequest(true, settings.deviceToken)) }) {
            is AccountResult.Failure -> result
            is AccountResult.Success -> {
                sessionStore.update { it.copy(consentRequired = false) }
                AccountResult.Success(result.value.claimedRequests)
            }
        }

    suspend fun verifyEmail(code: String): AccountResult<Unit> {
        val email = sessionStore.session.value?.email ?: return AccountResult.Failure(AccountError.SESSION_EXPIRED)
        return when (val result = calls.execute(authenticated = false) { it.verifyEmail(VerifyEmailRequest(email, code.trim())) }) {
            is AccountResult.Failure -> result
            is AccountResult.Success -> {
                sessionStore.update { it.copy(emailVerified = true) }
                AccountResult.Success(Unit)
            }
        }
    }

    suspend fun resendVerification(): AccountResult<Unit> =
        calls.execute(authenticated = true) { it.resendVerification() }.unit()

    /** Always succeeds from the farmer's side when the server answers: it never says whether the address has an account. */
    suspend fun forgotPassword(email: String): AccountResult<Unit> =
        calls.execute(authenticated = false) { it.forgotPassword(ForgotPasswordRequest(email.trim())) }.unit()

    suspend fun resetPassword(email: String, code: String, password: String): AccountResult<Unit> =
        calls.execute(authenticated = false) { it.resetPassword(ResetPasswordRequest(email.trim(), code.trim(), password)) }.unit()

    /** Signs out. The phone forgets the session and the saved advice copies even if the server cannot be reached. */
    suspend fun signOut() {
        if (sessionStore.isSignedIn) calls.execute(authenticated = true) { it.logout() }
        sessionStore.clear()
        savedAdvice.clearAll()
    }

    /** Deletes the account (past requests stay on the server as anonymous statistics) and everything kept on the phone. */
    suspend fun deleteAccount(): AccountResult<Unit> =
        when (val result = calls.execute(authenticated = true) { it.deleteAccount() }) {
            is AccountResult.Failure -> result
            is AccountResult.Success -> {
                sessionStore.clear()
                savedAdvice.clearAll()
                AccountResult.Success(Unit)
            }
        }

    private fun AccountResult<AuthResponse>.signedIn(): AccountResult<SignInOutcome> = when (this) {
        is AccountResult.Failure -> this
        is AccountResult.Success -> {
            val auth = value
            sessionStore.save(
                AccountSession(
                    token = auth.token,
                    expiresAt = runCatching { OffsetDateTime.parse(auth.expiresAt).toInstant().toEpochMilli() }
                        .getOrDefault(System.currentTimeMillis() + FALLBACK_TTL_MS),
                    accountId = auth.user.id,
                    name = auth.user.name.orEmpty(),
                    email = auth.user.email,
                    emailVerified = auth.user.emailVerified,
                    consentRequired = auth.consentRequired,
                )
            )
            AccountResult.Success(SignInOutcome(auth.consentRequired, auth.claimedRequests))
        }
    }

    private fun <T : Any> AccountResult<T>.unit(): AccountResult<Unit> = when (this) {
        is AccountResult.Failure -> this
        is AccountResult.Success -> AccountResult.Success(Unit)
    }

    private companion object {
        const val FALLBACK_TTL_MS = 24L * 60 * 60 * 1000
    }
}
