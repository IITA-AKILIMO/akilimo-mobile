package com.iita.akilimo.rest

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
    var initialTimeout: Int = 30000
}
