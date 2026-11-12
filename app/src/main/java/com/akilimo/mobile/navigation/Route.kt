package com.akilimo.mobile.navigation

import kotlinx.serialization.Serializable

// ── Top-level routes ─────────────────────────────────────────────────────────

@Serializable
object LegalWizardRoute

@Serializable
object OnboardingRoute

/** The app home after onboarding: cards for Recommendations, Profile and Settings */
@Serializable
object HubRoute

@Serializable
object ProfileRoute

@Serializable
object FarmRoute

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
object SettingsRoute

// ── Optional farmer account ──────────────────────────────────────────────────

/** Sign in or create an account; once signed in, the account details. The app works without one. */
@Serializable
object AccountRoute

@Serializable
object ForgotPasswordRoute

/** The advice the signed-in farmer saved. */
@Serializable
object SavedAdviceRoute

@Serializable
data class SavedAdviceDetailRoute(val requestId: String)

@Serializable
data class LocationPickerRoute(
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val zoom: Double = 12.0,
    /** ISO alpha-2 code whose supported area is drawn on the map; blank draws none */
    val countryCode: String = "",
)

@Serializable
data class WebViewRoute(val url: String, val title: String)
