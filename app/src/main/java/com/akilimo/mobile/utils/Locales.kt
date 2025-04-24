package com.akilimo.mobile.utils

import java.util.Locale

object Locales {
    private val LOCAL_TZ_SWA = Locale("sw", "TZ")
    private val LOCAL_KE_SWA = Locale("sw", "KE")
    private val LOCAL_NG_ENGLISH = Locale("en", "NG")
    private val LOCAL_RW_KINYARWANDA = Locale("rw", "RW")

    val APP_LOCALES: List<Locale> = listOf(
        Locale.ENGLISH,
        LOCAL_TZ_SWA
    )

    val LOCALE_COUNTRIES: List<Locale> =
        listOf(LOCAL_TZ_SWA, LOCAL_NG_ENGLISH, LOCAL_RW_KINYARWANDA)
}
