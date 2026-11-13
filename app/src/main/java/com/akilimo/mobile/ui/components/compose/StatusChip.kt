package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.theme.info
import com.akilimo.mobile.ui.theme.infoContainer
import com.akilimo.mobile.ui.theme.success
import com.akilimo.mobile.ui.theme.successContainer
import com.akilimo.mobile.ui.theme.warning
import com.akilimo.mobile.ui.theme.warningContainer

/** Meaning of a status, mapped to the app's semantic colours. */
enum class StatusTone { Success, Warning, Info, Error, Neutral }

/** Container and content colours for a [StatusTone]; every pair meets 4.5:1 contrast. */
@Composable
internal fun StatusTone.colors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        StatusTone.Success -> scheme.successContainer to scheme.success
        StatusTone.Warning -> scheme.warningContainer to scheme.warning
        StatusTone.Info -> scheme.infoContainer to scheme.info
        StatusTone.Error -> scheme.errorContainer to scheme.error
        StatusTone.Neutral -> scheme.surfaceVariant to scheme.onSurfaceVariant
    }
}

/**
 * A small pill that states a status in words (never colour alone), optionally with an icon:
 * "Completed", "Pending", "Unsynced".
 */
@Composable
fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Neutral,
    icon: ImageVector? = null,
) {
    val (container, content) = tone.colors()
    Surface(shape = CircleShape, color = container, contentColor = content, modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = AkilimoSpacing.sm, vertical = AkilimoSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(AkilimoSpacing.xxs))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
