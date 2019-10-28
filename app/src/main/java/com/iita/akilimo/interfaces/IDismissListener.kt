package com.iita.akilimo.interfaces

import com.iita.akilimo.models.Fertilizer

interface IDismissListener {
    fun onDismiss(priceSpecified: Boolean, fertilizer: Fertilizer, removeSelected: Boolean)
}
