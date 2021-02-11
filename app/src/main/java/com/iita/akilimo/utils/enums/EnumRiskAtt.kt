package com.iita.akilimo.utils.enums

import android.content.Context
import com.iita.akilimo.R
import org.jetbrains.annotations.NotNull

enum class EnumRiskAtt {
    Never {
        override fun riskName(context: Context): String {
            return context.getString(R.string.lbl_never)
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