package com.akilimo.mobile.interfaces

import com.akilimo.mobile.data.RemoteConfigResponse
import com.akilimo.mobile.rest.retrofit.RetrofitManager
import retrofit2.http.GET
import retrofit2.http.Path

class FuelrodApi(private val retrofitManager: RetrofitManager) {

    private val service: FuelrodService by lazy {
        retrofitManager.fuelrod().create(FuelrodService::class.java)
    }

    suspend fun readConfig(app: String): List<RemoteConfigResponse> {
        return service.readConfig(app)
    }
}


interface FuelrodService {
    @GET("v1/remote-config/app-name/{app}")
    suspend fun readConfig(@Path("app") app: String): List<RemoteConfigResponse>
}
