package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun WelcomeStep(
    languageCode: String,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locales = Locales.supportedLocales
    val selectedLocale = locales.find { it.toLanguageTag() == languageCode } ?: Locales.english

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.lbl_select_language),
            style = MaterialTheme.typography.bodyMedium,
        )
        AkilimoDropdown(
            label = stringResource(R.string.lbl_language),
            options = locales,
            selectedOption = selectedLocale,
            onOptionSelected = { locale ->
                val tag = locale.toLanguageTag()
                onEvent(OnboardingViewModel.Event.LanguageSelected(tag))
            },
            displayText = { locale -> locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercaseChar() } },
        )
    }
}
