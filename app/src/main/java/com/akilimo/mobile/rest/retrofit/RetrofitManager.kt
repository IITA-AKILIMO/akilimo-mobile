package com.akilimo.mobile.rest.retrofit

import retrofit2.Retrofit

object RetrofitManager {
    private var akilimoRetrofit: Retrofit? = null
    private var fuelrodRetroFit: Retrofit? = null

    fun init(akilimoUrl: String, fuelrodUrl: String) {
        akilimoRetrofit = RetroFitFactory.create(akilimoUrl, 130)
        fuelrodRetroFit = RetroFitFactory.create(fuelrodUrl)
    }

    fun akilimo(): Retrofit =
        akilimoRetrofit ?: throw IllegalStateException("Akilimo Retrofit not initialized")

    fun fuelrod(): Retrofit =
        fuelrodRetroFit ?: throw IllegalStateException("Fuelrod Retrofit not initialized")
}