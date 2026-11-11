package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AkilimoShapes = Shapes(
    // ExtraSmall (4dp) — chips, segmented-button tracks, compact inputs
    extraSmall = RoundedCornerShape(4.dp),
    // Small (12dp) — text fields, radio rows, small cards (M3 default is 8dp)
    small = RoundedCornerShape(12.dp),
    // Medium (20dp) — cards, primary buttons (M3 default is 12dp)
    medium = RoundedCornerShape(20.dp),
    // Large (32dp) — large containers, navigation drawers (M3 default is 16dp)
    large = RoundedCornerShape(32.dp),
    // ExtraLarge (40dp) — dialogs, bottom sheets (M3 default is 28dp)
    extraLarge = RoundedCornerShape(40.dp),
)
