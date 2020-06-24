package com.iita.akilimo.models

import android.os.Parcelable
import com.iita.akilimo.utils.enums.StepStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
class TimeLineModel(
    var stepTitle: String,
    var message: String? = null,
    var status: StepStatus
) : Parcelable