package com.akilimo.mobile.workers

import android.content.Context

fun WorkStatus.Payload.toText(context: Context): String =
    when (this) {
        is WorkStatus.Payload.Raw -> message
        is WorkStatus.Payload.Localized -> context.getString(resId, *args)
    }
