package com.akilimo.mobile.ui.screens.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.IconTile
import com.akilimo.mobile.ui.components.compose.InfoCard
import com.akilimo.mobile.ui.components.compose.InfoCardType
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import timber.log.Timber

/**
 * Blocking screen shown when the installed build is below the server's minimum. It covers the whole app and is the
 * fallback for installs that did not come from Google Play (where the in-app update flow is not available).
 */
@Composable
fun UpdateRequiredScreen(
    message: String?,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .safeDrawingPadding()
                .padding(AkilimoSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.md, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconTile(imageVector = Icons.Outlined.SystemUpdate)
            Text(
                text = stringResource(R.string.lbl_update_required),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message ?: stringResource(R.string.msg_update_required),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onUpdate,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.lbl_update_now))
            }
        }
    }
}

/** Dismissible notice that a newer version exists, or (once downloaded) that a restart finishes the update. */
@Composable
fun UpdateBanner(
    downloaded: Boolean,
    message: String?,
    onUpdate: () -> Unit,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs)) {
            InfoCard(
                title = stringResource(if (downloaded) R.string.lbl_update_downloaded else R.string.lbl_update_available),
                message = message
                    ?: stringResource(if (downloaded) R.string.msg_update_downloaded else R.string.msg_update_available),
                icon = Icons.Outlined.SystemUpdate,
                type = InfoCardType.Info,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!downloaded) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.lbl_update_later)) }
                    TextButton(onClick = onUpdate) { Text(stringResource(R.string.lbl_update_now)) }
                } else {
                    TextButton(onClick = onRestart) { Text(stringResource(R.string.lbl_update_restart)) }
                }
            }
        }
    }
}

/** Opens the store listing: the URL from the server, else the Play Store app, else Play in a browser. */
fun openStoreListing(context: Context, storeUrl: String?) {
    val packageName = context.packageName
    val candidates = listOfNotNull(
        storeUrl,
        "market://details?id=$packageName",
        "https://play.google.com/store/apps/details?id=$packageName",
    )
    for (url in candidates) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (e: ActivityNotFoundException) {
            Timber.d(e, "No app to open %s", url)
        }
    }
}
