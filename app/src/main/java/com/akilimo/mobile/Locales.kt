package com.akilimo.mobile

import java.util.Locale


object Locales {

    val Swahili: Locale = Locale.forLanguageTag("sw-TZ")
    val English: Locale = Locale.forLanguageTag("en-US")
    val Kinyarwanda: Locale = Locale.forLanguageTag("rw-RW")

    val supportedLocales: List<Locale> = listOf(
        English,
        Swahili,
        Kinyarwanda
    )
}