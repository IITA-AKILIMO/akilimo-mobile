package com.akilimo.mobile.utils

import io.sentry.Sentry
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

/**
 * Utility class that handles mathematical operations including:
 * - Currency conversions between USD and other currencies (NGN, TZS, GHS)
 * - Number formatting and rounding
 * - Area unit conversions (acre, ha, sqm, are)
 * - Investment amount calculations
 */
class MathHelper {
    companion object {
        // Area conversion constants
        private const val ACRE_TO_HECTARE = 0.404686  // 1 acre = 0.404686 hectares
        private const val ACRE_TO_SQM = 4046.86      // 1 acre = 4046.86 square meters
        private const val ACRE_TO_ARE = 40.469       // 1 acre = 40.469 ares

        // Common rounding values
        private const val DEFAULT_ROUNDING = 100.0
        private const val INVESTMENT_ROUNDING = 1000.0
        private const val DECIMAL_PRECISION = 100.0

        // Currency codes
        const val CURRENCY_NGN = "NGN"
        const val CURRENCY_TZS = "TZS"
        const val CURRENCY_GHS = "GHS"
        const val CURRENCY_USD = "USD"

        // Area unit types
        const val AREA_ACRE = "acre"
        const val AREA_HECTARE = "ha"
        const val AREA_SQM = "sqm"
        const val AREA_ARE = "are"
    }

    // Currency exchange rates (1 USD = X local currency)
    private var exchangeRates = mapOf(
        CURRENCY_NGN to 360.0,
        CURRENCY_TZS to 2250.0,
        CURRENCY_GHS to 6.11
    ).toMutableMap()

    /**
     * Converts a currency range string to another currency
     *
     * @param input The string containing currency value(s) to convert
     * @param targetCurrency The currency code to convert to
     * @return The converted currency string
     */
    private fun convertCurrency(input: String, targetCurrency: String): String {
        // If already in target currency, return as is
        if (input.contains(targetCurrency)) {
            return input
        }

        try {
            val cleanInput = Tools.replaceCharacters(input, "Dola", "")
            val numbersString = Tools.replaceNonNumbers(cleanInput, "TO")

            // Parse input into one or two values (for range)
            val values = numbersString.split("TO").filter { it.isNotEmpty() }
                .map { it.toDoubleOrNull() ?: 0.0 }

            if (values.isEmpty()) {
                return input
            }

            // Convert first value
            val firstConverted = convertToLocalCurrency(values[0], targetCurrency)

            // If we have a range, convert second value and return range
            return if (values.size > 1) {
                val secondConverted = convertToLocalCurrency(values[1], targetCurrency)
                "${formatNumber(firstConverted, null)} - ${
                    formatNumber(
                        secondConverted,
                        targetCurrency
                    )
                }"
            } else {
                formatNumber(firstConverted, targetCurrency)
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            return input // Return original input in case of error
        }
    }

