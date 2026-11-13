package com.akilimo.mobile.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeModeMigrationTest {

    private val darkMode = booleanPreferencesKey("darkMode")
    private val themeMode = stringPreferencesKey("themeMode")

    @Test
    fun `nothing stored starts in light`() {
        assertEquals(ThemeMode.LIGHT, emptyPreferences().themeMode())
    }

    @Test
    fun `legacy dark mode on becomes DARK`() {
        assertEquals(ThemeMode.DARK, preferencesOf(darkMode to true).themeMode())
    }

    @Test
    fun `legacy dark mode off becomes LIGHT`() {
        assertEquals(ThemeMode.LIGHT, preferencesOf(darkMode to false).themeMode())
    }

    @Test
    fun `stored theme mode wins over the legacy flag`() {
        assertEquals(
            ThemeMode.LIGHT,
            preferencesOf(themeMode to "LIGHT", darkMode to true).themeMode(),
        )
    }

    @Test
    fun `unknown stored value falls back to the legacy flag`() {
        assertEquals(ThemeMode.DARK, preferencesOf(themeMode to "PURPLE", darkMode to true).themeMode())
    }
}
