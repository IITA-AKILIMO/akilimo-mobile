package com.akilimo.mobile.utils

import android.content.*
import android.util.Log
import android.util.TypedValue
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.android.volley.VolleyError
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crashlytics.android.Crashlytics
import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.akilimo.mobile.rest.request.RecommendationRequest
import com.akilimo.mobile.rest.request.SurveyRequest
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object Tools {
    private val LOG_TAG = Tools::class.java.simpleName
    private val mapper = ObjectMapper()

    @JvmStatic
    fun displayImageOriginal(ctx: Context?, img: ImageView?, @DrawableRes drawable: Int) {
        try {
            Glide.with(ctx!!).load(drawable).transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.NONE).into(img!!)
        } catch (e: Exception) {
        }
    }

    @JvmStatic
    fun formatCalendarToDateString(dateTime: Date): String {
        val newFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)
        return newFormat.format(dateTime)
    }

    @JvmStatic
    fun formatLongToDateString(dateTime: Long?): String {
        return formatCalendarToDateString(Date(dateTime!!))
    }


    @JvmStatic
    fun dpToPx(c: Context, dp: Int): Int {
        val r = c.resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), r.displayMetrics
        ).roundToInt()
    }

    @JvmStatic
    fun replaceNonNumbers(rawString: String, replaceWith: String?): String {
        return try {
            rawString.replace("\\D+".toRegex(), replaceWith!!)
        } catch (ex: Exception) {
            rawString
        }
    }

    @JvmStatic
    fun replaceCharacters(
        rawString: String,
        stringToReplace: String?,
        replaceWith: String?,
    ): String {
        var cleanedString = rawString
        try {
            cleanedString = rawString.replace(stringToReplace!!, replaceWith!!).trim { it <= ' ' }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return cleanedString
    }

    @JvmStatic
    fun parseNetworkError(volleyError: VolleyError): VolleyError {
        return volleyError
    }

    /**
     * @param objectClass
     * @return JsonObject
     */
    @JvmStatic
    fun prepareJsonObject(objectClass: RecommendationRequest): JSONObject? {
        var jsonObject: JSONObject? = null
        val jsonString: String
        try {
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
            jsonString = mapper.writeValueAsString(objectClass)
            jsonObject = JSONObject(jsonString)
        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
        }
        return jsonObject
    }

    @JvmStatic
    fun prepareJsonObject(objectClass: SurveyRequest): JSONObject? {
        var jsonObject: JSONObject? = null
        val jsonString: String
        try {
            mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
            jsonString = mapper.writeValueAsString(objectClass)
            jsonObject = JSONObject(jsonString)
        } catch (ex: Exception) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.message)
            Crashlytics.logException(ex)
        }
        return jsonObject
    }

    private fun iterateJsonObjects(jObject: JSONObject): String? {
        val keys: Iterator<*> = jObject.keys()
        var message: String? = null
        try {
            while (keys.hasNext()) {
                val key = keys.next() as String
                if (jObject[key] is JSONObject) {
                    message = jObject.getString(key)
                    break
                } else if (jObject[key] is JSONArray) {
                    val J = jObject[key] as JSONArray
                    message = J[0].toString()
                    break
                } else if (jObject[key] is String) {
                    //it is a string
                    message = jObject.getString(key)
                    break
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return message
    }

    fun generateUUID(): String {
        return UUID.randomUUID().toString()
    }

}
