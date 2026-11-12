package com.akilimo.mobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The signed-in themes use the IITA logo colours; their text must stay readable (WCAG AA, 4.5:1). */
class SignedInThemeTest {

    private fun contrast(a: Color, b: Color): Float {
        val (light, dark) = if (a.luminance() > b.luminance()) a to b else b to a
        return (light.luminance() + 0.05f) / (dark.luminance() + 0.05f)
    }

    private fun assertReadable(name: String, text: Color, background: Color) =
        assertTrue("$name needs 4.5:1, has ${contrast(text, background)}", contrast(text, background) >= MIN_CONTRAST)

    @Test
    fun `the brand colours are the ones sampled from the IITA logo`() {
        assertEquals(Color(0xFFF26422), IitaOrange)
        assertEquals(Color(0xFF07362A), CgiarGreen)
    }

    @Test
    fun `the light signed-in theme is readable and differs from the signed-out one`() {
        val scheme = AkilimoIitaLightColorScheme
        assertReadable("onPrimary on primary", scheme.onPrimary, scheme.primary)
        assertReadable("onPrimaryContainer on primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer)
        assertReadable("accent text on accent container", scheme.onIitaAccentContainer, scheme.iitaAccentContainer)
        assertNotEquals(AkilimoLightColorScheme.primary, scheme.primary)
    }

    @Test
    fun `the dark signed-in theme is readable and differs from the signed-out one`() {
        val scheme = AkilimoIitaDarkColorScheme
        assertReadable("onPrimary on primary", scheme.onPrimary, scheme.primary)
        assertReadable("onPrimaryContainer on primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer)
        assertReadable("accent text on accent container", scheme.onIitaAccentContainer, scheme.iitaAccentContainer)
        assertNotEquals(AkilimoDarkColorScheme.primary, scheme.primary)
    }

    private companion object {
        const val MIN_CONTRAST = 4.5f
    }
}
