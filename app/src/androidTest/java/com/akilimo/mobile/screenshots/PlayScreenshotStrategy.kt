package com.akilimo.mobile.screenshots

import android.content.Context
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import tools.fastlane.screengrab.ScreenshotCallback
import tools.fastlane.screengrab.ScreenshotStrategy

/**
 * Takes the screenshot and trims what the Play Store listing should not show: the phone's own status
 * bar (clock, battery and notification icons) and the navigation bar. Play also rejects images whose
 * long side is more than twice the short side, and a tall phone screen (1080 x 2340) is over that, so
 * whatever is still too tall is trimmed from the bottom, below the last content.
 */
class PlayScreenshotStrategy : ScreenshotStrategy {

    override fun takeScreenshot(screenshotName: String, screenshotCallback: ScreenshotCallback) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val full = instrumentation.uiAutomation.takeScreenshot()
            ?: error("Could not take a screenshot")
        val context = instrumentation.targetContext

        val top = systemBarHeight(context, "status_bar_height")
        val bottom = systemBarHeight(context, "navigation_bar_height")
        val maxHeight = full.width * MAX_ASPECT
        val height = (full.height - top - bottom).coerceAtMost(maxHeight)
        require(height > 0) { "Nothing left after trimming the system bars" }

        screenshotCallback.screenshotCaptured(
            screenshotName,
            Bitmap.createBitmap(full, 0, top, full.width, height),
        )
    }

    private fun systemBarHeight(context: Context, name: String): Int {
        val id = context.resources.getIdentifier(name, "dimen", "android")
        return if (id > 0) context.resources.getDimensionPixelSize(id) else 0
    }

    private companion object {
        /** Play's limit: the long side may be at most this many times the short side. */
        const val MAX_ASPECT = 2
    }
}
