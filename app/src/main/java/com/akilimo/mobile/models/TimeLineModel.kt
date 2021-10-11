package com.akilimo.mobile.models

import android.os.Parcelable
import com.akilimo.mobile.utils.enums.StepStatus
import kotlinx.android.parcel.Parcelize

@Parcelize
class TimeLineModel(
    var stepTitle: String,
    var message: String? = null,
    var status: StepStatus
) : Parcelable
