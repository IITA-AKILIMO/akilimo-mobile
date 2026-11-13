package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.cardSurface
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

/** The page title is shown by the wizard's page header, so this holds the text and the agreement. */
@Composable
fun DisclaimerStep(
    disclaimerRead: Boolean,
    error: String?,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(AkilimoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .cardSurface()
                .verticalScroll(rememberScrollState())
                .padding(AkilimoSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.important_instructions),
                // Long read for every farmer: 18sp with generous line height
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 28.sp),
            )
        }
        AgreementCheckRow(
            checked = disclaimerRead,
            label = stringResource(R.string.lbl_agree_to_disclaimer),
            onCheckedChange = { onEvent(OnboardingViewModel.Event.DisclaimerChecked(it)) },
        )
        if (error != null) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
