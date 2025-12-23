package com.akilimo.mobile.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider
import kotlinx.parcelize.Parcelize

@Suppress("unused")
@Parcelize
enum class EnumAdviceTask(private val labelResId: Int) : ILabelProvider, Parcelable {
    PLANTING_AND_HARVEST(R.string.lbl_consider_alternative_planting),
    AVAILABLE_FERTILIZERS(R.string.lbl_available_fertilizers),
    AVAILABLE_FERTILIZERS_CIS(R.string.lbl_available_fertilizers),
    AVAILABLE_FERTILIZERS_CIM(R.string.lbl_available_fertilizers),
    INVESTMENT_AMOUNT(R.string.lbl_investment_amount),
    CURRENT_CASSAVA_YIELD(R.string.lbl_typical_yield),
    MAIZE_PERFORMANCE(R.string.lbl_maize_performance),
    CASSAVA_MARKET_OUTLET(R.string.lbl_market_outlet),
    MAIZE_MARKET_OUTLET(R.string.lbl_market_outlet_maize),
    SWEET_POTATO_MARKET_OUTLET(R.string.lbl_market_outlet_sweet_potato),
    TILLAGE_OPERATIONS(R.string.lbl_tillage_operations),
    COST_OF_WEED_CONTROL(R.string.lbl_cost_of_weed_control),
    MANUAL_TILLAGE_COST(R.string.lbl_cost_of_manual_tillage),
    TRACTOR_ACCESS(R.string.lbl_tractor_access), ;

    /**
     * Returns the localized label string for this task.
     */
    override fun label(context: Context): String = context.getString(labelResId)
}
