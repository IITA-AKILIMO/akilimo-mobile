package com.akilimo.mobile.enums

import android.content.Context
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider

enum class EnumAreaUnit(private val stringRes: Int) : ILabelProvider {
    ACRE(R.string.lbl_acre),
    HA(R.string.lbl_ha),
    M2(R.string.lbl_m2),
    ARE(R.string.lbl_are);

    override fun label(context: Context): String = stringRes.let { context.getString(it) }
}
