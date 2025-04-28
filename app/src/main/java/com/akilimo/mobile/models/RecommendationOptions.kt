package com.akilimo.mobile.models

import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.utils.enums.EnumAdviceTasks

class RecommendationOptions(
    recName: String,
    adviceName: EnumAdviceTasks,
    adviceStatus: AdviceStatus
) {
    var recName: String? = recName
    var adviceName: EnumAdviceTasks? = adviceName
    var statusImage: Int = 0
    var adviceStatus: AdviceStatus? = adviceStatus
}
