package com.akilimo.mobile.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserFeedBackRequest(
    @param:Json(name = "akilimo_usage")
    val akilimoUsage: String,

    @param:Json(name = "user_type")
    val userType: String,

    @param:Json(name = "device_token")
    val deviceToken: String,

    @param:Json(name = "device_language")
    val deviceLanguage: String,

    @param:Json(name = "satisfaction_rating")
    val satisfactionRating: Int,

    @param:Json(name = "nps_score")
    val npsScore: Int,

    @param:Json(name = "use_case")
    val useCase: String,
)

// Response DTO (optional, adjust based on your API response)
@JsonClass(generateAdapter = true)
data class FeedbackResponse(
    @param:Json(name = "data")
    val feedback: Feedback
)

@JsonClass(generateAdapter = true)
data class PaginatedFeedbackResponse(
    @param:Json(name = "data")
    val data: List<Feedback>,

    @param:Json(name = "links")
    val links: PaginationLinks,

    @param:Json(name = "meta")
    val meta: PaginationMeta
)

@JsonClass(generateAdapter = true)
data class Feedback(
    @param:Json(name = "id")
    val id: Long,

    @param:Json(name = "recommendation_id")
    val recommendationId: Long,

    @param:Json(name = "akilimo_usage")
    val akilimoUsage: String,

    @param:Json(name = "use_case")
    val useCase: String?,

    @param:Json(name = "user_type")
    val userType: String,

    @param:Json(name = "recommendation_rating")
    val recommendationRating: Int,

    @param:Json(name = "useful_rating")
    val usefulRating: Int,

    @param:Json(name = "language")
    val language: String,

    @param:Json(name = "device_token")
    val deviceToken: String,

    @param:Json(name = "created_at")
    val createdAt: String,

    @param:Json(name = "updated_at")
    val updatedAt: String
)