package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

/** True while a farmer is signed in; screens use it for the small "you are signed in" cues. */
val LocalSignedIn = staticCompositionLocalOf { false }

/**
 * @param textScale the reader's own text size on top of the phone's font size (1 = unchanged).
 *   It scales every `sp` size in the app, so screens never multiply sizes themselves.
 * @param signedIn a farmer is signed in: the app takes the IITA-coloured theme so it is plain at a glance.
 */
@Composable
fun AkilimoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textScale: Float = 1f,
    signedIn: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        signedIn && darkTheme -> AkilimoIitaDarkColorScheme
        signedIn -> AkilimoIitaLightColorScheme
        darkTheme -> AkilimoDarkColorScheme
        else -> AkilimoLightColorScheme
    }
    val density = LocalDensity.current

    CompositionLocalProvider(
        LocalDensity provides Density(density.density, density.fontScale * textScale),
        LocalSignedIn provides signedIn,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AkilimoTypography,
            shapes = AkilimoShapes,
            content = content,
        )
    }
}

/** A thin accent line along the bottom edge of a bar, drawn over its content (it moves with a collapsing bar). */
fun Modifier.bottomAccentLine(color: Color): Modifier = drawWithContent {
    drawContent()
    val height = 3.dp.toPx()
    drawRect(color = color, topLeft = Offset(0f, size.height - height), size = Size(size.width, height))
}
