package com.akilimo.mobile.ui.activities

import androidx.navigation.findNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.akilimo.mobile.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented regression tests for back navigation in the recommendations graph.
 *
 * These tests directly catch the bug where BaseActivity's OnBackPressedCallback
 * (registered after NavHostFragment's, so LIFO-wins) was calling finish() on every
 * system back press instead of delegating to NavController.
 *
 * Regression: if handleBackPressed() is removed from RecommendationsActivity,
 * pressBack() from a sub-screen will finish the Activity instead of popping to
 * the previous destination.
 *
 * No @HiltAndroidTest needed — we don't inject into the test class.
 * HiltTestRunner already provides HiltTestApplication so @AndroidEntryPoint
 * components in the launched Activity work without further setup here.
 */
@RunWith(AndroidJUnit4::class)
class RecommendationsBackNavigationTest {

    @Test
    fun pressBack_fromFrFragment_returnsToRecommendationsFragment() {
        ActivityScenario.launch(RecommendationsActivity::class.java).use { scenario ->
            // Navigate to the FR sub-screen
            scenario.onActivity { activity ->
                activity.findNavController(R.id.nav_host_recommendations)
                    .navigate(R.id.action_recommendations_to_fr)
            }

            // Simulate system back press
            pressBack()

            // Must land on the summary screen, not finish the Activity
            scenario.onActivity { activity ->
                val current = activity.findNavController(R.id.nav_host_recommendations)
                    .currentDestination?.id
                assertEquals(
                    "Back from FrFragment should return to RecommendationsFragment",
                    R.id.recommendationsFragment, current
                )
            }
        }
    }

    @Test
    fun pressBack_fromBppFragment_returnsToRecommendationsFragment() {
        ActivityScenario.launch(RecommendationsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findNavController(R.id.nav_host_recommendations)
                    .navigate(R.id.action_recommendations_to_bpp)
            }

            pressBack()

            scenario.onActivity { activity ->
                val current = activity.findNavController(R.id.nav_host_recommendations)
                    .currentDestination?.id
                assertEquals(R.id.recommendationsFragment, current)
            }
        }
    }

    @Test
    fun pressBack_fromSphFragment_returnsToRecommendationsFragment() {
        ActivityScenario.launch(RecommendationsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findNavController(R.id.nav_host_recommendations)
                    .navigate(R.id.action_recommendations_to_sph)
            }

            pressBack()

            scenario.onActivity { activity ->
                val current = activity.findNavController(R.id.nav_host_recommendations)
                    .currentDestination?.id
                assertEquals(R.id.recommendationsFragment, current)
            }
        }
    }

    @Test
    fun pressBack_fromIcMaizeFragment_returnsToRecommendationsFragment() {
        ActivityScenario.launch(RecommendationsActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findNavController(R.id.nav_host_recommendations)
                    .navigate(R.id.action_recommendations_to_icMaize)
            }

            pressBack()

            scenario.onActivity { activity ->
                val current = activity.findNavController(R.id.nav_host_recommendations)
                    .currentDestination?.id
                assertEquals(R.id.recommendationsFragment, current)
            }
        }
    }

    @Test
    fun pressBack_atRecommendationsFragment_finishesActivity() {
        ActivityScenario.launch(RecommendationsActivity::class.java).use { scenario ->
            // At start destination — back should finish the Activity
            pressBack()

            // ActivityScenario.getState() returns DESTROYED when the activity finishes
            assertEquals(
                androidx.lifecycle.Lifecycle.State.DESTROYED,
                scenario.state
            )
        }
    }
}
