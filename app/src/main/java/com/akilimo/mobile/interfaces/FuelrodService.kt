package com.akilimo.mobile.interfaces

import com.akilimo.mobile.data.RemoteConfigResponse
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


object FuelrodApi {
    val apiService: FuelrodService by lazy {
        RetrofitManager.fuelrod().create(FuelrodService::class.java)
    }
}

interface FuelrodService {
    @GET("v1/remote-config/app-name/{app}")
    fun readConfig(@Path("app") app: String): Call<List<RemoteConfigResponse>>
}
