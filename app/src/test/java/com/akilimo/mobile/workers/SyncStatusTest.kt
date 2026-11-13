package com.akilimo.mobile.workers

import org.junit.Assert.assertEquals
import org.junit.Test

class SyncStatusTest {

    @Test
    fun `offline wins over every other state`() {
        val status = SyncStatus(
            isOnline = false,
            isSyncing = true,
            lastSyncedAt = 1L,
            lastAttemptFailed = true,
        )

        assertEquals(SyncPhase.OFFLINE, status.phase)
    }

    @Test
    fun `a running sync wins over the last result`() {
        val status = SyncStatus(
            isOnline = true,
            isSyncing = true,
            lastSyncedAt = 1L,
            lastAttemptFailed = true,
        )

        assertEquals(SyncPhase.SYNCING, status.phase)
    }

    @Test
    fun `a failure after a success is reported as failed`() {
        val status = SyncStatus(
            isOnline = true,
            isSyncing = false,
            lastSyncedAt = 1L,
            lastAttemptFailed = true,
        )

        assertEquals(SyncPhase.FAILED, status.phase)
    }

    @Test
    fun `a completed sync is reported as synced`() {
        val status = SyncStatus(
            isOnline = true,
            isSyncing = false,
            lastSyncedAt = 1L,
            lastAttemptFailed = false,
        )

        assertEquals(SyncPhase.SYNCED, status.phase)
    }

    @Test
    fun `no finished sync is reported as never`() {
        val status = SyncStatus(
            isOnline = true,
            isSyncing = false,
            lastSyncedAt = null,
            lastAttemptFailed = false,
        )

        assertEquals(SyncPhase.NEVER, status.phase)
    }
}
