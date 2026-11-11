package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/** The raised, bordered card look: surface fill, thin border and a small shadow. */
@Composable
fun Modifier.cardSurface(): Modifier {
    val shape = MaterialTheme.shapes.medium
    return this
        .shadow(2.dp, shape)
        .background(MaterialTheme.colorScheme.surface, shape)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
}

/** A raised card that groups related form fields so a form reads as one block. */
@Composable
fun FormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardSurface()
            .padding(AkilimoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.md),
        content = content,
    )
}
