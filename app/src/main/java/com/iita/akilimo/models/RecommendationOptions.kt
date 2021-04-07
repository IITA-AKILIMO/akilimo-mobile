package com.iita.akilimo.models

import com.iita.akilimo.utils.enums.EnumAdviceTasks

class RecommendationOptions {
    var recName: String? = null
    var adviceName: EnumAdviceTasks? = null
    var statusImage: Int = 0
    var completed: Boolean = false

    constructor() {}

    constructor(recName: String, adviceName: EnumAdviceTasks, statusImage: Int) {
        this.recName = recName
        this.adviceName = adviceName
        this.statusImage = statusImage
    }
}