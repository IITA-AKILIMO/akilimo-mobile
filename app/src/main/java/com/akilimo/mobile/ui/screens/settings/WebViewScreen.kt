package com.akilimo.mobile.ui.screens.settings

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.WebViewRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.popBackStackSafely

private const val PROGRESS_MAX = 100f
private const val DARK_LUMINANCE_THRESHOLD = 0.5f

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
                onBack = { navController.popBackStackSafely() },
            )
        },
    ) { paddingValues ->
        AkilimoWebView(
            url = route.url,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

/**
 * A WebView with a loading bar, an error view with Retry (main-frame failures only), system Back
 * going back through visited pages, and algorithmic darkening in a dark theme (Android 13+).
 */
@Composable
fun AkilimoWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var loadFailed by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    // The app theme can differ from the system one, so look at the colours actually in use
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < DARK_LUMINANCE_THRESHOLD
    val backgroundColor = MaterialTheme.colorScheme.background.toArgb()

    BackHandler(enabled = canGoBack) { webView?.goBack() }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(backgroundColor)
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress / PROGRESS_MAX
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                            canGoBack = view?.canGoBack() == true
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame == true) loadFailed = true
                        }
                    }
                    if (darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        settings.isAlgorithmicDarkeningAllowed = true
                    }
                    if (url.isNotBlank()) loadUrl(url)
                    webView = this
                }
            },
            update = { view ->
                if (url.isNotBlank() && view.url == null && !loadFailed) view.loadUrl(url)
            },
        )

        if (isLoading && !loadFailed) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
            )
        }

        if (loadFailed) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.lbl_page_load_failed),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = {
                    loadFailed = false
                    progress = 0f
                    webView?.reload() ?: Unit
                }) {
                    Text(stringResource(R.string.lbl_retry))
                }
            }
        }
    }
}
