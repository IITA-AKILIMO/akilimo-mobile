package com.akilimo.mobile.network

import com.akilimo.mobile.network.dto.AppVersionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/** The API's release gate: the oldest and newest app version code. Public and cheap; the server lets it be cached. */
interface AppVersionApi {

    @GET("v1/app/version")
    suspend fun appVersion(@Query("platform") platform: String = PLATFORM_ANDROID): Response<AppVersionResponse>

    companion object {
        const val PLATFORM_ANDROID = "android"
    }
}
