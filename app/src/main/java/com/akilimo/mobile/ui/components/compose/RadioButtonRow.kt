package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * A row containing a RadioButton and a Label with a large touch target.
 * Enhanced with Material 3 selection states and improved spacing.
 */
@Composable
fun RadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AkilimoSpacing.xxs)
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        border = if (selected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = if (enabled) onClick else null,
                modifier = Modifier.size(24.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(AkilimoSpacing.sm))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
