package com.akilimo.mobile.data

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Single place that applies the app language.
 *
 * - [tag] non-null: "Override system language" is on, the app uses this language.
 * - [tag] null: the app follows the phone's language.
 *
 * Android 13+ applies per-app locales to every activity via the platform LocaleManager.
 * Below 13, AppCompat's per-app locales only reach AppCompatActivity, and MainActivity is a
 * ComponentActivity, so [wrap] localizes its base context explicitly.
 */
object AppLanguage {

    /** Persist settings to DataStore first, then call this (see AGENTS.md). */
    fun apply(tag: String?) {
        AppCompatDelegate.setApplicationLocales(
            if (tag.isNullOrBlank()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(tag)
        )
    }

    /** For Activity.attachBaseContext. */
    fun wrap(base: Context, tag: String?): Context {
        if (!tag.isNullOrBlank()) return LocaleHelper.wrap(base, tag)
        // Following the phone: undo a default left over from a previous override
        systemLocale()?.let { Locale.setDefault(it) }
        return base
    }

    /** Below Android 13 the activity must be recreated manually to pick up the change. */
    val needsManualRecreate: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU

    private fun systemLocale(): Locale? {
        val config = Resources.getSystem().configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales.takeIf { !it.isEmpty }?.get(0)
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
    }
}
