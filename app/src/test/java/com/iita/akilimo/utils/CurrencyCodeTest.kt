package com.iita.akilimo.utils

import org.junit.Assert
import org.junit.Test
import java.util.*

internal class CurrencyCodeTest {
    @Test
    fun should_match_symbol_for_nigeria() {
        val countryCode = "NG"
        val locale = Locale("EN", countryCode)

        val symbol = CurrencyCode.getCurrencySymbol(locale)
        Assert.assertNotNull(symbol)
        Assert.assertEquals("NGN", symbol.symbol)
    }

    @Test
    fun should_match_symbol_for_tanzania() {
        val countryCode = "TZ"
        val locale = Locale("sw_TZ", countryCode)

        val symbol = CurrencyCode.getCurrencySymbol(locale)
        Assert.assertNotNull(symbol)
        Assert.assertEquals("TZS", symbol.symbol)
    }
}