package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.entities.Fertilizer

interface IFertilizerDismissListener {
    fun onDismiss(priceSpecified: Boolean, @NonNull fertilizer: Fertilizer, removeSelected: Boolean)
}
