package com.akilimo.mobile.extensions

import android.content.Context
import com.akilimo.mobile.helper.WorkStatus

fun WorkStatus.Payload.toText(context: Context): String =
    when (this) {
        is WorkStatus.Payload.Raw -> message
        is WorkStatus.Payload.Localized -> context.getString(resId, *args)
    }
