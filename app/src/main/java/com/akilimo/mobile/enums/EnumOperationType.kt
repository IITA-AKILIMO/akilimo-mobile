package com.akilimo.mobile.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumOperationType(private val stringRes: Int) : Parcelable, ILabelProvider {
    PLOUGHING(R.string.lbl_ploughing),
    HARROWING(R.string.lbl_harrowing),
    RIDGING(R.string.lbl_ridging),
    WEEDING(R.string.lbl_weeding);

    override fun label(context: Context): String {
        return context.getString(stringRes)
    }
}
