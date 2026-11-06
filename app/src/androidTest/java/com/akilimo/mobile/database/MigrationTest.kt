package com.akilimo.mobile.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies every Room migration against the exported schemas in app/schemas, so a schema
 * change without a matching Migration fails here instead of wiping users' data.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate3To6_validatesSchemaAtEachStep() {
        helper.createDatabase(dbName, 3).close()
        helper.runMigrationsAndValidate(dbName, 4, true, DatabaseMigrations.MIGRATION_3_4).close()
        helper.runMigrationsAndValidate(dbName, 5, true, DatabaseMigrations.MIGRATION_4_5).close()
        helper.runMigrationsAndValidate(dbName, 6, true, DatabaseMigrations.MIGRATION_5_6).close()
    }

    @Test
    fun migrate4To6_addsUserColumnsWithDefaults() {
        helper.createDatabase(dbName, 4).use { db ->
            // All NOT NULL columns without defaults in the v4 schema (4.json)
            db.execSQL(
                """
                INSERT INTO akilimo_users (
                    user_name, farm_country, farm_size_unit, farm_size, latitude, longitude,
                    altitude, zoom_level, send_email, send_sms, risk_att, planting_flex,
                    harvest_flex, provided_alternative_date, tillage_operations
                ) VALUES ('farmer', 'NG', 'ACRE', 1.0, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, '[]')
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(
            dbName, 6, true,
            DatabaseMigrations.MIGRATION_4_5, DatabaseMigrations.MIGRATION_5_6,
        ).use { db ->
            db.query("SELECT weed_control_method, recommendation_language FROM akilimo_users").use { c ->
                c.moveToFirst()
                assertNull(c.getString(0))
                assertEquals("en", c.getString(1))
            }
        }
    }
}
