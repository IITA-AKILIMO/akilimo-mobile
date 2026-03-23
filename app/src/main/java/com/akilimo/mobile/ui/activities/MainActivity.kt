package com.akilimo.mobile.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.akilimo.mobile.navigation.AkilimoNavHost
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

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AkilimoTheme {
                AkilimoNavHost()
            }
        }
    }
}
