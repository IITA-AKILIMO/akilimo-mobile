package com.akilimo.mobile.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Light palette ────────────────────────────────────────────────────────────
private val Primary = Color(0xFF3D7600)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFBAEE82)
private val OnPrimaryContainer = Color(0xFF0D2100)

private val Secondary = Color(0xFF586200)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFDCE87A)
private val OnSecondaryContainer = Color(0xFF181D00)

private val Tertiary = Color(0xFFB5162A)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFFFDADC)
private val OnTertiaryContainer = Color(0xFF40000D)

private val Background = Color(0xFFF5FCE9)
private val OnBackground = Color(0xFF181D12)
private val Surface = Color(0xFFF5FCE9)
private val OnSurface = Color(0xFF181D12)
private val SurfaceVariant = Color(0xFFDCE6C8)
private val OnSurfaceVariant = Color(0xFF424940)

private val Error = Color(0xFFBA1A1A)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF410002)

private val Outline = Color(0xFF72796A)
private val OutlineVariant = Color(0xFFC1C9B2)
private val Scrim = Color(0xFF000000)

// ── Dark palette ─────────────────────────────────────────────────────────────
private val DarkPrimary = Color(0xFF9DD768)
private val DarkOnPrimary = Color(0xFF1C3A00)
private val DarkPrimaryContainer = Color(0xFF2C5900)
private val DarkOnPrimaryContainer = Color(0xFFBAEE82)

private val DarkSecondary = Color(0xFFBFCA6A)
private val DarkOnSecondary = Color(0xFF2D3300)
private val DarkSecondaryContainer = Color(0xFF434A00)
private val DarkOnSecondaryContainer = Color(0xFFDCE87A)

private val DarkTertiary = Color(0xFFFFB3B4)
private val DarkOnTertiary = Color(0xFF68000F)
private val DarkTertiaryContainer = Color(0xFF92001E)
private val DarkOnTertiaryContainer = Color(0xFFFFDADC)

private val DarkBackground = Color(0xFF10140C)
private val DarkOnBackground = Color(0xFFE1E8D4)
private val DarkSurface = Color(0xFF10140C)
private val DarkOnSurface = Color(0xFFE1E8D4)
private val DarkSurfaceVariant = Color(0xFF424940)
private val DarkOnSurfaceVariant = Color(0xFFC1C9B2)

private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

private val DarkOutline = Color(0xFF8B9383)
private val DarkOutlineVariant = Color(0xFF424940)

// ── Semantic extras (not part of Material ColorScheme but used in the app) ───
val AkilimoSuccess = Color(0xFF2D6A13)
val AkilimoSuccessContainer = Color(0xFFC3EFAB)
val AkilimoOnSuccess = Color(0xFFFFFFFF)
val AkilimoWarning = Color(0xFF7B5800)
val AkilimoWarningContainer = Color(0xFFFFDEAA)
val AkilimoOnWarning = Color(0xFFFFFFFF)
val AkilimoInfo = Color(0xFF0062A1)
val AkilimoInfoContainer = Color(0xFFCFE5FF)
val AkilimoOnInfo = Color(0xFFFFFFFF)

val AkilimoLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    outline = Outline,
    outlineVariant = OutlineVariant,
    scrim = Scrim,
)

val AkilimoDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = Scrim,
)
