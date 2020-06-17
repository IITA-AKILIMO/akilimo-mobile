package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.models.Fertilizer
import com.iita.akilimo.models.InterCropFertilizer

interface IDismissListener {
    fun onDismiss(priceSpecified: Boolean, @NonNull fertilizer: Fertilizer, removeSelected: Boolean)
}
