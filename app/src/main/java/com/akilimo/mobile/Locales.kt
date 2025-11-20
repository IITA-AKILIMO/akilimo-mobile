package com.akilimo.mobile

import java.util.Locale


object Locales {

    val TanzaniaSwahili = Locale.forLanguageTag("sw-TZ")
    val KenyaSwahili = Locale.forLanguageTag("sw-KE")
    val English = Locale.forLanguageTag("en-US")
    val RwandaKinyarwanda = Locale.forLanguageTag("rw-RW")

    val supportedLocales: List<Locale> = listOf(
        Locale.ENGLISH,
        TanzaniaSwahili
    )

    val localeCountries: List<Locale> = listOf(
        TanzaniaSwahili,
        English,
        RwandaKinyarwanda
    )
}