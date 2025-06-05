package com.akilimo.mobile.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources

object VectorDrawableUtils {

    fun getDrawable(context: Context, drawableResId: Int): Drawable? {
        return AppCompatResources.getDrawable(context, drawableResId)
    }

    fun getDrawable(context: Context, drawableResId: Int, colorFilter: Int): Drawable? {
        val drawable = getDrawable(context, drawableResId)?.mutate()
        drawable?.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN)
        return drawable
    }
}
