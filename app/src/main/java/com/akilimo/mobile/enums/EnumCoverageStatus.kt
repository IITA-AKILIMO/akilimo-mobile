package com.akilimo.mobile.enums

/**
 * Whether a farm location is inside the area AKILIMO can give recommendations for.
 *
 * [UNKNOWN] means the supported area could not be loaded (never fetched, offline, or the API has
 * none for the country). Like the API, treat it as "allowed", never as outside.
 */
enum class EnumCoverageStatus { UNKNOWN, INSIDE, OUTSIDE }
