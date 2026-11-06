package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.screens.settings.AkilimoWebView
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.lbl_agree_to_terms),
            style = MaterialTheme.typography.headlineMedium,
        )
        // Loading bar, error view with retry, dark-theme aware
        AkilimoWebView(
            url = termsUrl,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = termsAccepted,
                    role = Role.Checkbox,
                    onValueChange = { onEvent(OnboardingViewModel.Event.TermsChecked(it)) },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = termsAccepted, onCheckedChange = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.lbl_accept_terms_prompt))
        }
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}
