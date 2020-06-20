package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.models.Fertilizer

interface IDismissListener {
    fun onDismiss(priceSpecified: Boolean, @NonNull fertilizer: Fertilizer, removeSelected: Boolean)
}
