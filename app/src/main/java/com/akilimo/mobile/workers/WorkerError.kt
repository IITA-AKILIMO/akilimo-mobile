package com.akilimo.mobile.workers

import android.content.Context
import android.widget.Toast
import androidx.work.WorkInfo
import com.akilimo.mobile.network.reportError
import kotlinx.coroutines.CoroutineExceptionHandler

object WorkerError {
    fun showWorkerError(context: Context, info: WorkInfo) {
        val message = info.outputData.getString("errorMessage") ?: return
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

val handler = CoroutineExceptionHandler { _, throwable ->
    reportError(throwable)
}
