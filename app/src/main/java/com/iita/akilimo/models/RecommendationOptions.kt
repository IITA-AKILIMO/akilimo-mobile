package com.iita.akilimo.models

import com.iita.akilimo.entities.AdviceStatus
import com.iita.akilimo.utils.enums.EnumAdviceTasks

class RecommendationOptions {
    var recName: String? = null
    var adviceName: EnumAdviceTasks? = null
    var statusImage: Int = 0
    var adviceStatus: AdviceStatus? = null

    constructor() {}

    constructor(recName: String, adviceName: EnumAdviceTasks, statusImage: Int) {
        this.recName = recName
        this.adviceName = adviceName
        this.statusImage = statusImage
    }

    constructor(
        recName: String,
        adviceName: EnumAdviceTasks,
        statusImage: Int,
        adviceStatus: AdviceStatus
    ) {
        this.recName = recName
        this.adviceName = adviceName
        this.statusImage = statusImage
        this.adviceStatus = adviceStatus
    }
}