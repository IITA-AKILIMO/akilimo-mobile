package com.akilimo.mobile.interfaces

import com.akilimo.mobile.entities.Fertilizer

interface IFertilizerDismissListener {
    fun onDismiss(priceSpecified: Boolean, fertilizer: Fertilizer, removeSelected: Boolean)
}
