package com.akilimo.mobile.network

import io.sentry.Sentry
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null

        while (attempt < maxRetries) {
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                lastException = e
                attempt++
                Thread.sleep(1000L * attempt) // exponential backoff
            }
        }
        throw IOException(
            "Failed after $maxRetries retries ${lastException?.message}",
            lastException
        )
    }
}

class SafeNetworkInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            chain.proceed(chain.request())
        } catch (e: Exception) {
            Timber.e(e, "SafeNetworkInterceptor: Network error")
            Sentry.captureException(e)
            throw IOException("Network error", e)
        }
    }
}
