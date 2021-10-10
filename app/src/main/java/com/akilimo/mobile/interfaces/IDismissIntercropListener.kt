package com.akilimo.mobile.interfaces

import androidx.annotation.NonNull
import com.akilimo.mobile.entities.InterCropFertilizer

@Deprecated("To be removed ASAP")
interface IDismissIntercropListener {
    fun onDismiss(
        priceSpecified: Boolean,
        @NonNull fertilizer: InterCropFertilizer,
        removeSelected: Boolean
    )
}
