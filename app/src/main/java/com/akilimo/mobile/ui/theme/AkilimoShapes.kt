package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AkilimoShapes = Shapes(
    // ExtraSmall (4dp) — chips, segmented-button tracks, compact inputs
    extraSmall = RoundedCornerShape(4.dp),
    // Small (8dp) — text fields, radio rows, small cards
    small = RoundedCornerShape(8.dp),
    // Medium (12dp) — cards, primary buttons
    medium = RoundedCornerShape(12.dp),
    // Large (16dp) — large containers, navigation drawers
    large = RoundedCornerShape(16.dp),
    // ExtraLarge (28dp) — dialogs, bottom sheets
    extraLarge = RoundedCornerShape(28.dp),
)
