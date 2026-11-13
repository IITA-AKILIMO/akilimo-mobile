package com.akilimo.mobile.ui.screens.account

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.akilimo.mobile.BuildConfig
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope

/**
 * Whether the "Continue with Google" button is offered. It needs the app's Google Cloud OAuth client (package name and
 * signing certificate), which the owner sets up; until then (`akilimo.google.signin=true` is not set) the button stays hidden.
 */
val googleSignInEnabled: Boolean get() = BuildConfig.GOOGLE_SIGN_IN_ENABLED

/**
 * Starts Google sign-in and hands the result back. The API wants a Google OAuth **access token** (not an ID token), so this
 * asks Google's authorization client for the basic profile scopes; the farmer may be shown Google's own consent sheet first.
 * Returns the function that starts it.
 */
@Composable
fun rememberGoogleSignIn(onToken: (String) -> Unit, onFailed: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val client = Identity.getAuthorizationClient(context)

    fun deliver(result: AuthorizationResult) {
        val token = result.accessToken
        if (token.isNullOrBlank()) onFailed() else onToken(token)
    }

    // Google may need the farmer's approval first: it hands back a sheet to show, then the result
    val sheet = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            runCatching { client.getAuthorizationResultFromIntent(activityResult.data) }
                .onSuccess(::deliver)
                .onFailure { onFailed() }
        } else {
            onFailed()
        }
    }

    return {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope("email"), Scope("profile")))
            .build()
        client.authorize(request)
            .addOnSuccessListener { result ->
                val pending = result.pendingIntent
                if (result.hasResolution() && pending != null) {
                    sheet.launch(IntentSenderRequest.Builder(pending.intentSender).build())
                } else {
                    deliver(result)
                }
            }
            .addOnFailureListener { onFailed() }
    }
}