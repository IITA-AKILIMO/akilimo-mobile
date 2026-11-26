package com.akilimo.mobile

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner that replaces the application class with [HiltTestApplication]
 * so that Hilt's dependency injection works in all @HiltAndroidTest instrumented tests.
 *
 * Referenced by testInstrumentationRunner in build.gradle.kts.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)

    override fun callApplicationOnCreate(app: Application) {
        super.callApplicationOnCreate(app)
        // The manifest removes WorkManager's auto-initializer and HiltTestApplication is not a
        // Configuration.Provider. Jobs left scheduled by an earlier run start SystemJobService as soon as
        // the process does, before any test code, so WorkManager has to be ready here.
        WorkManager.initialize(app, Configuration.Builder().build())
    }
}
