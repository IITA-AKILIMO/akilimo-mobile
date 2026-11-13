package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/** Placeholder bar widths, as a share of the text column, so the rows do not look identical. */
private val LINE_WIDTHS = listOf(0.6f to 0.35f, 0.5f to 0.3f, 0.7f to 0.4f, 0.55f to 0.32f)

/**
 * Grey placeholder cards in the shape of real list rows, shown while a list loads instead of a
 * lone spinner. Static, hidden from TalkBack (the screen announces its own state).
 */
@Composable
fun SkeletonList(modifier: Modifier = Modifier, rows: Int = LINE_WIDTHS.size) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(AkilimoSpacing.md)
            .clearAndSetSemantics { },
        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
    ) {
        repeat(rows) { index ->
            val (first, second) = LINE_WIDTHS[index % LINE_WIDTHS.size]
            SkeletonRow(firstLine = first, secondLine = second)
        }
    }
}

@Composable
private fun SkeletonRow(firstLine: Float, secondLine: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(AkilimoSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBar(modifier = Modifier.size(40.dp), shape = MaterialTheme.shapes.small)
        Spacer(Modifier.width(AkilimoSpacing.sm))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
            SkeletonBar(modifier = Modifier.fillMaxWidth(firstLine).height(12.dp))
            SkeletonBar(modifier = Modifier.fillMaxWidth(secondLine).height(12.dp))
        }
    }
}

@Composable
private fun SkeletonBar(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(6.dp),
) {
    Spacer(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape))
}
