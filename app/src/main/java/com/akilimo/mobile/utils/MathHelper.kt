package com.akilimo.mobile.utils

import android.app.Activity
import com.akilimo.mobile.utils.Tools.replaceCharacters
import com.akilimo.mobile.utils.Tools.replaceNonNumbers
import io.sentry.Sentry
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Locale

class MathHelper() {

    private var ngnRate = 360.0
    private var tzsRate = 2250.0
    private var ghsRate = 6.11

    private val baseAcre = 2.471
    private val baseSqm = 4046.86
    private val baseAre = 40.469

    constructor(activity: Activity?) : this() {
        activity?.let {
            val sessionManager = SessionManager(it)
            ngnRate = sessionManager.ngnRate
            tzsRate = sessionManager.tzsRate
            ghsRate = sessionManager.ghsRate
        }
    }

    private fun convertCurrency(input: String, toCurrency: String): String {
        var value = replaceCharacters(input, "Dola", "")
        if (value.contains(toCurrency)) return value

        return try {
            val cleanValue = replaceNonNumbers(value, "TO")
            val parts = cleanValue.split("TO", ignoreCase = true)
            val band1 = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
            val band2 = parts.getOrNull(1)?.toDoubleOrNull()

            when {
                band1 > 0 && band2 != null && band2 > 0 -> {
                    val rate1 = convertToLocalCurrency(band1, toCurrency)
                    val rate2 = convertToLocalCurrency(band2, toCurrency)
                    "${formatNumber(rate1)} - ${formatNumber(rate2, toCurrency)}"
                }

                band1 > 0 -> {
                    val rate = convertToLocalCurrency(band1, toCurrency)
                    formatNumber(rate, toCurrency)
                }

                else -> ""
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            ""
        }
    }

    fun convertCurrency(
        input: String,
        toCurrency: String,
        currencySymbol: String?,
        unitType: String?,
        fieldSize: String,
        selectedField: String?,
        separator: String
    ): String {
        var result = convertCurrency(input, toCurrency)

        if (!unitType.isNullOrEmpty() && !result.contains(unitType) && !result.contains(separator)) {
            try {
                val amount = replaceNonNumbers(result, "").toDouble()
                val size = fieldSize.toDouble()
                val investment = amount * size
                val rounded = roundToNearestSpecifiedValue(investment, 1000.0)
                result = "${
                    formatNumber(rounded)
                } $currencySymbol $separator $selectedField"
            } catch (ex: Exception) {
                Sentry.captureException(ex)
            }
        }

        return result
    }

    fun formatNumber(number: Double, currency: String? = null): String {
        val formatted = String.format(Locale.US, "%,.0f", number)
        return if (currency != null) "$formatted $currency" else formatted
    }

    fun convertToLocalCurrency(
        amount: Double,
        toCurrency: String,
        nearestRounding: Int = 100
    ): Double {
        val rate = when (toCurrency.uppercase()) {
            "NGN" -> ngnRate
            "TZS" -> tzsRate
            "GHS" -> ghsRate
            else -> 1.0
        }
        val converted = amount * rate
        return roundToNearestSpecifiedValue(converted, nearestRounding.toDouble())
    }

    fun convertToUSD(amount: Double, fromCurrency: String, nearestRounding: Int = 100): Double {
        val converted = try {
            when (fromCurrency.uppercase()) {
                "NGN" -> amount / ngnRate
                "TZS" -> amount / tzsRate
                "GHS" -> amount / ghsRate
                "USD" -> amount
                else -> amount
            }
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            return 0.0
        }

        val rounded = roundToNearestSpecifiedValue(converted, nearestRounding.toDouble())
        return if (rounded < 1) rounded else rounded.toLong().toDouble()
    }

    fun roundToNearestSpecifiedValue(number: Double, nearest: Double): Double {
        return if (nearest < 1 || number < 1)
            roundToNDecimalPlaces(number, 10.0)
        else
            Math.round(number / nearest) * nearest
    }

    fun roundToNDecimalPlaces(number: Double, decimalPlaces: Double): Double {
        return Math.round(number * decimalPlaces) / decimalPlaces
    }

    fun convertToDouble(text: String): Double {
        return text.trim().toDoubleOrNull() ?: 0.0
    }

    fun computeInvestmentAmount(
        localAmount: Double,
        fieldSize: Double,
        fromCurrency: String
    ): Double {
        val total = localAmount * fieldSize
        val usdAmount = convertToUSD(total, fromCurrency)
        return roundToNearestSpecifiedValue(usdAmount, 1000.0)
    }

    fun convertFromAcreToSpecifiedArea(sizeInAcre: Double, unit: String): Double {
        val converted = when (unit.lowercase()) {
            "acre" -> sizeInAcre
            "ha" -> sizeInAcre / baseAcre
            "are" -> sizeInAcre * baseAre
            "sqm" -> sizeInAcre * baseSqm
            else -> sizeInAcre
        }

        return roundToNDecimalPlaces(converted, 100.0)
    }

    fun convertToUnitWeightPrice(price: Double, weight: Int): Double {
        return (price * weight) / 1000
    }

    fun removeLeadingZero(value: Double, pattern: String = "0.#"): String {
        val format = DecimalFormat(pattern)
        format.roundingMode = RoundingMode.CEILING
        return format.format(value)
    }

    fun computeInvestmentForSpecifiedAreaUnit(
        acreInvestmentAmount: Double,
        areaSizeInAcre: Double,
        areaUnit: String
    ): Double {
        return when (areaUnit.lowercase()) {
            "acre" -> acreInvestmentAmount * areaSizeInAcre
            "ha" -> acreInvestmentAmount * baseAcre * areaSizeInAcre
            "sqm" -> areaSizeInAcre * baseSqm * acreInvestmentAmount
            else -> acreInvestmentAmount * areaSizeInAcre
        }
    }
}
