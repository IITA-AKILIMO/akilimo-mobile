package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.CloudQueue
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.workers.SyncPhase
import com.akilimo.mobile.workers.SyncStatus

/**
 * A quiet full-width strip for the hub: offline, updating, the last update time, or a failure.
 *
 * The text always states the condition; the icon and its colour only reinforce it. Offline wins
 * over a running sync, because nothing can update without a network. A retry action is shown
 * only for a failure, when there is a network to retry on.
 */
@Composable
fun SyncStatusStrip(
    status: SyncStatus,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (icon, message) = when (status.phase) {
        SyncPhase.OFFLINE -> Icons.Outlined.CloudOff to stringResource(R.string.msg_sync_offline)
        SyncPhase.SYNCING -> Icons.Outlined.CloudQueue to stringResource(R.string.msg_sync_running)
        SyncPhase.FAILED -> Icons.Outlined.ErrorOutline to stringResource(R.string.msg_sync_failed)
        SyncPhase.SYNCED -> Icons.Outlined.CloudDone to lastUpdatedText(status.lastSyncedAt)
        SyncPhase.NEVER -> Icons.Outlined.CloudQueue to stringResource(R.string.msg_sync_never)
    }
    val tone = when (status.phase) {
        SyncPhase.OFFLINE -> StatusTone.Warning
        SyncPhase.SYNCING -> StatusTone.Info
        SyncPhase.FAILED -> StatusTone.Error
        SyncPhase.SYNCED -> StatusTone.Success
        SyncPhase.NEVER -> StatusTone.Neutral
    }
    val iconColor = tone.colors().second

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (status.phase == SyncPhase.SYNCING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = iconColor,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconColor,
                )
            }
            Spacer(Modifier.width(AkilimoSpacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            if (status.phase == SyncPhase.FAILED && onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.lbl_retry))
                }
            }
        }
    }
}

@Composable
private fun lastUpdatedText(epochMillis: Long?): String {
    if (epochMillis == null) return stringResource(R.string.msg_sync_never)

    val elapsed = (System.currentTimeMillis() - epochMillis).coerceAtLeast(0L)
    val minutes = (elapsed / 60_000L).toInt()
    val hours = (elapsed / 3_600_000L).toInt()
    val days = (elapsed / 86_400_000L).toInt()
    return when {
        minutes < 1 -> stringResource(R.string.msg_sync_updated_just_now)
        minutes < 60 -> pluralStringResource(R.plurals.msg_sync_updated_minutes_ago, minutes, minutes)
        hours < 24 -> pluralStringResource(R.plurals.msg_sync_updated_hours_ago, hours, hours)
        else -> pluralStringResource(R.plurals.msg_sync_updated_days_ago, days, days)
    }
}
