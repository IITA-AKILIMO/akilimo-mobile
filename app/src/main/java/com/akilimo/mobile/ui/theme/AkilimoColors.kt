package com.akilimo.mobile.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// ── Light palette ────────────────────────────────────────────────────────────
private val Primary = Color(0xFF006F45) // logo deep green
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFFE4F2C6) // tint of the logo lime
private val OnPrimaryContainer = Color(0xFF1F3300)

private val Secondary = Color(0xFF58624E) // Neutral olive
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFDCE6CF)
private val OnSecondaryContainer = Color(0xFF151E0F)

private val Tertiary = Color(0xFFB5162A)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFFFFDADC)
private val OnTertiaryContainer = Color(0xFF40000D)

private val Background = Color(0xFFFDFDF6) // Lighter background
private val OnBackground = Color(0xFF1A1C18)
private val Surface = Color(0xFFFDFDF6)
private val OnSurface = Color(0xFF1A1C18)
private val SurfaceVariant = Color(0xFFE1E4D5)
private val OnSurfaceVariant = Color(0xFF44483E)

private val Error = Color(0xFFBA1A1A)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF410002)

private val Outline = Color(0xFF6B7265) // Warmer gray (was 0xFF72796A)
private val OutlineVariant = Color(0xFFC1C9B2)
private val Scrim = Color(0xFF000000)

// ── Dark palette ─────────────────────────────────────────────────────────────
private val DarkPrimary = Color(0xFF9ACA3C) // logo lime
private val DarkOnPrimary = Color(0xFF1F3300)
private val DarkPrimaryContainer = Color(0xFF00553A)
private val DarkOnPrimaryContainer = Color(0xFFC9E88F)

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
// Contrast (WCAG): every on-X/X pair and every X-as-text-on-XContainer pair is
// ≥ 4.5:1 in both themes; X as text on the theme background is ≥ 6:1.
//
// Light variants (kept under their original public names for compatibility).
val AkilimoSuccess = Color(0xFF2D6A13)
val AkilimoSuccessContainer = Color(0xFFC3EFAB)
val AkilimoOnSuccess = Color(0xFFFFFFFF)
val AkilimoWarning = Color(0xFF7B5800)
val AkilimoWarningContainer = Color(0xFFFFDEAA)
val AkilimoOnWarning = Color(0xFFFFFFFF)
val AkilimoInfo = Color(0xFF0062A1)
val AkilimoInfoContainer = Color(0xFFCFE5FF)
val AkilimoOnInfo = Color(0xFFFFFFFF)

// Dark variants.
val AkilimoDarkSuccess = Color(0xFF9CD67D)
val AkilimoDarkSuccessContainer = Color(0xFF1E4D0A)
val AkilimoDarkOnSuccess = Color(0xFF0C3900)
val AkilimoDarkWarning = Color(0xFFF5BD48)
val AkilimoDarkWarningContainer = Color(0xFF5C4200)
val AkilimoDarkOnWarning = Color(0xFF412D00)
val AkilimoDarkInfo = Color(0xFF9ECAFF)
val AkilimoDarkInfoContainer = Color(0xFF0A4A7A)
val AkilimoDarkOnInfo = Color(0xFF003258)

/**
 * A scheme is treated as dark when its background is dark. Deriving this from the
 * scheme itself (rather than a CompositionLocal) keeps the accessors below usable
 * outside composition (e.g. in draw lambdas) and correct for any Akilimo scheme.
 */
private val ColorScheme.isDarkScheme: Boolean get() = background.luminance() < 0.5f

// Extensions to expose semantic colors via MaterialTheme.colorScheme
val ColorScheme.success: Color
    get() = if (isDarkScheme) AkilimoDarkSuccess else AkilimoSuccess
val ColorScheme.onSuccess: Color
    get() = if (isDarkScheme) AkilimoDarkOnSuccess else AkilimoOnSuccess
val ColorScheme.successContainer: Color
    get() = if (isDarkScheme) AkilimoDarkSuccessContainer else AkilimoSuccessContainer

val ColorScheme.warning: Color
    get() = if (isDarkScheme) AkilimoDarkWarning else AkilimoWarning
val ColorScheme.onWarning: Color
    get() = if (isDarkScheme) AkilimoDarkOnWarning else AkilimoOnWarning
val ColorScheme.warningContainer: Color
    get() = if (isDarkScheme) AkilimoDarkWarningContainer else AkilimoWarningContainer

val ColorScheme.info: Color
    get() = if (isDarkScheme) AkilimoDarkInfo else AkilimoInfo
val ColorScheme.onInfo: Color
    get() = if (isDarkScheme) AkilimoDarkOnInfo else AkilimoOnInfo
val ColorScheme.infoContainer: Color
    get() = if (isDarkScheme) AkilimoDarkInfoContainer else AkilimoInfoContainer

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

// ── Signed-in look: the IITA logo colours ────────────────────────────────────
// A farmer who is signed in gets a theme that blends with the IITA and CGIAR logo at iita.org: the IITA orange and the
// CGIAR dark green. Both values were sampled from the logo image, not guessed.
val IitaOrange = Color(0xFFF26422)
val CgiarGreen = Color(0xFF07362A)

/** Light theme for a signed-in farmer: the CGIAR dark green as the primary colour. Every pair keeps 4.5:1 contrast. */
val AkilimoIitaLightColorScheme = AkilimoLightColorScheme.copy(
    primary = CgiarGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E8DE),
    onPrimaryContainer = Color(0xFF002117),
)

/** Dark theme for a signed-in farmer: the IITA orange as the primary colour, with dark text on it. */
val AkilimoIitaDarkColorScheme = AkilimoDarkColorScheme.copy(
    primary = IitaOrange,
    onPrimary = Color(0xFF2B0D00),
    primaryContainer = CgiarGreen,
    onPrimaryContainer = Color(0xFFBFE3D2),
)

/** The IITA orange accent: the line under the top bars and the "signed in" pill. Use it for decoration, not for small text. */
val ColorScheme.iitaAccent: Color
    get() = IitaOrange
val ColorScheme.iitaAccentContainer: Color
    get() = if (isDarkScheme) Color(0xFF5A2208) else Color(0xFFFFDBC9)
val ColorScheme.onIitaAccentContainer: Color
    get() = if (isDarkScheme) Color(0xFFFFDBC9) else Color(0xFF3B1000)
