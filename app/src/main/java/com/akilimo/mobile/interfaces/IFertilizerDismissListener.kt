package com.akilimo.mobile.interfaces

import androidx.annotation.NonNull
import com.akilimo.mobile.entities.Fertilizer

interface IFertilizerDismissListener {
    fun onDismiss(priceSpecified: Boolean, @NonNull fertilizer: Fertilizer, removeSelected: Boolean)
}
