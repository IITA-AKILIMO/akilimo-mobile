package com.akilimo.mobile.workers

/** What the hub's status strip should say, in priority order (see [phase]). */
enum class SyncPhase {
    OFFLINE,
    SYNCING,
    FAILED,
    SYNCED,
    NEVER,
}

/**
 * Connectivity and background-sync state for the status strip.
 *
 * [isOnline] comes from the connectivity callback; [isSyncing] is true while one of the sync
 * workers is running; [lastSyncedAt] is the epoch millis of the last successful sync and
 * [lastAttemptFailed] is true when the last finished attempt failed.
 */
data class SyncStatus(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncedAt: Long? = null,
    val lastAttemptFailed: Boolean = false,
) {
    /**
     * Offline wins over everything (nothing is updating while there is no network), then a
     * running sync, then the last result. [NEVER] means no sync has finished yet.
     */
    val phase: SyncPhase
        get() = when {
            !isOnline -> SyncPhase.OFFLINE
            isSyncing -> SyncPhase.SYNCING
            lastAttemptFailed -> SyncPhase.FAILED
            lastSyncedAt != null -> SyncPhase.SYNCED
            else -> SyncPhase.NEVER
        }
}
