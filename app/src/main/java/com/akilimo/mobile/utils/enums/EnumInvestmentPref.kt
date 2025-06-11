package com.akilimo.mobile.utils.enums

import android.content.Context
import androidx.annotation.StringRes
import com.akilimo.mobile.R

enum class EnumInvestmentPref(@StringRes private val stringResId: Int, private val riskAtt: Int) {
    RARELY(R.string.lbl_rarely, 0),
    SOMETIMES(R.string.lbl_sometimes, 1),
    OFTEN(R.string.lbl_often, 2);

    fun prefName(context: Context): String {
        return context.getString(stringResId)
    }

    fun riskAtt(): Int {
        return riskAtt
    }
}