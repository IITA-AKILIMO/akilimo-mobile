package com.akilimo.mobile.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.akilimo.mobile.data.AppSettingsDataStore
import javax.inject.Inject
import androidx.compose.ui.platform.LocalContext
import com.akilimo.mobile.navigation.AkilimoNavHost
import com.akilimo.mobile.navigation.LegalWizardRoute
import com.akilimo.mobile.navigation.OnboardingRoute
import com.akilimo.mobile.navigation.RecommendationsRoute
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import com.akilimo.mobile.ui.theme.AkilimoTheme
import dagger.hilt.android.AndroidEntryPoint

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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val startRoute = if (appSettings.isFirstRun) {
            if (!appSettings.disclaimerRead || !appSettings.termsAccepted) {
                LegalWizardRoute
            } else {
                OnboardingRoute
            }
        } else {
            RecommendationsRoute
        }

        setContent {
            AkilimoTheme {
                AkilimoNavHost(startDestination = startRoute)
            }
        }
    }
}
