package com.akilimo.mobile.utils.enums

import android.content.Context
import com.akilimo.mobile.R
import org.jetbrains.annotations.NotNull

enum class EnumAreaUnits {
    ACRE {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_acre)
        }
    },
    HA {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_ha)
        }

    };

    abstract fun unitName(context: Context): @NotNull String
}