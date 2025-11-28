package com.akilimo.mobile.base.workers

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * Generic paged worker that safely pages through a remote list and persists results locally.
 *
 * Subclasses supply the API type via the BaseApiWorker generic, implement [fetchPage] and
 * [updateLocalDatabase], and set [serviceType] in the subclass to select the correct base URL.
 *
 * - Uses a Mutex to protect shared state updates.
 * - Respects WorkManager cancellation via [isStopped] and cooperative coroutines cancellation.
 * - Reports progress as pages are fetched.
 */
abstract class SafePagedWorker<TApi, TItem>(
    context: Context,
    params: WorkerParameters
) : BaseApiWorker<TApi>(context, params) {

    private val mutex = Mutex()

    override suspend fun performSafeWork(): Result {
        var page = 1
        var totalSaved = 0
        var lastPageReached = false

        try {
            while (!lastPageReached && !isStopped) {
                val pageResult = try {
                    fetchPage(page)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to fetch page $page")
                    return resultFailure("Failed to fetch page $page: ${e.message ?: "unknown"}")
                }

                // Persist and update shared counters inside a mutex so multiple coroutines won't race
                mutex.withLock {
                    if (pageResult.items.isNotEmpty()) {
                        updateLocalDatabase(pageResult.items)
                        totalSaved += pageResult.items.size
                    }
                    lastPageReached = pageResult.isLastPage
                }

                // Update progress using pages info if available
                val progressPercent = if (pageResult.totalPages > 0) {
                    ((page.toDouble() / pageResult.totalPages) * 100).toInt().coerceIn(0, 100)
                } else {
                    // no totalPages info -> approximate progress by pages fetched (cap at 99)
                    (page * 5).coerceAtMost(99)
                }
                setProgressPercent(progressPercent)

                page++
            }

            return resultSuccess(
                mapOf(
                    "savedCount" to totalSaved,
                    "pagesFetched" to (page - 1),
                    "message" to "Completed successfully"
                )
            )
        } catch (ce: kotlinx.coroutines.CancellationException) {
            Timber.w("Worker cancelled: ${javaClass.simpleName}")
            return resultFailure("Worker cancelled")
        } catch (t: Throwable) {
            Timber.e(t, "Unhandled error in paged worker ${javaClass.simpleName}")
            return resultFailure(t.message ?: "Unexpected error")
        }
    }

    /**
     * Fetch a single page from the remote API. Implementations should return a [PageResult]
     * containing the page items, whether this is the last page, and the total page count if known.
     *
     * Note: this method runs on the worker's IO dispatcher (inherited from BaseApiWorker).
     */
    protected abstract suspend fun fetchPage(page: Int): PageResult<TItem>

    /**
     * Persist page items into local storage. This is called under [mutex] to protect shared counters.
     */
    protected abstract suspend fun updateLocalDatabase(items: List<TItem>)

    /**
     * Simple page result wrapper. Keep item type generic.
     */
    protected data class PageResult<T>(
        val items: List<T>,
        val isLastPage: Boolean,
        val totalPages: Int = 0
    )
}