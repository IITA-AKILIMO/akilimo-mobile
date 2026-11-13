package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.cardSurface
import com.akilimo.mobile.ui.screens.settings.AkilimoWebView
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

/** The page title is shown by the wizard's page header, so this holds the terms and the agreement. */
@Composable
fun TermsStep(
    termsAccepted: Boolean,
    termsUrl: String,
    error: String?,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(AkilimoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .cardSurface()
                .clip(MaterialTheme.shapes.medium),
        ) {
            // Loading bar, error view with retry, dark-theme aware
            AkilimoWebView(url = termsUrl, modifier = Modifier.fillMaxSize())
        }
        AgreementCheckRow(
            checked = termsAccepted,
            label = stringResource(R.string.lbl_accept_terms_prompt),
            onCheckedChange = { onEvent(OnboardingViewModel.Event.TermsChecked(it)) },
        )
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
