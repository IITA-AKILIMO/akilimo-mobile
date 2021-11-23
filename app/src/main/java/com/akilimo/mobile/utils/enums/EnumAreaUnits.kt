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

    },
    M2 {
        override fun unitName(context: Context): String {
            return "ha"
        }

    },
    ARE {
        override fun unitName(context: Context): String {
            return context.getString(R.string.lbl_are)
        }

    };

    abstract fun unitName(context: Context): @NotNull String
}
