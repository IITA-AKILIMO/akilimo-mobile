package com.iita.akilimo.rest

import com.android.volley.DefaultRetryPolicy
import java.util.*

class RestParameters(
    val url: String,
    val countryCode: String? = null
) {
    var apiToken: String = "akilimo"
    var userId: String = "akilimo"

    @Deprecated("We no longer need to pass this filtering will happen on app side")
    var useCase: String? = null

    var operationType: String? = null
    var operationName: String? = null
    var initialTimeout: Int = 40000
    var maxRetries: Int = 3
    var backoffMultiplier: Float = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    var context: String = "dev"
    var appVersion: String = "0.0.0"
    var locale: Locale = Locale.ENGLISH
}
