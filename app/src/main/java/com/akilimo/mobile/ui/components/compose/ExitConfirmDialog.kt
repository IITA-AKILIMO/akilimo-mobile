package com.akilimo.mobile.ui.components.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.akilimo.mobile.R

@Composable
fun ExitConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lbl_exit_application)) },
        text = { Text(stringResource(R.string.lbl_confirm_app_exit)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.lbl_yes)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.lbl_no)) }
        },
    )
}
