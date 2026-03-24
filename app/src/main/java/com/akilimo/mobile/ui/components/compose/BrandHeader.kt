package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * Full-width gradient brand hero block.
 *
 * Renders the AKILIMO logo on a primary-colored background with a subtle
 * top-to-transparent dark overlay for visual depth. Accepts optional title
 * and subtitle text. Use at the top of key screens (Welcome, Recommendations)
 * to provide consistent visual anchoring.
 */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
) {
    val depthOverlay = Brush.verticalGradient(
        colors = listOf(Color.Black.copy(alpha = 0.12f), Color.Transparent),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primary)
            .background(brush = depthOverlay)
            .padding(vertical = AkilimoSpacing.xl, horizontal = AkilimoSpacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_akilimo_logo_white),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.size(72.dp),
            )
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                )
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = AkilimoSpacing.sm),
                )
            }
        }
    }
}
