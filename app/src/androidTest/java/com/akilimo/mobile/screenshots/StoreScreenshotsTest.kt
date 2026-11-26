package com.akilimo.mobile.screenshots

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akilimo.mobile.R
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.database.AppDatabase
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.ui.activities.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleUtil
import java.util.Locale
import javax.inject.Inject

/**
 * Captures the Play Store listing screenshots with fastlane screengrab (see `fastlane/Fastfile`,
 * lane `screenshots`). The device locale is never changed (no `LocaleTestRule`), so it also runs on
 * a real phone; the language comes from the `testLocale` argument screengrab passes and is applied
 * through the app's own language setting.
 *
 * The app starts past onboarding with a seeded demo farmer, in the language screengrab is running.
 * Screens that need the network (advice results) are deliberately left out.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class StoreScreenshotsTest {

    private val hiltRule = HiltAndroidRule(this)
    private val compose = createEmptyComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(hiltRule).around(compose)

    private lateinit var activity: MainActivity
    private var shotCount = 0

    @Inject
    lateinit var settings: AppSettingsDataStore

    @Inject
    lateinit var db: AppDatabase

    // Screengrab hands over a tag such as "en-GB"; the app stores plain BCP-47 languages
    private val language: String =
        Locale.forLanguageTag(LocaleUtil.getTestLocale().replace('_', '-')).language.ifBlank { "en" }

    @Before
    fun seed() {
        hiltRule.inject()
        Screengrab.setDefaultScreenshotStrategy(PlayScreenshotStrategy())
        val tag = language
        runBlocking {
            db.akilimoUserDao().insert(DemoData.user(tag))
            DemoData.seedReferenceData(db)
            DemoData.seedMessages(db)
            settings.setLanguageTagAndOverride(tag)
            settings.setPushPromptAnswered(true)
        }
        settings.akilimoUser = DemoData.USER_NAME
        settings.disclaimerRead = true
        settings.termsAccepted = true
        settings.isFirstRun = false
        // Settings writes are asynchronous and MainActivity reads them in onCreate
        check(waitUntil { !settings.isFirstRun }) { "Settings were not written in time" }
    }

    @Test
    fun captureStoreScreenshots() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // The activity's resources carry the app language; the target context follows the phone's
            scenario.onActivity { activity = it }
            val text = { id: Int -> activity.getString(id) }

            shot("hub")

            open(text(R.string.lbl_recommendations))
            shot("recommendations")
            // Each advice type: its step list, then every form behind it. Steps shared by several
            // advice types (markets, yield, dates) are captured once.
            val seenSteps = mutableSetOf<String>()
            ADVICE_TOURS.forEach { (advice, steps) ->
                open(advice.title(activity))
                shot("advice_${advice.name.lowercase()}")
                steps.forEach { step ->
                    val label = step.label(activity)
                    if (seenSteps.add(label)) {
                        open(label)
                        // The market form is empty until a tab is chosen
                        if (step == EnumAdviceTask.CASSAVA_MARKET_OUTLET) open(text(R.string.lbl_starch_factory))
                        shot("step_${step.name.lowercase()}")
                        Espresso.pressBack()
                    }
                }
                Espresso.pressBack()
            }
            Espresso.pressBack()

            open(text(R.string.lbl_profile))
            shot("profile")
            Espresso.pressBack()

            open(text(R.string.lbl_your_farm))
            shot("farm")
            // The farm's location section holds the button that opens the map
            open(text(R.string.lbl_manual_location))
            shot("map_picker", extraSettleMs = MAP_TILES_MS)
            Espresso.pressBack()
            Espresso.pressBack()

            open(text(R.string.inbox_title))
            shot("inbox")
            Espresso.pressBack()

            open(text(R.string.lbl_settings))
            shot("settings")
        }
    }

    /** Taps the first node with this text, scrolling to it first when it is off screen. */
    private fun open(label: String) {
        if (compose.onAllNodesWithText(label).fetchSemanticsNodes().isEmpty()) {
            compose.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(label))
        }
        val node = compose.onAllNodesWithText(label).onFirst()
        // Invoke the click action itself: a touch at the node's position can land on the bottom bar
        // ("Get recommendations") when the last list item sits underneath it
        if (node.fetchSemanticsNode().config.contains(SemanticsActions.OnClick)) {
            node.performSemanticsAction(SemanticsActions.OnClick)
        } else {
            node.performClick()
        }
        compose.waitForIdle()
    }

    private fun shot(name: String, extraSettleMs: Long = 0) {
        // waitForIdle does not cover screen transitions and loading animations, so let them finish
        compose.waitForIdle()
        Thread.sleep(SETTLE_MS + extraSettleMs)
        compose.waitForIdle()
        Screengrab.screenshot("%02d_%s".format(++shotCount, name))
        // Pause again so the next navigation starts from a still screen
        Thread.sleep(PAUSE_AFTER_SHOT_MS)
    }

    private fun waitUntil(timeoutMs: Long = 5_000, condition: () -> Boolean): Boolean {
        val end = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < end) {
            if (condition()) return true
            Thread.sleep(100)
        }
        return false
    }

    companion object {
        /** Mirrors the step lists in FeatureGraphs.kt for the advice types that work offline. */
        private val ADVICE_TOURS = listOf(
            EnumAdvice.FERTILIZER_RECOMMENDATIONS to listOf(
                EnumAdviceTask.AVAILABLE_FERTILIZERS,
                EnumAdviceTask.INVESTMENT_AMOUNT,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
            ),
            EnumAdvice.BEST_PLANTING_PRACTICES to listOf(
                EnumAdviceTask.PLANTING_AND_HARVEST,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
                EnumAdviceTask.MANUAL_TILLAGE_COST,
                EnumAdviceTask.TRACTOR_ACCESS,
                EnumAdviceTask.COST_OF_WEED_CONTROL,
            ),
            EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH to listOf(
                EnumAdviceTask.PLANTING_AND_HARVEST,
                EnumAdviceTask.CASSAVA_MARKET_OUTLET,
                EnumAdviceTask.CURRENT_CASSAVA_YIELD,
            ),
        )

        /** Wait before each capture for transitions to end. Raise it if shots still catch motion. */
        private const val SETTLE_MS = 2_000L
        private const val PAUSE_AFTER_SHOT_MS = 1_000L

        /** Extra wait on the map picker for the tiles to download. */
        private const val MAP_TILES_MS = 15_000L
    }
}
