package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akilimo.mobile.legal.LegalPage
import com.akilimo.mobile.legal.LegalPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Downloads the privacy policy and terms of use from akilimo.org into the app's own storage, so they can be shown
 * offline. Runs once at start-up and then about every 30 days. A page is only replaced by a complete, valid
 * download; on any failure the copy already saved stays, and the work is retried later.
 */
class LegalPagesWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var allOk = true
        for (page in LegalPage.entries) {
            if (!download(page)) allOk = false
        }
        if (allOk) Result.success() else Result.retry()
    }

    private fun download(page: LegalPage): Boolean {
        val target = LegalPages.file(applicationContext, page)
        val temp = File(target.parentFile, "${page.fileName}.tmp")
        return try {
            target.parentFile?.mkdirs()
            val request = Request.Builder().url(page.webUrl).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body.string()
                // Reject empty, oversized or non-HTML answers (for example a captive portal page)
                if (body.isBlank() || body.length > MAX_CHARS || !body.contains("<html", ignoreCase = true)) return false
                temp.writeText(body)
            }
            if (target.exists()) target.delete()
            temp.renameTo(target)
        } catch (e: Exception) {
            Timber.w(e, "Could not download %s", page.webUrl)
            temp.delete()
            false
        }
    }

    private companion object {
        const val MAX_CHARS = 1_000_000

        val client: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }
}
