package com.akilimo.mobile.ui.activities

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.data.AppLanguage
import com.akilimo.mobile.data.ThemeMode
import com.akilimo.mobile.navigation.AkilimoNavHost
import com.akilimo.mobile.navigation.LegalWizardRoute
import com.akilimo.mobile.navigation.HubRoute
import com.akilimo.mobile.navigation.OnboardingRoute
import com.akilimo.mobile.ui.theme.AkilimoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// Same scrims androidx.activity uses for 3-button navigation on older Android versions
private val LIGHT_NAV_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_NAV_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

/**
 * Single-Activity host for the AKILIMO app.
 *
 * Hosts [AkilimoNavHost] which owns the entire navigation graph.
 * All screen-to-screen navigation happens through the NavController
 * inside [AkilimoNavHost] — no more startActivity() calls.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appSettings: AppSettingsDataStore

    override fun attachBaseContext(newBase: Context) {
        // Runs before Hilt injection, so read settings directly
        val tag = AppSettingsDataStore.readAppliedLanguageTagSync(newBase)
        super.attachBaseContext(AppLanguage.wrap(newBase, tag))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Re-applied from Compose below once the theme is known
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)

        val startRoute = if (appSettings.isFirstRun) {
            if (!appSettings.disclaimerRead || !appSettings.termsAccepted) {
                LegalWizardRoute
            } else {
                OnboardingRoute
            }
        } else {
            HubRoute
        }

        setContent {
            val themeMode by appSettings.themeModeFlow
                .collectAsStateWithLifecycle(initialValue = appSettings.getThemeModeSync())
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Status bar icons sit on the top app bar, which is filled with colorScheme.primary:
            // dark green with white text in the light theme, light green with dark text in the dark one.
            // The automatic style would follow the system theme instead and can pick the wrong icons.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (darkTheme) {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (darkTheme) {
                        SystemBarStyle.dark(DARK_NAV_SCRIM)
                    } else {
                        SystemBarStyle.light(LIGHT_NAV_SCRIM, DARK_NAV_SCRIM)
                    },
                )
                onDispose { }
            }

            AkilimoTheme(darkTheme = darkTheme) {
                AkilimoNavHost(startDestination = startRoute)
            }
        }
    }
}
