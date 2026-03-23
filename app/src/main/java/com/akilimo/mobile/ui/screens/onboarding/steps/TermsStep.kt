package com.akilimo.mobile.ui.screens.onboarding.steps

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun TermsStep(
    termsAccepted: Boolean,
    termsUrl: String,
    error: String?,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.lbl_agree_to_terms),
            style = MaterialTheme.typography.headlineMedium,
        )
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    if (termsUrl.isNotBlank()) loadUrl(termsUrl)
                }
            },
            update = { webView ->
                if (termsUrl.isNotBlank() && webView.url != termsUrl) {
                    webView.loadUrl(termsUrl)
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { onEvent(OnboardingViewModel.Event.TermsChecked(it)) },
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.lbl_accept_terms_prompt))
        }
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
