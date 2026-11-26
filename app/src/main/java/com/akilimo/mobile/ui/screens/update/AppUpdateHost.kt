package com.akilimo.mobile.ui.screens.update

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akilimo.mobile.network.reportError
import com.akilimo.mobile.repos.UpdateLevel
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.AppUpdateViewModel
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Wraps the app with the update prompts. The server decides how urgent an update is ([AppUpdateViewModel]); Google Play's
 * in-app update does the installing: a flexible (background) update when a newer version exists, an immediate (blocking)
 * one below the minimum. [UpdateRequiredScreen] covers the app in the blocking case and is the way out for installs that
 * did not come from Play, where the Play flow is unavailable.
 */
@Composable
fun AppUpdateHost(
    modifier: Modifier = Modifier,
    viewModel: AppUpdateViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember(context) { AppUpdateManagerFactory.create(context) }
    var downloaded by remember { mutableStateOf(false) }

    // Play shows its own screens; there is nothing to do with the result (the Required screen stays until the app restarts)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {}

    DisposableEffect(manager) {
        val listener = InstallStateUpdatedListener { installState ->
            if (installState.installStatus() == InstallStatus.DOWNLOADED) downloaded = true
        }
        manager.registerListener(listener)
        onDispose { manager.unregisterListener(listener) }
    }

    LaunchedEffect(state.level) {
        if (state.level != UpdateLevel.NONE) {
            startPlayUpdate(manager, launcher, immediate = state.blocking, onlyIfInProgress = false)
        }
    }

    // Back from Play or the background: pick up an immediate update Play left in progress, or a finished download
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        // A long-lived process: ask the server again once the last answer is old
        viewModel.recheck(minIntervalMs = RECHECK_INTERVAL_MS)
        scope.launch {
            if (state.blocking) {
                startPlayUpdate(manager, launcher, immediate = true, onlyIfInProgress = true)
            } else if (state.level == UpdateLevel.AVAILABLE && manager.isDownloaded()) {
                downloaded = true
            }
        }
    }

    Box(modifier = modifier) {
        content()
        when {
            state.blocking -> UpdateRequiredScreen(
                message = state.message,
                onUpdate = { openStoreListing(context, state.storeUrl) },
            )

            state.showBanner || (downloaded && state.level == UpdateLevel.AVAILABLE) -> UpdateBanner(
                downloaded = downloaded,
                message = state.message,
                onUpdate = { openStoreListing(context, state.storeUrl) },
                onRestart = { manager.completeUpdate() },
                onDismiss = viewModel::dismissBanner,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(AkilimoSpacing.md),
            )
        }
    }
}

/** How old the server's answer may be before coming back to the app asks again. */
private const val RECHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L

private suspend fun AppUpdateManager.isDownloaded(): Boolean = try {
    requestAppUpdateInfo().installStatus() == InstallStatus.DOWNLOADED
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Timber.d(e, "Could not read the Play update state")
    false
}

/**
 * Starts Google Play's update flow when Play offers one. Anything that goes wrong (not installed from Play, Play missing
 * or out of date, offline) is non-blocking: the server's answer alone decides what the farmer sees.
 */
private suspend fun startPlayUpdate(
    manager: AppUpdateManager,
    launcher: ActivityResultLauncher<IntentSenderRequest>,
    immediate: Boolean,
    onlyIfInProgress: Boolean,
) {
    try {
        val info = manager.requestAppUpdateInfo()
        val type = if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        val inProgress = info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
        val offered = !onlyIfInProgress && info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        if ((offered || inProgress) && info.isUpdateTypeAllowed(type)) {
            manager.startUpdateFlowForResult(info, launcher, AppUpdateOptions.newBuilder(type).build())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: com.google.android.play.core.install.InstallException) {
        // Expected for sideloaded and debug builds, or a device without Play: the blocking screen is the fallback
        Timber.d(e, "Play in-app update unavailable")
    } catch (e: Exception) {
        reportError(e)
    }
}
