package com.akilimo.mobile.interfaces

fun interface LocationProvider {
    fun getCurrentLocation(callback: (Triple<Double, Double, Double>?) -> Unit)
}