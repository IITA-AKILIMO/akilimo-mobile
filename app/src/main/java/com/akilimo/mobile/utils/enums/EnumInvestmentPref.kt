package com.akilimo.mobile.utils.enums

import android.content.Context
import com.akilimo.mobile.R
import org.jetbrains.annotations.NotNull

enum class EnumInvestmentPref {
    Rarely {
        override fun prefName(context: Context): String {
            return context.getString(R.string.lbl_rarely)
        }
    },
    Sometimes {
        override fun prefName(context: Context): String {
            return context.getString(R.string.lbl_sometimes)
        }
    },
    Often {
        override fun prefName(context: Context): String {
            return context.getString(R.string.lbl_often)
        }
    };

    abstract fun prefName(context: Context): @NotNull String
}
