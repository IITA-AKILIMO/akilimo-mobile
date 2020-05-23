package com.iita.akilimo.utils

import org.junit.Assert
import org.junit.Test

internal class MathHelperTest {
    @Test
    fun convertToLocalCurrencySwahili() {
        val mathHelper = MathHelper()
        val band25 = "Dola 25 kwa"
        val currency = "TZS"
        val areaUnit = "acre"
        val fieldAreaAcre = "1.0"
        val selectedFieldArea = "1.0 acre"
        val separator = "kwa"

        val result = mathHelper.convertCurrency(
            band25,
            currency,
            areaUnit,
            fieldAreaAcre,
            selectedFieldArea,
            separator
        )
        Assert.assertEquals("56,000 TZS kwa 1.0 acre", result)
    }

    @Test
    fun convertToLocalCurrencyEnglish() {
        val mathHelper = MathHelper()
        val band25 = "25 USD per"
        val currency = "NGN"
        val areaUnit = "acre"
        val fieldAreaAcre = "1.0"
        val selectedFieldArea = "1.0 acre"
        val separator = "per"

        val result = mathHelper.convertCurrency(
            band25,
            currency,
            areaUnit,
            fieldAreaAcre,
            selectedFieldArea,
            separator
        )
        Assert.assertEquals("9,000 NGN per 1.0 acre", result)
    }
}