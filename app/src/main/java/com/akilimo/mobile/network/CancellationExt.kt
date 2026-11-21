package com.akilimo.mobile.network

import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

private const val OKHTTP_CANCELED = "Canceled"
private const val MAX_CAUSE_DEPTH = 10
/**
 * True when this failure only means the caller gave up (a cancelled coroutine or an OkHttp call
 * cancelled with `IOException: Canceled`). These are expected, so they must not be reported as errors.
 */
fun Throwable.isCancellation(): Boolean =
    generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH).any {
        it is CancellationException || (it is IOException && it.message == OKHTTP_CANCELED)
    }


