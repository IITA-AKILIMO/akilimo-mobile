package com.akilimo.mobile.ui.screens.settings

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.akilimo.mobile.navigation.WebViewRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(
    route: WebViewRoute,
    navController: NavHostController,
) {
    Scaffold(
        topBar = {
            BackTopAppBar(
                title = route.title,
                onBack = { navController.popBackStack() },
            )
        },
    ) { paddingValues ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    loadUrl(route.url)
                }
            },
        )
    }
}
