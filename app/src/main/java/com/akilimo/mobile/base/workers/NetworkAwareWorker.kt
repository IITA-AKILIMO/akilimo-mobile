package com.akilimo.mobile.base.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akilimo.mobile.AkilimoApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

abstract class NetworkAwareWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Check if network is available before doing work
     */
    protected suspend fun isNetworkAvailable(): Boolean {
        return AkilimoApp.Companion.instance.networkMonitor.isConnected.first()
    }

    /**
     * Wait for network connection (with timeout)
     */
    protected suspend fun waitForNetwork(timeoutMillis: Long = 30000): Boolean {
        val startTime = System.currentTimeMillis()
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            if (isNetworkAvailable()) {
                return true
            }
            delay(1000) // Check every second
        }
        return false
    }
}