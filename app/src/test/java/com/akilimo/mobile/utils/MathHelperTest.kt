package com.akilimo.mobile.utils

import org.junit.Assert
import org.junit.Test

internal class MathHelperTest {

    private val mathHelper = MathHelper()

    @Test
    fun convertToLocalCurrencySwahili() {
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
        val result = mathHelper.convertFromAcreToSpecifiedArea(0.25, "acre")
        Assert.assertEquals(0.25, result, 0.0)
    }

    @Test
    fun convertAcreToHa() {
        val result = mathHelper.convertFromAcreToSpecifiedArea(2.5, "ha")
        Assert.assertEquals(1.01, result, 0.0)
    }

    @Test
    fun convertAcreToSQM() {
        val result = mathHelper.convertFromAcreToSpecifiedArea(1.0, "sqm")
        Assert.assertEquals(4046.86, result, 0.0)
    }

    @Test
    fun convertAcreToAre() {
        val result = mathHelper.convertFromAcreToSpecifiedArea(1.0, "are")
        Assert.assertEquals(40.47, result, 0.0)
    }

    @Test
    fun compute_price_by_per_tonne_of_cassava() {
        val expected = 166000.00
        val unitWeight = 1000
        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun compute_price_by_per_100_kg_of_cassava() {
        val expected = 16600.00
        val unitWeight = 100

        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun compute_price_by_per_50_kg_of_cassava() {
        val expected = 8300.00
        val unitWeight = 50
        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun compute_price_by_per_kg_of_cassava() {
        val expected = 166.00
        val unitWeight = 1
        val result = mathHelper.convertToUnitWeightPrice(166000.00, unitWeight)
        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun computeInvestmentForSpecifiedAreaUnit_one_acre() {
        val expected: Double = 230200.0 * 1
        val areaUnit: String = "acre"
        val areaSizeAcre: Double = 1.0
        val acreInvestmentAmount: Double = 230200.00
        val result = mathHelper.computeInvestmentForSpecifiedAreaUnit(
            acreInvestmentAmount,
            areaSizeAcre,
            areaUnit
        )

        Assert.assertEquals(expected, result, 0.0)
    }

    @Test
    fun computeInvestmentForSpecifiedAreaUnit() {
        val expected: Double = 230200.0 * 10.0
        val areaUnit: String = "acre"
        val areaSizeAcre: Double = 10.0
        val acreInvestmentAmount: Double = 230200.00

        val result = mathHelper.computeInvestmentForSpecifiedAreaUnit(
            acreInvestmentAmount,
            areaSizeAcre,
            areaUnit
        )

        Assert.assertEquals(expected, result, 0.0)
    }
}
