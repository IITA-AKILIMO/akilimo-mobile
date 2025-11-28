package com.akilimo.mobile.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumUnitOfSale(
    val weight: Int,
    val isUniversal: Boolean,
    private val labelRes: Int,
    private val textRes: Int
) : Parcelable, ILabelProvider {
    // Crop-specific unit (only maize fresh cob)
    FRESH_COB(
        weight = 1,
        isUniversal = false,
        labelRes = R.string.lbl_fresh_cob,
        textRes = R.string.lbl_fresh_cob
    ),

    // Universal units (apply to all produce types)
    ONE_KG(
        weight = 1,
        isUniversal = true,
        labelRes = R.string.lbl_one_kg_unit,
        textRes = R.string.per_kg_sale_unit
    ),
    FIFTY_KG(
        weight = 50,
        isUniversal = true,
        labelRes = R.string.lbl_50_kg_unit,
        textRes = R.string.per_50_kg_sale_unit
    ),
    HUNDRED_KG(
        weight = 100,
        isUniversal = true,
        labelRes = R.string.lbl_100_kg_unit,
        textRes = R.string.per_100_kg_sale_unit
    ),
    THOUSAND_KG(
        weight = 1000,
        isUniversal = true,
        labelRes = R.string.lbl_1000_kg_unit,
        textRes = R.string.per_tonne_sale_unit
    ),
    PICKUP_LOAD(
        weight = 1,
        isUniversal = false,
        labelRes = R.string.per_pick_up_load_sale_unit,
        textRes = R.string.per_pick_up_load_sale_unit
    );

    fun unitWeight(): Int = weight

    fun unitOfSale(context: Context): String =
        labelRes.let(context::getString)

    override fun label(context: Context): String =
        textRes.let(context::getString)
}