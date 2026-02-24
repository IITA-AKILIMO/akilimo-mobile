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
}