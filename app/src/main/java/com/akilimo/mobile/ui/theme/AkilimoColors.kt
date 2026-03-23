package com.akilimo.mobile.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Light palette ────────────────────────────────────────────────────────────
private val Primary = Color(0xFF2E5900) // Deepened for sunlight contrast (was 0xFF3D7600)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFC8E68A) // Warmer, less neon (was 0xFFBAEE82)
private val OnPrimaryContainer = Color(0xFF0E2400) // Match deeper primary (was 0xFF0D2100)

private val Secondary = Color(0xFF4A5200) // Muted olive-earth (was 0xFF586200)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFD4E175) // Less saturated (was 0xFFDCE87A)
private val OnSecondaryContainer = Color(0xFF161900) // Adjusted

private val Tertiary = Color(0xFFB5162A)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFFFDADC)
private val OnTertiaryContainer = Color(0xFF40000D)

private val Background = Color(0xFFF8FBF4) // Softer green-white (was 0xFFF5FCE9)
private val OnBackground = Color(0xFF1C1F16) // Deeper charcoal-green (was 0xFF181D12)
private val Surface = Color(0xFFF8FBF4) // Match background (was 0xFFF5FCE9)
private val OnSurface = Color(0xFF1C1F16) // Match (was 0xFF181D12)
private val SurfaceVariant = Color(0xFFE3E8D8) // Neutral gray-green (was 0xFFDCE6C8)
private val OnSurfaceVariant = Color(0xFF424940)

private val Error = Color(0xFFBA1A1A)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF410002)

private val Outline = Color(0xFF6B7265) // Warmer gray (was 0xFF72796A)
private val OutlineVariant = Color(0xFFC1C9B2)
private val Scrim = Color(0xFF000000)

// ── Dark palette ─────────────────────────────────────────────────────────────
private val DarkPrimary = Color(0xFF91C660) // Adjusted (was 0xFF9DD768)
private val DarkOnPrimary = Color(0xFF132F00) // Adjusted
private val DarkPrimaryContainer = Color(0xFF244400) // Adjusted
private val DarkOnPrimaryContainer = Color(0xFF91C660) // Adjusted

private val DarkSecondary = Color(0xFFAEB95B) // Adjusted
private val DarkOnSecondary = Color(0xFF232700) // Adjusted
private val DarkSecondaryContainer = Color(0xFF353C00) // Adjusted
private val DarkOnSecondaryContainer = Color(0xFFAEB95B) // Adjusted

private val DarkTertiary = Color(0xFFFFB3B4)
private val DarkOnTertiary = Color(0xFF68000F)
private val DarkTertiaryContainer = Color(0xFF92001E)
private val DarkOnTertiaryContainer = Color(0xFFFFDADC)

private val DarkBackground = Color(0xFF141811) // Adjusted (was 0xFF10140C)
private val DarkOnBackground = Color(0xFFE2E3DE) // Adjusted
private val DarkSurface = Color(0xFF141811) // Match (was 0xFF10140C)
private val DarkOnSurface = Color(0xFFE2E3DE) // Adjusted
private val DarkSurfaceVariant = Color(0xFF44483D) // Adjusted
private val DarkOnSurfaceVariant = Color(0xFFC4C8BA) // Adjusted

private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

private val DarkOutline = Color(0xFF8E9285) // Adjusted
private val DarkOutlineVariant = Color(0xFF44483D) // Adjusted

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

// Extensions to expose semantic colors via MaterialTheme.colorScheme
val ColorScheme.success: Color get() = AkilimoSuccess
val ColorScheme.onSuccess: Color get() = AkilimoOnSuccess
val ColorScheme.successContainer: Color get() = AkilimoSuccessContainer

val ColorScheme.warning: Color get() = AkilimoWarning
val ColorScheme.onWarning: Color get() = AkilimoOnWarning
val ColorScheme.warningContainer: Color get() = AkilimoWarningContainer

val ColorScheme.info: Color get() = AkilimoInfo
val ColorScheme.onInfo: Color get() = AkilimoOnInfo
val ColorScheme.infoContainer: Color get() = AkilimoInfoContainer

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
