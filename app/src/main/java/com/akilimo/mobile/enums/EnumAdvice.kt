package com.akilimo.mobile.enums

import android.content.Context
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.ILabelProvider

enum class EnumAdvice(private val stringResId: Int, private val titleResId: Int) : ILabelProvider {
    FERTILIZER_RECOMMENDATIONS(R.string.lbl_fertilizer_recommendations, R.string.lbl_fertilizer_rec),
    BEST_PLANTING_PRACTICES(R.string.lbl_best_planting_practices, R.string.lbl_best_planting_practices_title),
    INTERCROPPING_MAIZE(R.string.lbl_intercropping_maize, R.string.lbl_intercropping_maize_title),
    INTERCROPPING_SWEET_POTATO(R.string.lbl_intercropping_sweet_potato, R.string.lbl_intercropping_sweet_potato_title),
    SCHEDULED_PLANTING_HIGH_STARCH(R.string.lbl_scheduled_planting_and_high_starch, R.string.lbl_scheduled_planting_rec);
//    WEED_MANAGEMENT(R.string.lbl_weed_management, R.string.lbl_weed_management);

    override fun label(context: Context): String = context.getString(stringResId)
    fun title(context: Context): String = context.getString(titleResId)
}
