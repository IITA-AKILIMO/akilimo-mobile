package com.iita.akilimo.models

import com.iita.akilimo.utils.enums.EnumAdviceTasks

class RecommendationOptions {
    var recommendationName: String? = null
    var recCode: EnumAdviceTasks? = null
    var image = 0

    constructor() {}

    constructor(recName: String, recommendationCode: EnumAdviceTasks, imageId: Int) {
        recommendationName = recName
        recCode = recommendationCode
        image = imageId
    }
}