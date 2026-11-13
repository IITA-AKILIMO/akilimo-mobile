package com.akilimo.mobile.ui.components.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A two-option toggle; a thin wrapper over [SegmentedTabRow] so both look and behave the same. */
@Composable
fun BinaryToggleChips(
    labelA: String,
    labelB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedTabRow(
        options = listOf(labelA, labelB),
        selectedIndex = if (selectedA) 0 else 1,
        onSelect = { if (it == 0) onSelectA() else onSelectB() },
        modifier = modifier,
    )
}
