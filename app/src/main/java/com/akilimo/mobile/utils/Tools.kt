package com.akilimo.mobile.utils

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import kotlin.math.roundToInt

object Tools {

    fun displayImageOriginal(ctx: Context?, img: ImageView?, @DrawableRes drawable: Int) {
        if (ctx == null || img == null) return
        try {
            Glide.with(ctx)
                .load(drawable)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(img)
        } catch (e: Exception) {
            // Optional: log or ignore
        }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics
        ).roundToInt()
    }

    fun replaceNonNumbers(rawString: String, replaceWith: String?): String {
        return try {
            rawString.replace("\\D+".toRegex(), replaceWith ?: "")
        } catch (ex: Exception) {
            rawString
        }
    }

    fun replaceCharacters(
        rawString: String,
        stringToReplace: String?,
        replaceWith: String?
    ): String {
        return try {
            rawString.replace(stringToReplace ?: "", replaceWith ?: "").trim()
        } catch (ex: Exception) {
            rawString
        }
    }
}