package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AkilimoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AkilimoDarkColorScheme else AkilimoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AkilimoTypography,
        shapes = AkilimoShapes,
        content = content,
    )
}
