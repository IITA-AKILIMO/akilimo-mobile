package com.akilimo.mobile.network

import io.sentry.Sentry
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

/**
 * Retries a request on [IOException], but only for idempotent methods: re-sending a POST
 * after a read timeout could duplicate data the server already stored (e.g. feedback).
 * Stops early if the call was cancelled.
 */
class RetryInterceptor(
    private val maxAttempts: Int = 3,
    private val backoffMillis: Long = 1000L,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method.uppercase() !in IDEMPOTENT_METHODS) {
            return chain.proceed(request)
        }

        var attempt = 0
        var lastException: IOException? = null

        while (attempt < maxAttempts) {
            if (chain.call().isCanceled()) {
                throw IOException("Canceled", lastException)
            }
            try {
                return chain.proceed(request)
            } catch (e: IOException) {
                lastException = e
                attempt++
                if (attempt < maxAttempts) {
                    Thread.sleep(backoffMillis * attempt) // linear backoff between attempts
                }
            }
        }
        throw IOException(
            "Failed after $maxAttempts attempts: ${lastException?.message}",
            lastException
        )
    }

    private companion object {
        val IDEMPOTENT_METHODS = setOf("GET", "HEAD", "OPTIONS")
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
