package com.akilimo.mobile.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Farmer accounts: /v1/account (see docs/FARMER_ACCOUNTS_API.md in new-akilimo-api)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "name") val name: String?,
    @param:Json(name = "consent") val consent: Boolean,
    @param:Json(name = "device_token") val deviceToken: String?,
)

@JsonClass(generateAdapter = true)
data class AccountLoginRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "device_token") val deviceToken: String?,
)

/** Social sign-in: the provider's OAuth access token (not an ID token). */
@JsonClass(generateAdapter = true)
data class SocialSignInRequest(
    @param:Json(name = "access_token") val accessToken: String,
    @param:Json(name = "consent") val consent: Boolean,
    @param:Json(name = "device_token") val deviceToken: String?,
)

@JsonClass(generateAdapter = true)
data class ConsentRequest(
    @param:Json(name = "consent") val consent: Boolean = true,
    @param:Json(name = "device_token") val deviceToken: String?,
)

@JsonClass(generateAdapter = true)
data class VerifyEmailRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "code") val code: String,
)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(@param:Json(name = "email") val email: String)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "code") val code: String,
    @param:Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class AccountUserDto(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "name") val name: String?,
    @param:Json(name = "email") val email: String,
    @param:Json(name = "email_verified") val emailVerified: Boolean = false,
    @param:Json(name = "consent_required") val consentRequired: Boolean = false,
)

/** The answer of register, login and social sign-in (the same shape as the staff login, plus consent info). */
@JsonClass(generateAdapter = true)
data class AuthResponse(
    @param:Json(name = "token_type") val tokenType: String,
    @param:Json(name = "token") val token: String,
    @param:Json(name = "expires_at") val expiresAt: String,
    @param:Json(name = "consent_required") val consentRequired: Boolean = false,
    @param:Json(name = "claimed_requests") val claimedRequests: Int = 0,
    @param:Json(name = "user") val user: AccountUserDto,
)

@JsonClass(generateAdapter = true)
data class MessageResponse(
    @param:Json(name = "message") val message: String? = null,
    /** Consent: how many earlier requests of this phone were linked to the account. */
    @param:Json(name = "claimed_requests") val claimedRequests: Int = 0,
    /** Delete all saved advice: how many items the server removed. */
    @param:Json(name = "deleted") val deleted: Int = 0,
)

/** A saved recommendation in the history list; the detail view also carries [result]. */
@JsonClass(generateAdapter = true)
data class HistoryItemDto(
    @param:Json(name = "request_id") val requestId: String,
    @param:Json(name = "country_code") val countryCode: String? = null,
    @param:Json(name = "use_case") val useCase: String? = null,
    @param:Json(name = "created_at") val createdAt: String? = null,
    @param:Json(name = "result") val result: HistoryResultDto? = null,
)

/** The stored answer of the compute service: `{status, version, data: {rec_type, recommendation, ...}}`. */
@JsonClass(generateAdapter = true)
data class HistoryResultDto(
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "data") val data: RecommendationData? = null,
)

@JsonClass(generateAdapter = true)
data class HistoryDetailResponse(@param:Json(name = "data") val data: HistoryItemDto)

/** The ids of all saved advice, newest first (at most 5000; [IdsMeta.truncated] says when the list was cut). */
@JsonClass(generateAdapter = true)
data class HistoryIdsResponse(
    @param:Json(name = "data") val data: List<String>,
    @param:Json(name = "meta") val meta: IdsMeta? = null,
)

@JsonClass(generateAdapter = true)
data class IdsMeta(
    @param:Json(name = "count") val count: Int = 0,
    @param:Json(name = "truncated") val truncated: Boolean = false,
)
