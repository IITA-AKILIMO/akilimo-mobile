package com.akilimo.mobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.akilimo.mobile.network.dto.ApiErrorResponse
import com.squareup.moshi.Moshi
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {
    /**
     * Check if device has network connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Check actual internet connectivity by attempting to reach a reliable server
     * Should be called from a coroutine scope
     */
    suspend fun hasInternetConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://www.google.com")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.connect()
            conn.responseCode == 200
        } catch (e: Exception) {
            // Offline / DNS failures are the expected "no" answer here
            reportError(e)
            false
        }
    }

    /**
     * Comprehensive connectivity check
     * Combines network availability and actual internet access
     */
    suspend fun isInternetAccessible(context: Context): Boolean {
        return isNetworkAvailable(context) && hasInternetConnection()
    }
}

private const val DETAIL_MAX_CHARS = 240
private const val DETAIL_HTTP_MAX_CHARS = 400
private const val BODY_MAX_CHARS = 4000
private val WHITESPACE = Regex("\\s+")
private val DECIMAL_NUMBER = Regex("-?\\d+\\.\\d{2,}")

/**
 * Hides decimal numbers, which in an API message are the farm's coordinates. The farmer sees the full detail on their
 * own screen; copies sent to Sentry go through this first.
 */
fun String.redactDecimals(): String = replace(DECIMAL_NUMBER, "\u2026")

/**
 * One line a farmer can read out or paste to support when advice does not arrive: the failure type and its message
 * (for example `UnknownHostException: Unable to resolve host ...`), then the cause when it differs.
 */
fun Throwable.technicalDetail(): String {
    val root = cause?.takeIf { it !== this }
    val first = "${javaClass.simpleName}: ${message ?: "no message"}"
    val text = if (root == null) first else "$first (cause: ${root.javaClass.simpleName}: ${root.message ?: "no message"})"
    return text.replace(WHITESPACE, " ").take(DETAIL_MAX_CHARS)
}

/**
 * Technical facts for a failed HTTP answer, one per ` \u00B7 ` separator: status, method and path (no query string, so
 * no keys or signatures), plus the API's `code` and the names of invalid fields. The error body itself travels
 * separately (see [cappedBody]) so the screen can show it as formatted JSON.
 */
fun Response<*>.technicalDetail(error: ApiErrorResponse? = null): String {
    val request = raw().request
    val parts = mutableListOf("HTTP ${code()} ${request.method} ${request.url.encodedPath}")
    error?.code?.let { parts += "code=$it" }
    error?.errors?.keys?.takeIf { it.isNotEmpty() }?.let { parts += "fields=${it.joinToString(",")}" }
    return parts.joinToString(" \u00B7 ").take(DETAIL_HTTP_MAX_CHARS)
}

/** The error body as the API sent it, trimmed and capped so a huge error page cannot fill the screen. */
fun String.cappedBody(): String = trim().take(BODY_MAX_CHARS)

/** Parses the API's error JSON; null when it is not that shape (for example an HTML error page from a proxy). */
fun parseApiError(rawBody: String?): ApiErrorResponse? {
    if (rawBody.isNullOrBlank()) return null
    return runCatching { Moshi.Builder().build().adapter(ApiErrorResponse::class.java).fromJson(rawBody) }.getOrNull()
}

fun <T> Response<T>.parseError(): ApiErrorResponse? {
    val errorBody = errorBody()?.string() ?: return null
    return try {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        adapter.fromJson(errorBody)
    } catch (e: Exception) {
        Sentry.captureException(e)
        null
    }
}