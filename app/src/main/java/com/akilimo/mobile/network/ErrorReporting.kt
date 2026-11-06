package com.akilimo.mobile.network

import io.sentry.Sentry
import java.io.IOException

/**
 * True for failures that are expected in the field (offline, DNS, timeouts, resets).
 * [SafeNetworkInterceptor] wraps non-IO errors in an IOException, so a wrapped
 * non-IO cause still counts as a real bug.
 */
fun Throwable.isExpectedNetworkError(): Boolean =
    this is IOException && (cause == null || cause is IOException)

/** Report to Sentry unless it's an expected network failure (avoids flooding the quota). */
fun reportUnlessNetworkError(error: Throwable) {
    if (!error.isExpectedNetworkError()) Sentry.captureException(error)
}
