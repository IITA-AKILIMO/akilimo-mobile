package com.akilimo.mobile

import java.util.Locale


object Locales {

    val swahili: Locale = Locale.forLanguageTag("sw-TZ")
    val english: Locale = Locale.forLanguageTag("en-US")
    val kinyarwanda: Locale = Locale.forLanguageTag("rw-RW")

    val supportedLocales: List<Locale> = listOf(
        english,
        swahili,
        kinyarwanda,
    )

    /**
     * Converts any stored language code (old short codes like "en", "sw", "rw" or full
     * BCP-47 tags like "en-US") to the canonical BCP-47 tag used by the app.
     * Falls back to English if the code is unrecognised.
     */
    fun normalize(code: String): String {
        if (code.isBlank()) return english.toLanguageTag()
        return supportedLocales
            .find { it.toLanguageTag().equals(code, ignoreCase = true) || it.language.equals(code, ignoreCase = true) }
            ?.toLanguageTag()
            ?: english.toLanguageTag()
    }
}