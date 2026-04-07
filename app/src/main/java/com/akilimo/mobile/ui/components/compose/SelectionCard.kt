package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.akilimo.mobile.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * A card that displays an image, title, optional subtitle, and optional description.
 * Switches between grid (image-dominant, text below) and list (image left, text right)
 * layouts based on [isGridLayout].
 * Enhanced with Material 3 selection states and checkmark badge.
 */
@Composable
fun SelectionCard(
    imageRes: Int,
    title: String,
    subtitle: String? = null,
    description: String? = null,
    isSelected: Boolean,
    isGridLayout: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageColorFilter: ColorFilter? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outlineVariant,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isGridLayout) {
                GridContent(imageRes, title, subtitle, description, imageColorFilter)
            } else {
                ListContent(imageRes, title, subtitle, description, imageColorFilter)
            }

            if (isSelected) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(AkilimoSpacing.xs)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.lbl_selected),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GridContent(
    imageRes: Int,
    title: String,
    subtitle: String?,
    description: String?,
    imageColorFilter: ColorFilter? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = AkilimoSpacing.xs)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            colorFilter = imageColorFilter,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(AkilimoSpacing.sm)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AkilimoSpacing.xs)
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AkilimoSpacing.xs)
            )
        }
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AkilimoSpacing.xs)
            )
        }
    }
}

@Composable
private fun ListContent(
    imageRes: Int,
    title: String,
    subtitle: String?,
    description: String?,
    imageColorFilter: ColorFilter? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(AkilimoSpacing.sm)
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = title,
            contentScale = ContentScale.Fit,
            colorFilter = imageColorFilter,
            modifier = Modifier.size(88.dp)
        )
        Spacer(modifier = Modifier.width(AkilimoSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(AkilimoSpacing.xxs))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (description != null) {
                Spacer(modifier = Modifier.height(AkilimoSpacing.xxs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
