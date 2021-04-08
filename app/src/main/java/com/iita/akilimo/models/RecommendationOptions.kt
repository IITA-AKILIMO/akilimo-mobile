package com.iita.akilimo.models

import com.iita.akilimo.entities.AdviceStatus
import com.iita.akilimo.utils.enums.EnumAdviceTasks

class RecommendationOptions {
    var recName: String? = null
    var adviceName: EnumAdviceTasks? = null
    var statusImage: Int = 0
    var adviceStatus: AdviceStatus? = null

    constructor() {}

    @Deprecated("Removed this no need to pass image id now")
    constructor(recName: String, adviceName: EnumAdviceTasks, statusImage: Int) {
        this.recName = recName
        this.adviceName = adviceName
        this.statusImage = statusImage
    }

    constructor(
        recName: String,
        adviceName: EnumAdviceTasks,
        adviceStatus: AdviceStatus
    ) {
        this.recName = recName
        this.adviceName = adviceName
        this.statusImage = statusImage
        this.adviceStatus = adviceStatus
    }
}