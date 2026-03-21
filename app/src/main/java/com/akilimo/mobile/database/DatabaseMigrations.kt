package com.akilimo.mobile.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    /**
     * Baseline migration — no schema changes.
     * Versions 1 and 2 used fallbackToDestructiveMigration (data was not preserved).
     * Version 3 establishes the first properly tracked schema snapshot.
     * All future schema changes must be accompanied by an explicit Migration here.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No schema changes between v2 and v3.
            // This migration exists to establish the proper migration chain.
        }
    }

    /**
     * v3 → v4: Add last_sync_at column to akilimo_users.
     * Tracks when the user's profile was last synced with the remote API.
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE akilimo_users ADD COLUMN last_sync_at INTEGER DEFAULT NULL")
        }
    }

    /**
     * v4 → v5: Add weed_control_method column to akilimo_users.
     * Captures the weeding method selected during onboarding so it
     * pre-fills the BPP weed-control use-case screen.
     */
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE akilimo_users ADD COLUMN weed_control_method TEXT DEFAULT NULL")
        }
    }
}
