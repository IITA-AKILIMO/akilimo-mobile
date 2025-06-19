package com.akilimo.mobile.utils

import kotlinx.coroutines.delay
import kotlin.math.pow

suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L, // in milliseconds
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var attempt = 0
    var currentDelay = initialDelay

    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            attempt++
            if (attempt >= maxRetries) throw e
            delay(currentDelay)
            currentDelay = (initialDelay * factor.pow(attempt)).toLong()
        }
    }
}
