package com.iita.akilimo.interfaces

import androidx.annotation.NonNull
import com.iita.akilimo.entities.Fertilizer

interface IDismissListener {
    fun onDismiss(priceSpecified: Boolean, @NonNull fertilizer: Fertilizer, removeSelected: Boolean)
}
