package com.iita.akilimo.utils

import org.junit.Assert
import org.junit.Test

internal class CurrencyCodeTest {
    @Test
    fun should_match_symbol_for_nigeria() {
        val currencyCode = "NGN"

        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        Assert.assertNotNull(extendedCurrency)
        Assert.assertEquals("₦", extendedCurrency.symbol)
    }

    @Test
    fun should_match_symbol_for_tanzania() {
        val currencyCode = "TZS"
        val extendedCurrency = CurrencyCode.getCurrencySymbol(currencyCode)
        Assert.assertNotNull(extendedCurrency)
        Assert.assertEquals("TSh", extendedCurrency.symbol)
    }
}
