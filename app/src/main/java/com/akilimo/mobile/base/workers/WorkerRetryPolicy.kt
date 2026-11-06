package com.akilimo.mobile.base.workers

import retrofit2.HttpException
import java.io.IOException

/**
 * Decides whether a sync worker should ask WorkManager to retry (with the configured
 * exponential backoff) or give up.
 *
 * Transient: network/IO errors (offline, timeouts, DNS) and server-side HTTP errors
 * (5xx, 408 Request Timeout, 429 Too Many Requests). Everything else — 4xx client errors,
 * parsing bugs, database errors — is permanent and retrying would not help.
 */
object WorkerRetryPolicy {

    /** Give up after this many attempts so a persistently broken endpoint can't retry forever. */
    const val MAX_ATTEMPTS = 5

    fun isTransient(error: Throwable): Boolean = when (error) {
        is HttpException -> error.code() >= 500 || error.code() == 408 || error.code() == 429
        is IOException -> true
        else -> false
    }

    /** [runAttemptCount] is WorkManager's 0-based attempt counter for this request. */
    fun shouldRetry(error: Throwable, runAttemptCount: Int): Boolean =
        isTransient(error) && runAttemptCount + 1 < MAX_ATTEMPTS
}
