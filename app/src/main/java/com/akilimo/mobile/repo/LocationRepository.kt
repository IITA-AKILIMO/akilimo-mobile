package com.akilimo.mobile.repo

fun interface LocationRepository {
    suspend fun reverseGeocode(
        lat: Double,
        lon: Double
    ): Pair<String, String>? // countryCode, countryName
}