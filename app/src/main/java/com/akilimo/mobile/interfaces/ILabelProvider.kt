package com.akilimo.mobile.interfaces

import android.content.Context

interface ILabelProvider {
    fun label(context: Context): String
}