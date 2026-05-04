package com.akilimo.mobile.dto

import android.os.Parcelable
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdviceCompletionDto(
    val taskName: EnumAdviceTask,
    val stepStatus: EnumStepStatus
) : Parcelable