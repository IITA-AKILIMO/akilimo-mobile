package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AkilimoShapes = Shapes(
    // ExtraSmall — chips, compact inputs
    extraSmall = RoundedCornerShape(4.dp),
    // Small — text fields, small cards
    small = RoundedCornerShape(8.dp),
    // Medium — cards (matches ShapeAppearance.Akilimo.Card = 12dp)
    medium = RoundedCornerShape(12.dp),
    // Large — bottom sheets, dialogs (matches ShapeAppearance.Akilimo.Large = 24dp)
    large = RoundedCornerShape(24.dp),
    // ExtraLarge — full pill buttons (50%)
    extraLarge = RoundedCornerShape(50),
)
