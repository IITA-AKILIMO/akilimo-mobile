package com.akilimo.mobile.network

import com.akilimo.mobile.network.dto.AccountLoginRequest
import com.akilimo.mobile.network.dto.AuthResponse
import com.akilimo.mobile.network.dto.ConsentRequest
import com.akilimo.mobile.network.dto.DeleteItemsRequest
import com.akilimo.mobile.network.dto.ForgotPasswordRequest
import com.akilimo.mobile.network.dto.HistoryDetailResponse
import com.akilimo.mobile.network.dto.HistoryIdsResponse
import com.akilimo.mobile.network.dto.HistoryItemDto
import com.akilimo.mobile.network.dto.MessageResponse
import com.akilimo.mobile.network.dto.PagedResponse
import com.akilimo.mobile.network.dto.RegisterRequest
import com.akilimo.mobile.network.dto.ResetPasswordRequest
import com.akilimo.mobile.network.dto.SocialSignInRequest
import com.akilimo.mobile.network.dto.VerifyEmailRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Optional farmer accounts. Signed-in calls carry the farmer's Bearer token (added by [ApiClient]). */
interface AccountApi {

    companion object {
        private const val CLIENT_ID_HEADER = "Client-Id: akilimo-mobile"
    }

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/login")
    suspend fun login(@Body body: AccountLoginRequest): Response<AuthResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/social/{provider}")
    suspend fun social(@Path("provider") provider: String, @Body body: SocialSignInRequest): Response<AuthResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/consent")
    suspend fun consent(@Body body: ConsentRequest): Response<MessageResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/logout")
    suspend fun logout(): Response<MessageResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/email/verify")
    suspend fun verifyEmail(@Body body: VerifyEmailRequest): Response<MessageResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/email/resend")
    suspend fun resendVerification(): Response<MessageResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/password/forgot")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/password/reset")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<MessageResponse>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/account/history")
    suspend fun history(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 50,
    ): Response<PagedResponse<HistoryItemDto>>

    /** The ids of all saved items: the phone drops its copies of items deleted elsewhere, and finds what it is missing. */
    @Headers(CLIENT_ID_HEADER)
    @GET("v1/account/history/ids")
    suspend fun historyIds(): Response<HistoryIdsResponse>

    @Headers(CLIENT_ID_HEADER)
    @GET("v1/account/history/{requestId}")
    suspend fun historyItem(@Path("requestId") requestId: String): Response<HistoryDetailResponse>

    @Headers(CLIENT_ID_HEADER)
    @DELETE("v1/account/history/{requestId}")
    suspend fun deleteHistoryItem(@Path("requestId") requestId: String): Response<MessageResponse>

    /** Deletes several saved items in one request (up to 500): swipe and multi-select in the app. */
    @Headers(CLIENT_ID_HEADER)
    @POST("v1/account/history/delete")
    suspend fun deleteHistoryItems(@Body body: DeleteItemsRequest): Response<MessageResponse>

    /** Deletes all saved advice and keeps the account. */
    @Headers(CLIENT_ID_HEADER)
    @DELETE("v1/account/history")
    suspend fun deleteAllHistory(): Response<MessageResponse>

    /** Deletes the account. Past requests stay as anonymous statistics (the link to the person is removed). */
    @Headers(CLIENT_ID_HEADER)
    @DELETE("v1/account")
    suspend fun deleteAccount(): Response<MessageResponse>
}
