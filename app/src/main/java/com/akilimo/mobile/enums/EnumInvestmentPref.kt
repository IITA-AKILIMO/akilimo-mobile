package com.akilimo.mobile.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumInvestmentPref(private val stringResId: Int, private val riskLevel: Int) :
    Parcelable, ILabelProvider {
    Rarely(R.string.lbl_rarely, 0),
    Sometimes(R.string.lbl_sometimes, 1),
    Often(R.string.lbl_often, 2),
    Prompt(R.string.lbl_investment_pref_prompt, -1);

    override fun label(context: Context): String {
        return context.getString(stringResId)
    }

    fun riskLevel(): Int {
        return riskLevel
    }
}
