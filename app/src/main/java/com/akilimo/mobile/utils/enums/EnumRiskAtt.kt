package com.akilimo.mobile.utils.enums

import android.content.Context
import com.akilimo.mobile.R
import org.jetbrains.annotations.NotNull

enum class EnumRiskAtt {
    Rarely {
        override fun riskName(context: Context): String {
            return context.getString(R.string.lbl_rarely)
        }
    },
    Sometimes {
        override fun riskName(context: Context): String {
            return context.getString(R.string.lbl_sometimes)
        }
    },
    Often {
        override fun riskName(context: Context): String {
            return context.getString(R.string.lbl_often)
        }
    };

    abstract fun riskName(context: Context): @NotNull String
}
