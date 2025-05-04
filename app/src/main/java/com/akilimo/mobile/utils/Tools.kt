package com.akilimo.mobile.utils

import android.content.Context
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.SurveyRequest
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.sentry.Sentry
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt

object Tools {

    private val mapper: ObjectMapper = ObjectMapper().apply {
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        configure(SerializationFeature.INDENT_OUTPUT, true)
    }

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

    fun parseNetworkError(ex: VolleyError): VolleyError = ex

    fun prepareJsonObject(objectClass: RecommendationRequest): JSONObject? {
        return serializeObjectToJson(objectClass)
    }

    fun prepareJsonObject(objectClass: SurveyRequest): JSONObject? {
        return serializeObjectToJson(objectClass)
    }

    private fun serializeObjectToJson(obj: Any): JSONObject? {
        return try {
            val jsonString = mapper.writeValueAsString(obj)
            JSONObject(jsonString)
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            null
        }
    }

    fun iterateJsonObjects(jObject: JSONObject): String? {
        return try {
            jObject.keys().asSequence()
                .mapNotNull { key ->
                    when (val value = jObject[key]) {
                        is JSONObject -> jObject.optString(key)
                        is JSONArray -> value.optString(0)
                        is String -> value
                        else -> null
                    }
                }.firstOrNull()
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    fun generateUUID(): String = UUID.randomUUID().toString()
}