package com.akilimo.mobile.ui.screens.recommendations

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.components.compose.StatusChip
import com.akilimo.mobile.ui.components.compose.StatusTone
import com.akilimo.mobile.ui.components.compose.colors
import com.akilimo.mobile.ui.theme.AkilimoSpacing

private val TILE_SIZE = 42.dp

@Composable
fun TaskItemCard(
    label: String,
    status: EnumStepStatus,
    onClick: () -> Unit,
    stepNumber: Int = -1,
) {
    val statusText = stringResource(status.labelRes())
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            // The chip also says it in words; TalkBack reads the state once, here
            .semantics { stateDescription = statusText }
            .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepTile(stepNumber = stepNumber, status = status)
            Spacer(modifier = Modifier.width(AkilimoSpacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(AkilimoSpacing.xs))
            StatusChip(text = statusText, tone = status.tone())
        }
    }
}

@StringRes
internal fun EnumStepStatus.labelRes(): Int = when (this) {
    EnumStepStatus.COMPLETED -> R.string.lbl_status_completed
    EnumStepStatus.IN_PROGRESS -> R.string.lbl_status_in_progress
    EnumStepStatus.NOT_STARTED -> R.string.lbl_status_not_started
}

private fun EnumStepStatus.tone(): StatusTone = when (this) {
    EnumStepStatus.COMPLETED -> StatusTone.Success
    EnumStepStatus.IN_PROGRESS -> StatusTone.Warning
    EnumStepStatus.NOT_STARTED -> StatusTone.Neutral
}

/** Rounded tile on the left: a tick when done, a clock in progress, otherwise the step number. */
@Composable
private fun StepTile(stepNumber: Int, status: EnumStepStatus) {
    val (container, content) = status.tone().colors()
    Surface(shape = MaterialTheme.shapes.small, color = container, modifier = Modifier.size(TILE_SIZE)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            when {
                status == EnumStepStatus.COMPLETED -> Icon(Icons.Default.Check, null, tint = content)
                status == EnumStepStatus.IN_PROGRESS -> Icon(Icons.Default.AccessTime, null, tint = content)
                stepNumber > 0 -> Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = content,
                )
            }
        }
    }
}
