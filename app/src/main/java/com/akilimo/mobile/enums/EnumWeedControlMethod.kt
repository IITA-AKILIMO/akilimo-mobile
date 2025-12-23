package com.akilimo.mobile.enums

import android.os.Parcelable
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumWeedControlMethod(private val stringResId: Int) : Parcelable, ILabelProvider {
    MANUAL(R.string.lbl_manual_weed_control),
    HERBICIDE(R.string.lbl_herbicide_weed_control),
    HERBICIDE_AND_MANUAL(R.string.lbl_both_weed_control);

    override fun label(context: android.content.Context): String {
        return context.getString(stringResId)
    }
}
