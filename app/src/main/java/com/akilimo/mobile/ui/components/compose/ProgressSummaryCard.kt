package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * Prominent summary at the top of a multi-step screen: a small [title], a large [figure]
 * ("3 of 8") with its [caption], a progress bar and a [footer] line. [progress] is clamped to 0..1.
 */
@Composable
fun ProgressSummaryCard(
    title: String,
    figure: String,
    caption: String,
    progress: Float,
    footer: String,
    modifier: Modifier = Modifier,
) {
    val onHero = MaterialTheme.colorScheme.onPrimary
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = onHero)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
                Text(
                    text = figure,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = onHero,
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyLarge,
                    color = onHero,
                    modifier = Modifier.padding(bottom = AkilimoSpacing.xxs),
                )
            }
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primaryContainer,
                trackColor = onHero.copy(alpha = 0.25f),
            )
            Text(text = footer, style = MaterialTheme.typography.bodyMedium, color = onHero)
        }
    }
}
