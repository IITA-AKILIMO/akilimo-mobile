package com.akilimo.mobile.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumUnitOfSale(
    val weight: Int,
    private val labelRes: Int?,
    private val textRes: Int?,
    private val plainLabel: String? = null
) : Parcelable {

    NA(0, null, null, "NA"),
    FRESH_COB(1000, null, R.string.lbl_fresh_cob, "fresh cob"),
    ONE_KG(1, R.string.lbl_one_kg_unit, R.string.lbl_one_kg_bag_unit),
    FIFTY_KG(50, R.string.lbl_50_kg_unit, R.string.lbl_50_kg_bag_unit),
    HUNDRED_KG(100, R.string.lbl_100_kg_unit, R.string.lbl_100_kg_bag_unit),
    THOUSAND_KG(1000, R.string.lbl_1000_kg_unit, R.string.lbl_1000_kg_bag_unit);

    fun unitWeight() = weight

    fun unitOfSale(context: Context): String =
        plainLabel ?: labelRes?.let(context::getString) ?: "N/A"

    fun unitOfSaleText(context: Context): String =
        plainLabel ?: textRes?.let(context::getString) ?: "N/A"
}
