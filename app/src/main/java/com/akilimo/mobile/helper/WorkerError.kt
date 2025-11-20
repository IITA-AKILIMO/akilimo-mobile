package com.akilimo.mobile.helper

import android.content.Context
import android.widget.Toast
import androidx.work.WorkInfo
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineExceptionHandler

object WorkerError {
    fun showWorkerError(context: Context, info: WorkInfo) {
        val message = info.outputData.getString("errorMessage") ?: return
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

val handler = CoroutineExceptionHandler { _, throwable ->
    Sentry.captureException(throwable)
}