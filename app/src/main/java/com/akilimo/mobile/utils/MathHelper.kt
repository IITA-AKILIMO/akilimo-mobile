package com.akilimo.mobile.utils

import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumUnitOfSale
import java.util.Locale

object MathHelper {
    fun convertFromAcres(acres: Double, targetUnit: EnumAreaUnit): Double {
        return when (targetUnit) {
            EnumAreaUnit.HA -> acres * 0.404686
            EnumAreaUnit.M2 -> acres * 4046.86
            EnumAreaUnit.ARE -> acres * 40.4686
            else -> acres
        }
    }

    /** Convert an area between any two units (via acres). */
    fun convertArea(value: Double, from: EnumAreaUnit, to: EnumAreaUnit): Double {
        if (from == to) return value
        val acres = value / convertFromAcres(1.0, from)
        return convertFromAcres(acres, to)
    }

    fun computeUnitPrice(avgPrice: Double, unitOfSaleEnum: EnumUnitOfSale): Double {
        return when (unitOfSaleEnum) {
            EnumUnitOfSale.FRESH_COB -> avgPrice
            else -> (avgPrice * unitOfSaleEnum.unitWeight()) / 1000
        }
    }

    fun format(amount: Double): String =
        if (amount % 1.0 == 0.0) String.format(Locale.US, "%,.0f", amount)
        else String.format(Locale.US, "%,.2f", amount)
}