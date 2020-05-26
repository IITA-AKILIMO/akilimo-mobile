package com.iita.akilimo.utils.enums

import com.iita.akilimo.utils.MathHelper

@Deprecated("Consider removing in future version")
enum class EnumUnitPrice {
    UNKNOWN {
        var unitPriceLower = -1.0
        var unitPriceUpper = -1.0
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            return (-1).toDouble()
        }

        override fun unitPricePerTonneLower(): Double {
            return unitPriceLower
        }

        override fun unitPricePerTonneUpper(): Double {
            return unitPriceUpper
        }
    },
    PRICE_EXACT {
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            return 0.0
        }

        override fun unitPricePerTonneLower(): Double {
            return 0.0
        }

        override fun unitPricePerTonneUpper(): Double {
            return 0.0
        }
    };

    abstract fun convertToLocalCurrency(
        toCurrency: String?,
        mathHelper: MathHelper?
    ): Double

    abstract fun unitPricePerTonneLower(): Double
    abstract fun unitPricePerTonneUpper(): Double

    companion object {
        private fun convertCurrency(
            amount: Double,
            toCurrency: String?,
            mathHelper: MathHelper
        ): Double {
            return mathHelper.convertToLocalCurrency(amount, toCurrency)
        }
    }
}