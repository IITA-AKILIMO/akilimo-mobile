package com.akilimo.mobile.ui.components.compose

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.viewmodels.NotificationSettingsViewModel

/**
 * Answers the view model's request for the notification permission: below Android 13 there is
 * nothing to ask, otherwise the system dialog is shown unless the permission is already granted.
 */
@Composable
fun NotificationPermissionEffect(viewModel: NotificationSettingsViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onPermissionResult(granted)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                NotificationSettingsViewModel.Effect.RequestPermission -> {
                    val needed = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    if (needed) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    else viewModel.onPermissionResult(true)
                }
            }
        }
    }
}

/** Rationale shown before the system permission dialog. */
@Composable
fun NotificationPromptDialog(onAllow: () -> Unit, onNotNow: () -> Unit) {
    AlertDialog(
        onDismissRequest = onNotNow,
        title = { Text(stringResource(R.string.notif_prompt_title)) },
        text = { Text(stringResource(R.string.notif_prompt_body)) },
        confirmButton = { TextButton(onClick = onAllow) { Text(stringResource(R.string.notif_prompt_allow)) } },
        dismissButton = { TextButton(onClick = onNotNow) { Text(stringResource(R.string.notif_prompt_not_now)) } },
    )
}

/**
 * Opens the phone's notification settings for AKILIMO. Android 8+ has a dedicated screen; older
 * versions (API 21 to 25) only have the app details page, which is also the fallback if the
 * dedicated screen is missing.
 */
fun openAppNotificationSettings(context: Context) {
    val details = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        context.startActivity(details)
        return
    }
    val dedicated = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(dedicated)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(details)
    }
}
