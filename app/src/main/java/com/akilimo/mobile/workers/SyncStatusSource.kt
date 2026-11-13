package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one place the UI asks about sync state: connectivity, whether a sync worker is running,
 * and when the last sync succeeded or failed.
 *
 * WorkManager keeps the state of the unique starts from [AkilimoApp]; watching those names covers
 * every worker without each caller having to know the list. A worker's success/failure timestamp
 * is written to [AppSettingsDataStore] by the worker base class, so it survives the process and
 * is still there before the first WorkManager query returns.
 */
@Singleton
class SyncStatusSource @Inject constructor(
    @ApplicationContext context: Context,
    private val networkMonitor: NetworkMonitor,
    private val appSettings: AppSettingsDataStore,
) {
    private val workManager = WorkManager.getInstance(context)

    private val watchedWorkNames = listOf(
        WorkConstants.FERTILIZER_CHAIN_WORK_NAME,
        WorkConstants.INVESTMENT_AMOUNT_WORK_NAME,
        WorkConstants.STARCH_FACTORY_WORK_NAME,
        WorkConstants.CASSAVA_MARKET_PRICES_WORK_NAME,
        WorkConstants.CASSAVA_UNITS_WORK_NAME,
        WorkConstants.MAIZE_UNITS_WORK_NAME,
    )

    /** Only RUNNING counts: ENQUEUED work may be waiting for a network that is not there yet. */
    private val isSyncing: Flow<Boolean> =
        combine(watchedWorkNames.map { workManager.getWorkInfosForUniqueWorkFlow(it) }) { infosByWork ->
            infosByWork.any { infos -> infos.any { it.state == WorkInfo.State.RUNNING } }
        }.distinctUntilChanged()

    val status: Flow<SyncStatus> = combine(
        networkMonitor.isConnected,
        isSyncing,
        appSettings.lastSyncAtFlow,
        appSettings.lastSyncFailedFlow,
    ) { online, syncing, lastSyncedAt, lastAttemptFailed ->
        SyncStatus(
            isOnline = online,
            isSyncing = syncing,
            lastSyncedAt = lastSyncedAt,
            lastAttemptFailed = lastAttemptFailed,
        )
    }.distinctUntilChanged()
}
