package com.akilimo.mobile.utils.enums

import android.content.Context
import com.akilimo.mobile.R
import org.jetbrains.annotations.NotNull

enum class EnumAreaUnit {
    ACRE {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_acre)
        }
    },
    HA {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_ha)
        }

    },
    SQM {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_sqm)
        }

    },
    ARE {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_are)
        }

    };

    abstract fun unitName(context: Context): @NotNull String

    fun labelResIds(): List<Int> = when (this) {
        ACRE -> listOf(
            R.string.quarter_acre,
            R.string.half_acre,
            R.string.one_acre,
            R.string.two_half_acres
        )

        HA -> listOf(
            R.string.quarter_acre_to_ha,
            R.string.half_acre_to_ha,
            R.string.one_acre_to_ha,
            R.string.two_half_acre_to_ha
        )

        ARE -> listOf(
            R.string.quarter_acre_to_are,
            R.string.half_acre_to_are,
            R.string.one_acre_to_are,
            R.string.two_half_acre_to_are
        )

        SQM -> listOf(
            R.string.quarter_acre_to_m2,
            R.string.half_acre_to_m2,
            R.string.one_acre_to_m2,
            R.string.two_half_acre_to_m2
        )
    }

    fun yieldLabelIds(): List<Int> = when (this) {
        ACRE -> listOf(
            R.string.yield_less_than_3_tonnes_per_acre,
            R.string.yield_3_to_6_tonnes_per_acre,
            R.string.yield_6_to_9_tonnes_per_acre,
            R.string.yield_9_to_12_tonnes_per_acre,
            R.string.yield_more_than_12_tonnes_per_acre
        )

        HA -> listOf(
            R.string.yield_less_than_3_tonnes_per_hectare,
            R.string.yield_3_to_6_tonnes_per_hectare,
            R.string.yield_6_to_9_tonnes_per_hectare,
            R.string.yield_9_to_12_tonnes_per_hectare,
            R.string.yield_more_than_12_tonnes_per_hectare
        )

        ARE -> listOf(
            R.string.yield_less_than_3_tonnes_per_are,
            R.string.yield_3_to_6_tonnes_per_are,
            R.string.yield_6_to_9_tonnes_per_are,
            R.string.yield_9_to_12_tonnes_per_are,
            R.string.yield_more_than_12_tonnes_per_are
        )

        SQM -> listOf(
            R.string.yield_less_than_3_tonnes_per_meter,
            R.string.yield_3_to_6_tonnes_per_meter,
            R.string.yield_6_to_9_tonnes_per_meter,
            R.string.yield_9_to_12_tonnes_per_meter,
            R.string.yield_more_than_12_tonnes_per_meter
        )
    }
}
