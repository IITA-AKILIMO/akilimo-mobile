package com.akilimo.mobile

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
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
}
