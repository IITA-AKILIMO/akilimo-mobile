package com.akilimo.mobile.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.data.account.AccountSession
import com.akilimo.mobile.navigation.ForgotPasswordRoute
import com.akilimo.mobile.navigation.SavedAdviceRoute
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.FormCard
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.SegmentedTabRow
import com.akilimo.mobile.ui.components.compose.StatusChip
import com.akilimo.mobile.ui.components.compose.StatusTone
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.screens.recommendations.navigateIfResumed
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.AccountViewModel
import androidx.compose.runtime.LaunchedEffect

/**
 * The optional account: sign in or create one (password or Google), the consent step, and, once signed in, confirming the
 * email, opening the saved advice, signing out and deleting the account. The app works without any of it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavHostController,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val consentStep = viewModel.consentStep(session, state)

    // A complete sign-in (consent given) goes back to where the farmer came from
    LaunchedEffect(state.done) {
        if (state.done) {
            viewModel.onDoneHandled()
            navController.popBackStackSafely()
        }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.account_title),
                onBack = { navController.popBackStackSafely() },
            )
        },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(Modifier.height(AkilimoSpacing.md))
            state.messageRes?.let { Notice(it, isError = state.messageIsError) }
            when {
                consentStep != AccountViewModel.ConsentStep.NONE -> ConsentContent(state.busy, viewModel)
                session != null -> SignedInContent(session!!, state.busy, navController, viewModel)
                else -> SignedOutContent(state, navController, viewModel)
            }
            Spacer(Modifier.height(AkilimoSpacing.xl))
        }
    }
}

@Composable
private fun Notice(messageRes: Int, isError: Boolean) {
    val container = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AkilimoSpacing.md),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
        ) {
            Icon(if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle, contentDescription = null)
            Text(text = stringResource(messageRes), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ── Signed out: sign in / create account ─────────────────────────────────────

@Composable
private fun SignedOutContent(state: AccountViewModel.UiState, navController: NavHostController, viewModel: AccountViewModel) {
    val signUp = state.mode == AccountViewModel.Mode.SIGN_UP
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val google = rememberGoogleSignIn(onToken = viewModel::onGoogleToken, onFailed = viewModel::onGoogleFailed)

    Text(
        text = stringResource(R.string.account_intro),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = AkilimoSpacing.md),
    )
    SegmentedTabRow(
        options = listOf(stringResource(R.string.account_tab_sign_in), stringResource(R.string.account_tab_create)),
        selectedIndex = if (signUp) 1 else 0,
        onSelect = { viewModel.setMode(if (it == 1) AccountViewModel.Mode.SIGN_UP else AccountViewModel.Mode.SIGN_IN) },
    )
    Spacer(Modifier.height(AkilimoSpacing.md))

    FormCard {
        if (signUp) {
            AkilimoTextField(
                value = state.name,
                onValueChange = viewModel::onName,
                label = stringResource(R.string.account_name),
                enabled = !state.busy,
            )
        }
        AkilimoTextField(
            value = state.email,
            onValueChange = viewModel::onEmail,
            label = stringResource(R.string.account_email),
            error = state.emailError?.let { stringResource(it) },
            enabled = !state.busy,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        AkilimoTextField(
            value = state.password,
            onValueChange = viewModel::onPassword,
            label = stringResource(R.string.account_password),
            helperText = if (signUp) stringResource(R.string.account_password_hint) else null,
            error = state.passwordError?.let { stringResource(it) },
            enabled = !state.busy,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = stringResource(
                            if (showPassword) R.string.account_hide_password else R.string.account_show_password
                        ),
                    )
                }
            },
        )
        if (signUp) {
            ConsentCheckbox(
                checked = state.consent,
                error = state.consentError,
                enabled = !state.busy,
                onChange = viewModel::onConsent,
            )
            ConsentText()
        }
        Button(onClick = viewModel::submit, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            if (state.busy) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(if (signUp) R.string.account_btn_create else R.string.account_btn_sign_in))
            }
        }
        if (!signUp) {
            TextButton(
                onClick = { navController.navigateIfResumed(ForgotPasswordRoute) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text(stringResource(R.string.account_forgot)) }
        }
    }

    if (googleSignInEnabled) {
        Spacer(Modifier.height(AkilimoSpacing.md))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(stringResource(R.string.account_or), color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(AkilimoSpacing.md))
        OutlinedButton(onClick = google, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_btn_google))
        }
    }
}

@Composable
private fun ConsentCheckbox(checked: Boolean, error: Int?, enabled: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = onChange, enabled = enabled)
            Text(stringResource(R.string.account_consent_checkbox), style = MaterialTheme.typography.bodyLarge)
        }
        error?.let {
            Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ConsentText() {
    Text(
        text = stringResource(R.string.account_consent_body),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ── Consent step ─────────────────────────────────────────────────────────────

@Composable
private fun ConsentContent(busy: Boolean, viewModel: AccountViewModel) {
    FormCard {
        Text(
            text = stringResource(R.string.account_consent_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(text = stringResource(R.string.account_consent_body), style = MaterialTheme.typography.bodyMedium)
        Button(onClick = viewModel::agree, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_consent_agree))
        }
        OutlinedButton(onClick = viewModel::declineConsent, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_consent_decline))
        }
    }
}

// ── Signed in ────────────────────────────────────────────────────────────────

@Composable
private fun ColumnScope.SignedInContent(
    session: AccountSession,
    busy: Boolean,
    navController: NavHostController,
    viewModel: AccountViewModel,
) {
    var showVerify by rememberSaveable { mutableStateOf(false) }
    var showDelete by rememberSaveable { mutableStateOf(false) }

    FormCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm)) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column {
                Text(
                    text = stringResource(R.string.account_signed_in_as, session.displayName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (session.name.isNotBlank()) {
                    Text(session.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        StatusChip(
            text = stringResource(if (session.emailVerified) R.string.account_email_verified else R.string.account_email_unverified),
            tone = if (session.emailVerified) StatusTone.Success else StatusTone.Warning,
        )
        if (!session.emailVerified) {
            OutlinedButton(onClick = { showVerify = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.account_confirm_email))
            }
        }
        Button(onClick = { navController.navigateIfResumed(SavedAdviceRoute) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_open_saved))
        }
        OutlinedButton(onClick = viewModel::signOut, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.account_sign_out))
        }
    }
    Spacer(Modifier.height(AkilimoSpacing.md))
    TextButton(onClick = { showDelete = true }, enabled = !busy, modifier = Modifier.align(Alignment.CenterHorizontally)) {
        Text(stringResource(R.string.account_delete), color = MaterialTheme.colorScheme.error)
    }

    if (showVerify) {
        VerifyEmailDialog(
            email = session.email,
            busy = busy,
            onResend = viewModel::resendCode,
            onSubmit = { code -> viewModel.verifyEmail(code) { showVerify = false } },
            onDismiss = { showVerify = false },
        )
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.account_delete_title)) },
            text = { Text(stringResource(R.string.account_delete_body)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.deleteAccount() }) {
                    Text(stringResource(R.string.account_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.lbl_cancel)) } },
        )
    }
}

@Composable
private fun VerifyEmailDialog(
    email: String,
    busy: Boolean,
    onResend: () -> Unit,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.account_confirm_email)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm)) {
                Text(stringResource(R.string.account_verify_body, email))
                AkilimoTextField(value = code, onValueChange = { code = it }, label = stringResource(R.string.account_verify_code))
                TextButton(onClick = onResend, enabled = !busy) { Text(stringResource(R.string.account_verify_resend)) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(code) }, enabled = !busy && code.isNotBlank()) {
                Text(stringResource(R.string.account_verify_submit))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.lbl_cancel)) } },
    )
}
