package com.iita.akilimo.rest

import com.android.volley.DefaultRetryPolicy
import java.util.*

class RestParameters(
    val url: String,
    val countryCode: String? = null
) {
    var apiToken: String = "akilimo"
    var userId: String = "akilimo"

    var operationType: String? = null
    var operationName: String? = null
    var initialTimeout: Int = 40000
    var maxRetries: Int = 3
    var backoffMultiplier: Float = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    var context: String = "dev"
    var appVersion: String = "0.0.0"
    var locale: Locale = Locale.ENGLISH
}
