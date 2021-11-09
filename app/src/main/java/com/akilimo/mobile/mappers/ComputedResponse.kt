package com.akilimo.mobile.mappers

class ComputedResponse {
    var computedTitle: String? = null
    var computedRecommendation: String? = null

    fun createObject(title: String?, body: String?): ComputedResponse {
        computedTitle = title
        computedRecommendation = body
        return this
    }

}
