package com.akilimo.mobile.models

import com.akilimo.mobile.entities.AdviceStatus
import com.akilimo.mobile.utils.enums.EnumAdvice
import com.akilimo.mobile.utils.enums.EnumAdviceTask

data class RecommendationOptions(
    val recommendationName: String,
    val adviceName: EnumAdviceTask = EnumAdviceTask.NOT_SELECTED,
    val adviceStatus: AdviceStatus = AdviceStatus("", false),
    val recommendationCode: EnumAdvice = EnumAdvice.NOT_SELECTED,
)
