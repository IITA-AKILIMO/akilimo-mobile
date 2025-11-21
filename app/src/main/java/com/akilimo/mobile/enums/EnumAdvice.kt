package com.akilimo.mobile.enums

import android.content.Context
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider

enum class EnumAdvice(private val stringResId: Int) : ILabelProvider {
    FERTILIZER_RECOMMENDATIONS(R.string.lbl_fertilizer_recommendations),
    BEST_PLANTING_PRACTICES(R.string.lbl_best_planting_practices),
    INTERCROPPING_MAIZE(R.string.lbl_intercropping_maize),
    INTERCROPPING_SWEET_POTATO(R.string.lbl_intercropping_sweet_potato),
    SCHEDULED_PLANTING_HIGH_STARCH(R.string.lbl_scheduled_planting_and_high_starch),
    WEED_MANAGEMENT(R.string.lbl_weed_management);

    override fun label(context: Context): String {
        return context.getString(stringResId)
    }
}
