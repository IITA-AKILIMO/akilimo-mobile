package com.akilimo.mobile.base.workers

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.akilimo.mobile.AppDatabase
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.interfaces.DefaultDispatcherProvider
import com.akilimo.mobile.interfaces.IDispatcherProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Generic base worker that lazily provides a typed API client for a specified service.
 *
 * Subclasses must provide the serviceType (used to resolve base URL via AppConfig)
 * and implement createApi to construct the typed API client (Retrofit/OkHttp wrapper).
 */
abstract class BaseApiWorker<TApi>(
    appContext: Context,
    params: WorkerParameters
) : NetworkAwareWorker(appContext, params) {

    protected val db: AppDatabase = AppDatabase.getDatabase(appContext)

    /**
     * Which service this worker targets (used to pick the right base URL from AppConfig).
     * Example: EnumServiceType.AKILIMO or EnumServiceType.FUELROD
     */
    protected abstract val serviceType: EnumServiceType

    /**
     * Create the typed API client given a Context and resolved baseUrl.
     * Implementations should keep creation cheap and deterministic (no heavy blocking).
     */
    protected abstract fun createApi(context: Context, baseUrl: String): TApi

    /**
     * Lazily create the typed API using the resolved base URL for the configured serviceType.
     * This avoids early class-initialization problems and lets AppConfig / SessionManager be ready.
     */
    protected val api: TApi by lazy {
        val base = AppConfig.resolveBaseUrlFor(
            applicationContext, when (serviceType) {
                EnumServiceType.AKILIMO -> EnumServiceType.AKILIMO
                EnumServiceType.FUELROD -> EnumServiceType.FUELROD
            }
        )
        createApi(applicationContext, base)
    }

    protected val dispatcherProvider: IDispatcherProvider = DefaultDispatcherProvider()

    override suspend fun doWork(): Result = withContext(dispatcherProvider.io) {

        if (!isNetworkAvailable()) {
            return@withContext resultRetry("Network unavailable")
        }

        if (isStopped) {
            Timber.w("Worker stopped before execution: ${javaClass.simpleName}")
            return@withContext resultFailure("Worker was stopped")
        }

        try {
            ensureActive()
            performSafeWork()
        } catch (e: CancellationException) {
            Timber.w("Worker cancelled: ${javaClass.simpleName}")
            resultFailure("Worker cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Unhandled error in ${javaClass.simpleName}")
            resultFailure(e.message ?: "Unexpected error")
        }
    }

    protected abstract suspend fun performSafeWork(): Result

    protected suspend fun setProgressPercent(percent: Int) {
        setProgress(workDataOf("progress" to percent.coerceIn(0, 100)))
    }

    protected fun resultSuccess(data: Map<String, Any> = emptyMap()): Result =
        Result.success(workDataOf(*data.map { it.key to it.value }.toTypedArray()))

    protected fun resultRetry(reason: String): Result {
        Timber.w("Retry scheduled: $reason")
        return Result.retry()
    }

    protected fun resultFailure(reason: String): Result {
        Timber.e("Worker failure: $reason")
        return Result.failure(workDataOf("errorMessage" to reason))
    }
}