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
private const val DETAIL_MESSAGE_MAX_CHARS = 160
private val WHITESPACE = Regex("\\s+")
private val DECIMAL_NUMBER = Regex("-?\\d+\\.\\d{2,}")

/**
 * One line a farmer can read out or paste to support when advice does not arrive: the failure type and its message
 * (for example `UnknownHostException: Unable to resolve host ...`), then the cause when it differs.
 * Never holds a request or response body.
 */
fun Throwable.technicalDetail(): String {
    val root = cause?.takeIf { it !== this }
    val first = "${javaClass.simpleName}: ${message ?: "no message"}"
    val text = if (root == null) first else "$first (cause: ${root.javaClass.simpleName}: ${root.message ?: "no message"})"
    return text.replace(WHITESPACE, " ").take(DETAIL_MAX_CHARS)
}

/**
 * Technical line for a failed HTTP answer: status, method and path (no query string, so no keys or signatures), plus
 * the API's own machine fields when it sent them: `code`, a short `message` and the names (not values) of invalid fields.
 */
fun Response<*>.technicalDetail(error: ApiErrorResponse? = null): String {
    val request = raw().request
    val parts = mutableListOf("HTTP ${code()} ${request.method} ${request.url.encodedPath}")
    error?.code?.let { parts += "code=$it" }
    // The free text only helps when there is no machine code, and it can quote the farm's coordinates: those are hidden
    if (error?.code == null) {
        error?.message?.takeIf { it.isNotBlank() }
            ?.let { parts += "message=${it.replace(DECIMAL_NUMBER, "\u2026").take(DETAIL_MESSAGE_MAX_CHARS)}" }
    }
    error?.errors?.keys?.takeIf { it.isNotEmpty() }?.let { parts += "fields=${it.joinToString(",")}" }
    return parts.joinToString(" \u00B7 ").replace(WHITESPACE, " ").take(DETAIL_MAX_CHARS)
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