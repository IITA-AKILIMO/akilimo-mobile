package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumInvestmentPref
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun InvestmentPrefStep(
    investmentPref: EnumInvestmentPref,
    error: String?,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val options = remember { EnumInvestmentPref.entries.filter { it != EnumInvestmentPref.Prompt } }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.lbl_investment_pref), style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(R.string.lbl_investment_pref_sub), style = MaterialTheme.typography.bodyMedium)
        AkilimoDropdown(
            label = stringResource(R.string.lbl_investment_pref_prompt),
            options = options,
            selectedOption = investmentPref.takeIf { it != EnumInvestmentPref.Prompt },
            onOptionSelected = { onEvent(OnboardingViewModel.Event.InvestmentPrefSelected(it)) },
            displayText = { it.label(context) },
            error = error,
        )
    }
}
