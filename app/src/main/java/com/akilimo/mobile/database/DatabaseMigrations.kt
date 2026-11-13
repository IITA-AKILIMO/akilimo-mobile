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

    /**
     * v5 → v6: Add weed_control_method column to akilimo_users.
     * Captures the weeding method selected during onboarding so it
     * pre-fills the BPP weed-control use-case screen.
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE akilimo_users ADD COLUMN recommendation_language TEXT DEFAULT 'en'")
        }
    }

    /**
     * v6 → v8: Drop the min/max allowed and local price columns from fertilizer_prices and
     * cassava_market_prices (never read), add the fertilizer category and the maize_units table.
     * Price rows are copied across with their ids, so saved fertilizer and cassava picks that point at them stay valid. Room keeps foreign keys off
     * while migrating, so the swap does not cascade into the selected_* tables.
     */
    val MIGRATION_6_8 = object : Migration(6, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE `fertilizer_prices_new` (`id` INTEGER NOT NULL, `fertilizer_key` TEXT NOT NULL, " +
                    "`fertilizer_country` TEXT NOT NULL, `country_code` TEXT NOT NULL, `currency_code` TEXT NOT NULL, " +
                    "`currency_symbol` TEXT NOT NULL, `sort_order` INTEGER NOT NULL, `price_per_bag` REAL NOT NULL, " +
                    "`price_range` TEXT NOT NULL, `active` INTEGER NOT NULL, `description` TEXT, `created_at` INTEGER, " +
                    "`updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "INSERT INTO fertilizer_prices_new (id, fertilizer_key, fertilizer_country, country_code, currency_code, " +
                    "currency_symbol, sort_order, price_per_bag, price_range, active, description, created_at, updated_at) " +
                    "SELECT id, fertilizer_key, fertilizer_country, country_code, currency_code, currency_symbol, " +
                    "sort_order, price_per_bag, price_range, active, description, created_at, updated_at FROM fertilizer_prices"
            )
            db.execSQL("DROP TABLE fertilizer_prices")
            db.execSQL("ALTER TABLE fertilizer_prices_new RENAME TO fertilizer_prices")

            db.execSQL(
                "CREATE TABLE `cassava_market_prices_new` (`id` INTEGER NOT NULL, `country_code` TEXT NOT NULL, " +
                    "`currency_code` TEXT NOT NULL, `currency_symbol` TEXT NOT NULL, `average_price` REAL NOT NULL, " +
                    "`exact_price` INTEGER NOT NULL, `item_tag` TEXT NOT NULL, `active` INTEGER NOT NULL, " +
                    "`sort_order` INTEGER NOT NULL, `created_at` INTEGER, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "INSERT INTO cassava_market_prices_new (id, country_code, currency_code, currency_symbol, average_price, " +
                    "exact_price, item_tag, active, sort_order, created_at, updated_at) " +
                    "SELECT id, country_code, currency_code, currency_symbol, average_price, exact_price, item_tag, " +
                    "active, sort_order, created_at, updated_at FROM cassava_market_prices"
            )
            db.execSQL("DROP TABLE cassava_market_prices")
            db.execSQL("ALTER TABLE cassava_market_prices_new RENAME TO cassava_market_prices")
            db.execSQL("ALTER TABLE fertilizers ADD COLUMN category TEXT DEFAULT NULL")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `maize_units` (`id` INTEGER NOT NULL, `label` TEXT NOT NULL, " +
                    "`unit_weight` REAL NOT NULL, `sort_order` INTEGER NOT NULL, `is_default` INTEGER NOT NULL, " +
                    "`is_active` INTEGER NOT NULL, `created_at` INTEGER, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))"
            )
        }
    }

    /**
     * v8 → v9: the farmer's saved advice (optional accounts). A new table only; nothing existing changes.
     */
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `saved_advice` (`request_id` TEXT NOT NULL, `account_id` INTEGER NOT NULL, " +
                    "`country_code` TEXT, `use_case` TEXT, `rec_type` TEXT, `recommendation` TEXT, " +
                    "`created_at` INTEGER NOT NULL, PRIMARY KEY(`request_id`))"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_saved_advice_account_id` ON `saved_advice` (`account_id`)")
        }
    }
}
