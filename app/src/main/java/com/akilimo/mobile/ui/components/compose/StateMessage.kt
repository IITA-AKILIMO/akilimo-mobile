package com.akilimo.mobile.ui.components.compose

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * The one look for a screen or list that has nothing to show or could not load: a soft icon, a
 * title, a plain message, and optionally a hint line or an action button (for example Retry).
 * [isError] tints the icon red instead of lime.
 */
@Composable
fun StateMessage(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    hint: String? = null,
    technicalDetail: String? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    val container = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AkilimoSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
    ) {
        Surface(shape = CircleShape, color = container, modifier = Modifier.size(72.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(34.dp))
            }
        }
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (hint != null) {
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (technicalDetail != null) TechnicalDetail(technicalDetail)
        if (actionLabel != null) {
            Button(onClick = onAction, shape = MaterialTheme.shapes.medium) { Text(actionLabel) }
        }
    }
}

/**
 * The raw failure under an error message, built to be read out or pasted to support: a tonal card with a header and a
 * copy button, then the facts in a selectable code block, one per line. The copy adds the app version.
 */
@Composable
private fun TechnicalDetail(detail: String) {
    val context = LocalContext.current
    val copied = stringResource(R.string.msg_details_copied)
    val label = stringResource(R.string.lbl_technical_details)
    val copyLabel = stringResource(R.string.lbl_copy_details)
    val facts = remember(detail) { detail.split(DETAIL_SEPARATOR) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().widthIn(max = DETAIL_MAX_WIDTH),
    ) {
        Column(
            modifier = Modifier.padding(AkilimoSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.BugReport,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f).padding(start = AkilimoSpacing.xs),
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(label, "AKILIMO ${BuildConfig.VERSION_NAME}$DETAIL_SEPARATOR$detail")
                        )
                        Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = copyLabel,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SelectionContainer {
                    Column(
                        modifier = Modifier.padding(AkilimoSpacing.xs),
                        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs),
                    ) {
                        facts.forEach { fact ->
                            Text(
                                text = fact,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            )
                        }
                    }
                }
            }
            Text(
                text = stringResource(R.string.msg_details_share_hint),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private const val DETAIL_SEPARATOR = " \u00B7 "
private val DETAIL_MAX_WIDTH = 420.dp
