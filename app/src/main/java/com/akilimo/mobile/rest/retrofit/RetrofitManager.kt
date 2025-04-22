package com.akilimo.mobile.rest.retrofit

import retrofit2.Retrofit

object RetrofitManager {
    private var akilimoRetrofit: Retrofit? = null
    private var fuelrodRetroFit: Retrofit? = null

    fun init(akilimoUrl: String, otherUrl: String) {
        akilimoRetrofit = RetroFitFactory.create(akilimoUrl)
        fuelrodRetroFit = RetroFitFactory.create(otherUrl)
    }

    fun akilimo(): Retrofit =
        akilimoRetrofit ?: throw IllegalStateException("Akilimo Retrofit not initialized")

    fun fuelrod(): Retrofit =
        fuelrodRetroFit ?: throw IllegalStateException("Other Retrofit not initialized")
}