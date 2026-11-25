package com.akilimo.mobile.network

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.ScopeCallback
import io.sentry.SentryLevel
import io.sentry.protocol.SentryId
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

private const val REFERENCE_LENGTH = 8
private const val ANALYTICS_VALUE_MAX = 100

/**
 * Counts a failed recommendation request in Firebase Analytics for quick tracking by day, app version and country:
 * only the kind of failure, HTTP status and API code, never any farm data. Never throws.
 */
fun logRecommendationFailureEvent(context: Context, kind: String, status: Int?, apiCode: String?) {
    runCatching {
        FirebaseAnalytics.getInstance(context).logEvent(
            "recommendation_failed",
            Bundle().apply {
                putString("kind", kind)
                status?.let { putLong("http_status", it.toLong()) }
                apiCode?.let { putString("api_code", it.take(ANALYTICS_VALUE_MAX)) }
            },
        )
    }.onFailure { Timber.d(it, "Analytics event not sent") }
}

/**
 * Sends a copy of a failed recommendation request to Sentry, grouped into one issue per kind of failure (kind, HTTP
 * status and API code, never the farm's data) with the technical line attached. Returns a short reference the farmer can
 * quote so support finds the event, or null when nothing was sent (cancelled, throttled, or Sentry is off).
 */
fun reportRecommendationFailure(
    kind: String,
    detail: String,
    level: SentryLevel,
    status: Int? = null,
    apiCode: String? = null,
    cause: Throwable? = null,
    throttle: Boolean = false,
): String? {
    if (cause?.isCancellation() == true) return null
    if (throttle && !allowEvent("recommendation:$kind:${status ?: ""}:${apiCode ?: ""}")) return null

    val scope = ScopeCallback { s ->
        s.level = level
        s.fingerprint = listOf("recommendation-failure", kind, status?.toString().orEmpty(), apiCode.orEmpty())
        s.setTag("failure.kind", kind)
        status?.let { s.setTag("http.status", it.toString()) }
        apiCode?.let { s.setTag("api.code", it) }
        s.setExtra("technical_detail", detail)
    }
    val id = if (cause != null) {
        Sentry.captureException(cause, scope)
    } else {
        Sentry.captureMessage("Recommendation failed: ${listOfNotNull(kind, status, apiCode).joinToString(" ")}", scope)
    }
    return id.takeUnless { it == SentryId.EMPTY_ID }?.toString()?.take(REFERENCE_LENGTH)
}

/** True at most once per [EXPECTED_EVENT_INTERVAL_MS] for each [key]. */
private fun allowEvent(key: String): Boolean {
    val now = System.currentTimeMillis()
    val last = lastExpectedEvent[key] ?: 0L
    if (now - last < EXPECTED_EVENT_INTERVAL_MS) return false
    lastExpectedEvent[key] = now
    return true
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
