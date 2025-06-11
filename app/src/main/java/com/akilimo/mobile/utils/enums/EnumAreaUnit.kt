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
    M2 {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_m2)
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

        M2 -> listOf(
            R.string.quarter_acre_to_m2,
            R.string.half_acre_to_m2,
            R.string.one_acre_to_m2,
            R.string.two_half_acre_to_m2
        )
    }
}
