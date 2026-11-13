package com.akilimo.mobile.ui.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.AkilimoTextField
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.FormCard
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.ForgotPasswordViewModel

/** Reset a forgotten password: ask for a code by email, then choose a new password with it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.forgot_title),
                onBack = { navController.popBackStackSafely() },
            )
        },
    ) { padding ->
        ScrollableFormColumn(padding = padding) {
            Spacer(Modifier.height(AkilimoSpacing.md))
            state.messageRes?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AkilimoSpacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(AkilimoSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
                    ) {
                        Icon(Icons.Outlined.ErrorOutline, contentDescription = null)
                        Text(stringResource(message), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            when (state.step) {
                ForgotPasswordViewModel.Step.EMAIL -> FormCard {
                    Text(stringResource(R.string.forgot_body), style = MaterialTheme.typography.bodyMedium)
                    AkilimoTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmail,
                        label = stringResource(R.string.account_email),
                        error = state.emailError?.let { stringResource(it) },
                        enabled = !state.busy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Button(onClick = viewModel::sendCode, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.forgot_send))
                    }
                }

                ForgotPasswordViewModel.Step.CODE -> FormCard {
                    Text(stringResource(R.string.forgot_sent), style = MaterialTheme.typography.bodyMedium)
                    AkilimoTextField(
                        value = state.code,
                        onValueChange = viewModel::onCode,
                        label = stringResource(R.string.forgot_code),
                        enabled = !state.busy,
                    )
                    AkilimoTextField(
                        value = state.password,
                        onValueChange = viewModel::onPassword,
                        label = stringResource(R.string.forgot_new_password),
                        helperText = stringResource(R.string.account_password_hint),
                        error = state.passwordError?.let { stringResource(it) },
                        enabled = !state.busy,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    Button(onClick = viewModel::resetPassword, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.forgot_reset))
                    }
                }

                ForgotPasswordViewModel.Step.DONE -> FormCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.forgot_done), style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(onClick = { navController.popBackStackSafely() }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.account_btn_sign_in))
                    }
                }
            }
            Spacer(Modifier.height(AkilimoSpacing.xl))
        }
    }
}
