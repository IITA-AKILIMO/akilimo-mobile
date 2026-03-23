package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BinaryToggleChips(
    labelA: String,
    labelB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        FilterChip(
            selected = selectedA,
            onClick = onSelectA,
            label = { Text(labelA) },
            modifier = Modifier.padding(end = 8.dp)
        )
        FilterChip(
            selected = !selectedA,
            onClick = onSelectB,
            label = { Text(labelB) }
        )
    }
}
