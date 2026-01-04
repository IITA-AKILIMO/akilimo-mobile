package com.akilimo.mobile.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RecommendationFeedback(
    @param:Json(name = "satisfaction_rating")
    val satisfactionRating: Int,

    @param:Json(name = "nps_score")
    val npsScore: Int,

    @param:Json(name = "use_case")
    val useCase: String,

    @param:Json(name = "timestamp")
    val timestamp: Long
)

// Response DTO (optional, adjust based on your API response)
@JsonClass(generateAdapter = true)
data class FeedbackResponse(
    @param:Json(name = "success")
    val success: Boolean,
    @param:Json(name = "message")
    val message: String?
)
