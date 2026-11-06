package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.theme.AkilimoSpacing

@Composable
fun TaskItemCard(
    label: String,
    status: EnumStepStatus,
    onClick: () -> Unit,
    stepNumber: Int = -1,
) {
    val isCompleted = status == EnumStepStatus.COMPLETED
    val statusText = stringResource(status.labelRes())
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            // Status is otherwise conveyed only by colour and an icon; TalkBack reads it here
            .semantics { stateDescription = statusText }
            .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else
                MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (stepNumber > 0) {
                StepBadge(stepNumber = stepNumber, status = status)
                Spacer(modifier = Modifier.width(AkilimoSpacing.md))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(AkilimoSpacing.xs))
            StatusBadge(status = status)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@StringRes
internal fun EnumStepStatus.labelRes(): Int = when (this) {
    EnumStepStatus.COMPLETED -> R.string.lbl_status_completed
    EnumStepStatus.IN_PROGRESS -> R.string.lbl_status_in_progress
    EnumStepStatus.NOT_STARTED -> R.string.lbl_status_not_started
}

@Composable
private fun StepBadge(stepNumber: Int, status: EnumStepStatus) {
    val isCompleted = status == EnumStepStatus.COMPLETED
    Surface(
        shape = CircleShape,
        color = if (isCompleted) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(AkilimoSpacing.xxl),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(AkilimoSpacing.xxs),
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: EnumStepStatus) {
    when (status) {
        EnumStepStatus.IN_PROGRESS -> Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(end = AkilimoSpacing.xs),
        )
        else -> Unit
    }
}
