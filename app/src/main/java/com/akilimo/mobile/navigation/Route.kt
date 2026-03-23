package com.akilimo.mobile.navigation

import kotlinx.serialization.Serializable

// ── Top-level routes ─────────────────────────────────────────────────────────

@Serializable
object OnboardingRoute

@Serializable
object RecommendationsRoute

// ── Recommendation sub-routes ────────────────────────────────────────────────

@Serializable
object FrRoute          // Fertilizer Recommendations

@Serializable
object BppRoute         // Best Planting Practices

@Serializable
object SphRoute         // Scheduled Planting High-Starch

@Serializable
object IcMaizeRoute     // Intercropping with Maize

@Serializable
object IcSweetPotatoRoute // Intercropping with Sweet Potato

// ── Use-case form routes ─────────────────────────────────────────────────────

@Serializable
object FertilizersRoute

@Serializable
object InterCropFertilizersRoute

@Serializable
object SweetPotatoInterCropFertilizersRoute

@Serializable
object InvestmentAmountRoute

@Serializable
object CassavaMarketRoute

@Serializable
object CassavaYieldRoute

@Serializable
object DatesRoute

@Serializable
object ManualTillageCostRoute

@Serializable
object TractorAccessRoute

@Serializable
object WeedControlCostsRoute

@Serializable
object MaizeMarketRoute

@Serializable
object MaizePerformanceRoute

@Serializable
object SweetPotatoMarketRoute

@Serializable
data class GetRecommendationRoute(val useCaseCode: String)

// ── Settings ─────────────────────────────────────────────────────────────────

@Serializable
object UserSettingsRoute

@Serializable
data class LocationPickerRoute(val lat: Double = 0.0, val lon: Double = 0.0, val zoom: Double = 12.0)
