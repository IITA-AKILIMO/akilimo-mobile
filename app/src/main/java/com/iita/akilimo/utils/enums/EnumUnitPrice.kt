package com.iita.akilimo.utils.enums

import com.iita.akilimo.utils.MathHelper

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
    PRICE_RANGE_ONE {
        var unitPriceLower = 20.0
        var unitPriceUpper = 30.0
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            val usd = (unitPriceLower + unitPriceUpper) / 2
            return convertCurrency(usd, toCurrency, mathHelper)
        }

        override fun unitPricePerTonneLower(): Double {
            return unitPriceLower
        }

        override fun unitPricePerTonneUpper(): Double {
            return unitPriceUpper
        }
    },
    PRICE_RANGE_TWO {
        var unitPriceLower = 31.0
        var unitPriceUpper = 50.0
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            val usd = (unitPriceLower + unitPriceUpper) / 2
            return convertCurrency(usd, toCurrency, mathHelper)
        }

        override fun unitPricePerTonneLower(): Double {
            return unitPriceLower
        }

        override fun unitPricePerTonneUpper(): Double {
            return unitPriceUpper
        }
    },
    PRICE_RANGE_THREE {
        var unitPriceLower = 51.0
        var unitPriceUpper = 100.0
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            val usd = (unitPriceLower + unitPriceUpper) / 2
            return convertCurrency(usd, toCurrency, mathHelper)
        }

        override fun unitPricePerTonneLower(): Double {
            return unitPriceLower
        }

        override fun unitPricePerTonneUpper(): Double {
            return unitPriceUpper
        }
    },
    PRICE_RANGE_FOUR {
        var unitPriceLower = 101.0
        var unitPriceUpper = 150.0
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            val usd = (unitPriceLower + unitPriceUpper) / 2
            return convertCurrency(usd, toCurrency, mathHelper)
        }

        override fun unitPricePerTonneLower(): Double {
            return unitPriceLower
        }

        override fun unitPricePerTonneUpper(): Double {
            return unitPriceUpper
        }
    },
    PRICE_RANGE_FIVE {
        var unitPriceLower = 151.0
        var unitPriceUpper = 200.0
        override fun convertToLocalCurrency(
            toCurrency: String?,
            mathHelper: MathHelper?
        ): Double {
            val usd = (unitPriceLower + unitPriceUpper) / 2
            return convertCurrency(usd, toCurrency, mathHelper)
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
            mathHelper: MathHelper?
        ): Double {
            var mathHelper = mathHelper
            if (mathHelper == null) {
                mathHelper = MathHelper()
            }
            return mathHelper.convertToLocalCurrency(amount, toCurrency)
        }
    }
}