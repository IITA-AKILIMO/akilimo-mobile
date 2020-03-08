package com.iita.akilimo.utils.enums

import com.iita.akilimo.utils.MathHelper

enum class EnumPotatoUnitPrice {
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
        var unitPriceLower = 47.62
        var unitPriceUpper = 49.79
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
        var unitPriceLower = 49.79
        var unitPriceUpper = 51.95
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
        var unitPriceLower = 51.95
        var unitPriceUpper = 56.28
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
            val resp = when (mathHelper) {
                null -> {
                    val mh = MathHelper()
                    mh.convertToLocalCurrency(amount, toCurrency)
                }
                else -> {
                    mathHelper.convertToLocalCurrency(amount, toCurrency)
                }
            }

            return resp
        }
    }
}