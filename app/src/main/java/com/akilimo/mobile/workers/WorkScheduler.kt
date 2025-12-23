package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.util.concurrent.TimeUnit

object WorkerScheduler {

    inline fun <reified T : ListenableWorker> schedulePeriodicWorker(
        context: Context,
        workName: String,
        repeatInterval: Long = 24L,
        timeUnit: TimeUnit = TimeUnit.HOURS,
        inputData: Data = Data.EMPTY,
        constraints: Constraints = defaultConstraints(),
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP
    ) {
        val periodicRequest = PeriodicWorkRequestBuilder<T>(repeatInterval, timeUnit)
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            policy,
            periodicRequest
        )
    }

    inline fun <reified T : ListenableWorker> scheduleOneTimeWorker(
        context: Context,
        workName: String,
        constraints: Constraints = defaultConstraints(),
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE,
        inputData: Data = Data.EMPTY
    ) {
        val oneTimeRequest = OneTimeWorkRequestBuilder<T>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            policy,
            oneTimeRequest
        )
    }

    inline fun <reified T : ListenableWorker> scheduleChainedWorkers(
        context: Context,
        secondWorker: Class<out ListenableWorker>,
        firstName: String,
        secondName: String,
        constraints: Constraints = defaultConstraints()
    ) {
        val firstRequest = OneTimeWorkRequestBuilder<T>()
            .setConstraints(constraints)
            .addTag(firstName)
            .build()

        val secondRequest = OneTimeWorkRequest.Builder(secondWorker)
            .setConstraints(constraints)
            .addTag(secondName)
            .build()


        WorkManager.getInstance(context)
            .beginWith(firstRequest)
            .then(secondRequest)
            .enqueue()
    }

    fun cancelWorker(context: Context, workName: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName)
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }

    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWork()
    }

    fun defaultConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}