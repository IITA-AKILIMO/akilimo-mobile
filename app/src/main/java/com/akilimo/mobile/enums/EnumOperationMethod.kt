package com.akilimo.mobile.enums

import android.content.Context
import android.os.Parcelable
import com.akilimo.mobile.R
import com.akilimo.mobile.interfaces.ILabelProvider
import kotlinx.parcelize.Parcelize

@Parcelize
enum class EnumOperationMethod(private val stringRes: Int) : Parcelable, ILabelProvider {
    MANUAL(R.string.lbl_manual),
    TRACTOR(R.string.lbl_tractor);

    override fun label(context: Context): String {
        return context.getString(stringRes)
    }
}
