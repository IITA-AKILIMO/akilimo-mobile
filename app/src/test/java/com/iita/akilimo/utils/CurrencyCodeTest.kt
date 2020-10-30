package com.iita.akilimo.utils

import org.junit.Assert
import org.junit.Test

internal class CurrencyCodeTest {
    @Test
    fun should_match_symbol_for_nigeria() {
        val currencyCode = "NGN"

        val symbol = CurrencyCode.getCurrencySymbol(currencyCode)
        Assert.assertNotNull(symbol)
        Assert.assertEquals("₦", symbol)
    }

    @Test
    fun should_match_symbol_for_tanzania() {
        val currencyCode = "TZS"
        val symbol = CurrencyCode.getCurrencySymbol(currencyCode)
        Assert.assertNotNull(symbol)
        Assert.assertEquals("TSh", symbol)
    }
}