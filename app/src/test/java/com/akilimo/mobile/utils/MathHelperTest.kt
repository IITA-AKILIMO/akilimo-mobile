package com.akilimo.mobile.utils

import org.junit.Assert
import org.junit.Test

internal class MathHelperTest {
    @Test
    fun convertToLocalCurrencySwahili() {
        val mathHelper = MathHelper()
        val band25 = "Dola 25 kwa"
        val currency = "TZS"
        val currencySymbol = "TZS"
        val areaUnit = "acre"
        val fieldAreaAcre = "1.0"
        val selectedFieldArea = "1.0 acre"
        val separator = "kwa"

        val result = mathHelper.convertCurrency(
            band25,
            currency,
            currencySymbol,
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
        val currencySymbol = "NGN"
        val areaUnit = "acre"
        val fieldAreaAcre = "1.0"
        val selectedFieldArea = "1.0 acre"
        val separator = "per"

        val result = mathHelper.convertCurrency(
            band25,
            currency,
            currencySymbol,
            areaUnit,
            fieldAreaAcre,
            selectedFieldArea,
            separator
        )
        Assert.assertEquals("9,000 NGN per 1.0 acre", result)
    }

    @Test
    fun convertAcreToAcre() {
        val baseAcre = 2.471
        val baseSqm = 4046.86
        val mathHelper = MathHelper(baseAcre, baseSqm)


        val result = mathHelper.convertFromAcreToSpecifiedArea(0.25, "acre")
        Assert.assertEquals(0.25, result, 0.0)
    }

    @Test
    fun convertAcreToHa() {
        val baseAcre = 2.471
        val baseSqm = 4046.86
        val mathHelper = MathHelper(baseAcre, baseSqm)


        val result = mathHelper.convertFromAcreToSpecifiedArea(2.5, "ha")
        Assert.assertEquals(1.0, result, 0.0)
    }

    @Test
    fun convertAcreToSQM() {
        val baseAcre = 2.471
        val baseSqm = 4046.86
        val mathHelper = MathHelper(baseAcre, baseSqm)


        val result = mathHelper.convertFromAcreToSpecifiedArea(1.0, "sqm")
        Assert.assertEquals(4046.9, result, 0.0)
    }

    @Test
    fun compute_price_by_per_tonne_of_cassava() {
        val expected = 166000.00
        val unitWeight = 1000
        val mathHelper = MathHelper()

        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun compute_price_by_per_100_kg_of_cassava() {
        val expected = 16600.00
        val unitWeight = 100
        val mathHelper = MathHelper()

        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun compute_price_by_per_50_kg_of_cassava() {
        val expected = 8300.00
        val unitWeight = 50
        val mathHelper = MathHelper()

        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun compute_price_by_per_kg_of_cassava() {
        val expected = 166.00
        val unitWeight = 1
        val mathHelper = MathHelper()

        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }
}