package com.akilimo.mobile

import java.util.Locale


object Locales {

    val TanzaniaSwahili: Locale = Locale.forLanguageTag("sw-TZ")
    val English: Locale = Locale.forLanguageTag("en-US")
    val RwandaKinyarwanda: Locale = Locale.forLanguageTag("rw-RW")

    val supportedLocales: List<Locale> = listOf(
        English,
        TanzaniaSwahili,
        RwandaKinyarwanda
    )

    val localeCountries: List<Locale> = listOf(
        TanzaniaSwahili,
        English,
        RwandaKinyarwanda
    )
}