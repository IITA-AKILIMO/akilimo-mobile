package com.akilimo.mobile.enums

import android.content.Context
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider

enum class EnumAdvice(private val stringResId: Int) : ILabelProvider {
    FR(R.string.lbl_fertilizer_recommendations),
    BPP(R.string.lbl_best_planting_practices),
    IC_MAIZE(R.string.lbl_intercropping_maize),
    IC_SWEET_POTATO(R.string.lbl_intercropping_sweet_potato),
    SPH(R.string.lbl_scheduled_planting_and_harvest),
    WM(R.string.lbl_weed_management);

    override fun label(context: Context): String {
        return context.getString(stringResId)
    }
}