    /**
     * Extended version of convertCurrency that also handles unit conversion
     *
     * @param input The input string to convert
     * @param targetCurrency The target currency code
     * @param currencySymbol The currency symbol to use in output
     * @param unitType The unit type (if any)
     * @param fieldSize The field size as string
     * @param selectedField The selected field description
     * @param separator The separator to use between amount and field
     * @return The converted and formatted string
     */
    fun convertCurrency(
        input: String,
        targetCurrency: String,
        currencySymbol: String?,
        unitType: String?,
        fieldSize: String,
        selectedField: String?,
        separator: String
    ): String {
        var result = convertCurrency(input, targetCurrency)

        try {
            // If unitType is provided and not already in the result
            if (unitType != null && !result.contains(unitType) && !result.contains(separator)) {
                val cleanedNumber = Tools.replaceNonNumbers(result, "").toDoubleOrNull() ?: 0.0
                val fieldSizeValue = fieldSize.toDoubleOrNull() ?: 0.0

                val investmentAmount = cleanedNumber * fieldSizeValue
                val roundedAmount =
                    roundToNearestSpecifiedValue(investmentAmount, INVESTMENT_ROUNDING)
                val formattedNumber = formatNumber(roundedAmount, null)

                result = "$formattedNumber $currencySymbol $separator $selectedField"
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
        }

        return result
    }

    /**
     * Formats a number according to the specified currency
     *
     * @param number The number to format
     * @param currency The currency code (if any)
     * @return The formatted number string
     */
    fun formatNumber(number: Double, currency: String?): String {
        return if (currency == null) {
            String.format(Locale.US, "%,.0f", number)
        } else {
            String.format("%,.0f $currency", number)
        }
    }

    /**
     * Converts an amount in USD to a local currency
     *
     * @param amount The USD amount to convert
     * @param targetCurrency The target currency code
     * @param nearestRounding Optional rounding precision
     * @return The converted amount
     */
    fun convertToLocalCurrency(
        amount: Double,
        targetCurrency: String,
        vararg nearestRounding: Int
    ): Double {
        val rounding = nearestRounding.firstOrNull()?.toDouble() ?: DEFAULT_ROUNDING

        try {
            // Get exchange rate or return original amount if currency not supported
            val rate = exchangeRates[targetCurrency] ?: return amount
            val converted = amount * rate

            return if (rounding > 0) {
                roundToNearestSpecifiedValue(converted, rounding)
            } else {
                converted
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            return amount
        }
    }

    /**
     * Converts a local currency amount to USD
     *
     * @param amount The amount to convert
     * @param sourceCurrency The source currency code
     * @param nearestRounding Optional rounding precision
     * @return The converted amount in USD
     */
    fun convertToUSD(
        amount: Double,
        sourceCurrency: String,
        vararg nearestRounding: Int
    ): Double {
        if (sourceCurrency == CURRENCY_USD) {
            return amount
        }

        val rounding = nearestRounding.firstOrNull()?.toDouble() ?: DEFAULT_ROUNDING

        try {
            // Get exchange rate or return original amount if currency not supported
            val rate = exchangeRates[sourceCurrency] ?: return amount
            val converted = amount / rate

            val roundedValue = roundToNearestSpecifiedValue(converted, rounding)

            // For small values, keep decimals; for larger values, round to integer
            return if (roundedValue < 1) {
                roundedValue
            } else {
                Math.round(roundedValue).toDouble()
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            return amount
        }
    }

    /**
     * Rounds a number to the nearest specified value
     *
     * @param value The value to round
     * @param nearest Round to nearest multiple of this value
     * @return The rounded value
     */
    fun roundToNearestSpecifiedValue(value: Double, nearest: Double): Double {
        if (value < 1 || nearest < 1) {
            return roundToNDecimalPlaces(value, DECIMAL_PRECISION)
        }

        return Math.round(value / nearest) * nearest
    }

    /**
     * Rounds a number to N decimal places
     *
     * @param value The value to round
     * @param decimalPlaces The number of decimal places to round to
     * @return The rounded value
     */
    fun roundToNDecimalPlaces(value: Double, decimalPlaces: Double = 2.0): Double {
        return Math.round(value * decimalPlaces) / decimalPlaces
    }

    /**
     * Computes the investment amount based on local currency, field size and currency
     *
     * @param localAmount The amount in local currency
     * @param fieldSize The field size
     * @param sourceCurrency The source currency code
     * @return The computed investment amount in USD
     */
    fun computeInvestmentAmount(
        localAmount: Double,
        fieldSize: Double,
        sourceCurrency: String
    ): Double {
        val totalAmount = localAmount * fieldSize
        val usdAmount = convertToUSD(totalAmount, sourceCurrency)

        return roundToNearestSpecifiedValue(usdAmount, INVESTMENT_ROUNDING)
    }

    /**
     * Safely converts a string to double
     *
     * @param text The text to convert
     * @return The double value or 0.0 if conversion fails
     */
    fun convertToDouble(text: String): Double {
        if (text.isEmpty()) {
            return 0.0
        }

        return try {
            text.trim().toDouble()
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            0.0
        }
    }

    /**
     * Converts an area from acres to another unit
     *
     * @param acres The area in acres
     * @param targetUnit The target unit to convert to
     * @return The converted area
     */
    fun convertFromAcreToSpecifiedArea(acres: Double, targetUnit: String): Double {
        val convertedArea = when (targetUnit) {
            AREA_ACRE -> acres
            AREA_HECTARE -> acres * ACRE_TO_HECTARE
            AREA_ARE -> acres * ACRE_TO_ARE
            AREA_SQM -> acres * ACRE_TO_SQM
            else -> acres
        }

        return roundToNDecimalPlaces(convertedArea, DECIMAL_PRECISION)
    }

    /**
     * Converts price to unit weight price
     *
     * @param price The price
     * @param unitWeight The unit weight in g
     * @return The price per kg
     */
    fun convertToUnitWeightPrice(price: Double, unitWeight: Int): Double {
        return (price * unitWeight) / 1000
    }

    /**
     * Removes leading/trailing zeros from a number
     *
     * @param value The value to format
     * @return Formatted string without unnecessary zeros
     */
    fun removeLeadingZero(value: Double): String {
        return removeLeadingZero(value, "0.#")
    }

    /**
     * Removes leading/trailing zeros from a number with custom pattern
     *
     * @param value The value to format
     * @param pattern The decimal format pattern
     * @return Formatted string according to pattern
     */
    fun removeLeadingZero(value: Double, pattern: String?): String {
        val format = DecimalFormat(pattern ?: "0.#")
        format.roundingMode = RoundingMode.CEILING
        return format.format(value)
    }

    /**
     * Computes investment based on area size and unit
     *
     * @param acreInvestment The investment amount per acre
     * @param areaSize The area size
     * @param areaUnit The area unit
     * @return The total investment for the specified area
     */
    fun computeInvestmentForSpecifiedAreaUnit(
        acreInvestment: Double,
        areaSize: Double,
        areaUnit: String
    ): Double {
        val amount = when (areaUnit) {
            AREA_ACRE -> acreInvestment * areaSize
            AREA_HECTARE -> (acreInvestment / ACRE_TO_HECTARE) * areaSize
            AREA_SQM -> (areaSize / ACRE_TO_SQM) * acreInvestment
            AREA_ARE -> (areaSize / ACRE_TO_ARE) * acreInvestment
            else -> acreInvestment * areaSize
        }

        return amount
    }
}