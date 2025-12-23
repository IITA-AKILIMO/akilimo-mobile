package com.akilimo.mobile.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.akilimo.mobile.dto.ApiErrorResponse
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.URL
import android.os.Build

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

fun <T> Response<T>.parseError(): ApiErrorResponse? {
    val errorBody = errorBody()?.string() ?: return null
    return try {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(ApiErrorResponse::class.java)
        adapter.fromJson(errorBody)
    } catch (e: Exception) {
        null
    }
}