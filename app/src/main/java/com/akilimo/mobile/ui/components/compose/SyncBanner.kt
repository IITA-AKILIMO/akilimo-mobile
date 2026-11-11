package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * A full-width strip that tells the farmer the state of their data: offline, waiting to sync,
 * or synced. Strings come from the caller so they follow the in-app language.
 */
@Composable
fun SyncBanner(
    title: String,
    modifier: Modifier = Modifier,
    detail: String? = null,
    tone: StatusTone = StatusTone.Warning,
    offline: Boolean = false,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val (container, content) = tone.colors()
    Surface(color = container, contentColor = content, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (offline) Icons.Default.CloudOff else Icons.Default.Sync,
                contentDescription = null,
            )
            Spacer(Modifier.width(AkilimoSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                if (detail != null) {
                    Text(text = detail, style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (actionLabel != null) {
                TextButton(onClick = onAction) { Text(text = actionLabel) }
            }
        }
    }
}
