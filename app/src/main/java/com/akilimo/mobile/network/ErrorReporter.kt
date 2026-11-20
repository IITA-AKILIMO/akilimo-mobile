package com.akilimo.mobile.network

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import retrofit2.HttpException
import timber.log.Timber
import java.io.EOFException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

private const val MAX_CAUSE_DEPTH = 10
private const val HTTP_CLIENT_ERROR_MIN = 400
private const val HTTP_CLIENT_ERROR_MAX = 499
private const val EXPECTED_EVENT_INTERVAL_MS = 5 * 60 * 1000L

/**
 * True for failures that are normal on mobile networks (offline, DNS, timeouts, dropped
 * connections) or expected API answers (HTTP 4xx). They say nothing about a bug in the app.
 * TLS certificate problems are deliberately NOT included: those need attention.
 */
fun Throwable.isExpectedNetworkFailure(): Boolean =
    generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH).any {
        when (it) {
            is UnknownHostException,
            is SocketTimeoutException,
            is ConnectException,
            is NoRouteToHostException,
            is SocketException,
            is EOFException,
            is InterruptedIOException,
            is ProtocolException -> true

            is HttpException -> it.code() in HTTP_CLIENT_ERROR_MIN..HTTP_CLIENT_ERROR_MAX
            else -> false
        }
    }

private val lastExpectedEvent = ConcurrentHashMap<String, Long>()

/**
 * Reports [e] to Sentry according to how much it matters:
 * - cancellations are dropped (the caller gave up);
 * - expected network failures are tracked as a breadcrumb plus, at most once every five minutes per
 *   exception type, one INFO event grouped into a single issue, so outages stay visible without noise;
 * - everything else is captured as a normal error.
 */
fun reportError(e: Throwable) {
    when {
        e.isCancellation() -> Timber.d(e, "Cancelled, not reported")
        e.isExpectedNetworkFailure() -> trackExpectedFailure(e)
        else -> Sentry.captureException(e)
    }
}

private fun trackExpectedFailure(e: Throwable) {
    val type = (e as? IOException ?: e.cause ?: e).javaClass.simpleName
    Timber.w(e, "Expected network failure: %s", type)

    Sentry.addBreadcrumb(
        Breadcrumb().apply {
            category = "network"
            message = "Expected network failure: $type"
            level = SentryLevel.WARNING
        }
    )

    val now = System.currentTimeMillis()
    val last = lastExpectedEvent[type] ?: 0L
    if (now - last >= EXPECTED_EVENT_INTERVAL_MS) {
        lastExpectedEvent[type] = now
        Sentry.captureException(e) { scope ->
            scope.level = SentryLevel.INFO
            scope.fingerprint = listOf("expected-network-failure", type)
            scope.setTag("expected", "true")
        }
    }
}
