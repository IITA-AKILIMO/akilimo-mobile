package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

/**
 * Display guidance, tips, and important notices with semantic color coding.
 */
@Composable
fun InfoCard(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Info,
    modifier: Modifier = Modifier,
    type: InfoCardType = InfoCardType.Info
) {
    val containerColor = when (type) {
        InfoCardType.Info -> MaterialTheme.colorScheme.infoContainer
        InfoCardType.Warning -> MaterialTheme.colorScheme.warningContainer
        InfoCardType.Error -> MaterialTheme.colorScheme.errorContainer
        InfoCardType.Success -> MaterialTheme.colorScheme.successContainer
    }

    val contentColor = when (type) {
        InfoCardType.Info -> MaterialTheme.colorScheme.info
        InfoCardType.Warning -> MaterialTheme.colorScheme.warning
        InfoCardType.Error -> MaterialTheme.colorScheme.error
        InfoCardType.Success -> MaterialTheme.colorScheme.success
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor // 👈 ensures text/icons match
        ),
        border = BorderStroke(1.dp, contentColor),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(Modifier.width(AkilimoSpacing.sm))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor // 👈 use contentColor
                )
                Spacer(Modifier.height(AkilimoSpacing.xxs))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor // 👈 use contentColor
                )
            }
        }
    }
}

enum class InfoCardType { Info, Warning, Error, Success }
