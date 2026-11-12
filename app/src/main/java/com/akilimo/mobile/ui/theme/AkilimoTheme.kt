package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * @param textScale the reader's own text size on top of the phone's font size (1 = unchanged).
 *   It scales every `sp` size in the app, so screens never multiply sizes themselves.
 */
@Composable
fun AkilimoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textScale: Float = 1f,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) AkilimoDarkColorScheme else AkilimoLightColorScheme
    val density = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * textScale),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AkilimoTypography,
            shapes = AkilimoShapes,
            content = content,
        )
    }
}
