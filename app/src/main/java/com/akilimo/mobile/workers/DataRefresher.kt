package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkManager
import androidx.work.await
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the same syncs as app start-up on demand (pull to refresh) and waits for them to finish.
 *
 * A sync that is already running is joined rather than restarted. With no network the work stays
 * queued, so the wait gives up after [TIMEOUT_MS] and the data on screen stays as it was.
 */
@Singleton
class DataRefresher @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun fertilizers() = await(WorkConstants.FERTILIZER_CHAIN_WORK_NAME) {
        WorkerScheduler.scheduleChainedWorkers<FertilizerWorker>(
            context = context,
            secondWorker = FertilizerPriceWorker::class.java,
            chainName = WorkConstants.FERTILIZER_CHAIN_WORK_NAME,
            firstName = WorkConstants.FERTILIZER_WORK_NAME,
            secondName = WorkConstants.FERTILIZER_PRICE_WORK_NAME
        )
    }

    suspend fun investmentAmounts() = await(WorkConstants.INVESTMENT_AMOUNT_WORK_NAME) {
        WorkerScheduler.scheduleOneTimeWorker<InvestmentAmountWorker>(
            context = context,
            workName = WorkConstants.INVESTMENT_AMOUNT_WORK_NAME
        )
    }

    suspend fun cassavaMarket() {
        await(WorkConstants.CASSAVA_MARKET_PRICES_WORK_NAME) {
            WorkerScheduler.scheduleOneTimeWorker<CassavaPriceWorker>(
                context = context,
                workName = WorkConstants.CASSAVA_MARKET_PRICES_WORK_NAME
            )
        }
        await(WorkConstants.CASSAVA_UNITS_WORK_NAME) {
            WorkerScheduler.scheduleOneTimeWorker<CassavaUnitWorker>(
                context = context,
                workName = WorkConstants.CASSAVA_UNITS_WORK_NAME
            )
        }
        await(WorkConstants.STARCH_FACTORY_WORK_NAME) {
            WorkerScheduler.scheduleOneTimeWorker<StarchFactoryWorker>(
                context = context,
                workName = WorkConstants.STARCH_FACTORY_WORK_NAME
            )
        }
    }

    private suspend fun await(uniqueName: String, enqueue: () -> androidx.work.Operation) {
        enqueue().await() // the work is registered, so the status below is for this run
        withTimeoutOrNull(TIMEOUT_MS) {
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(uniqueName)
                .first { infos -> infos.isNotEmpty() && infos.all { it.state.isFinished } }
        }
    }

    private companion object {
        const val TIMEOUT_MS = 30_000L
    }
}