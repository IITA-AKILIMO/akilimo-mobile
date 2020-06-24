package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.entities.InterCropFertilizer

@Deprecated("To be removed ASAP")
interface IDismissIntercropListener {
    fun onDismiss(
        priceSpecified: Boolean,
        @NonNull fertilizer: InterCropFertilizer,
        removeSelected: Boolean
    )
}
