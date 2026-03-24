package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A binary toggle component using Material 3 SegmentedButton.
 * Provides a clear visual connection between the two options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinaryToggleChips(
    labelA: String,
    labelB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(2.dp)
    ) {
        SegmentedButton(
            selected = selectedA,
            onClick = onSelectA,
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                inactiveBorderColor = Color.Transparent,
                activeBorderColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(labelA, style = MaterialTheme.typography.labelLarge)
        }

        SegmentedButton(
            selected = !selectedA,
            onClick = onSelectB,
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            colors = SegmentedButtonDefaults.colors(
                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                inactiveContainerColor = Color.Transparent,
                inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                inactiveBorderColor = Color.Transparent,
                activeBorderColor = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(labelB, style = MaterialTheme.typography.labelLarge)
        }
    }
}
