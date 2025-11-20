package com.akilimo.mobile.enums

import android.content.Context
import com.akilimo.mobile.R

enum class EnumAreaUnit(private val labelRes: Int) {
    ACRE(R.string.lbl_acre),
    HA(R.string.lbl_ha),
    M2(R.string.lbl_m2),
    ARE(R.string.lbl_are);

    fun label(context: Context): String = labelRes.let { context.getString(it) }
}
