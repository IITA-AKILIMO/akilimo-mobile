package com.akilimo.mobile.network

import com.akilimo.mobile.network.dto.DeviceRegistrationRequest
import com.akilimo.mobile.network.dto.DeviceUnregisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.PUT

/**
 * Registers this phone for push to one person (not only topics). A signed-in farmer's Bearer token, added by
 * [ApiClient], links the phone to the account.
 */
interface DeviceApi {

    companion object {
        private const val CLIENT_ID_HEADER = "Client-Id: akilimo-mobile"
    }

    @Headers(CLIENT_ID_HEADER)
    @PUT("v1/devices")
    suspend fun register(@Body body: DeviceRegistrationRequest): Response<Unit>

    @Headers(CLIENT_ID_HEADER)
    @HTTP(method = "DELETE", path = "v1/devices", hasBody = true)
    suspend fun unregister(@Body body: DeviceUnregisterRequest): Response<Unit>
}
