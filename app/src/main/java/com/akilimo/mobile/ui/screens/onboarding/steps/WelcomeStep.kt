package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.BrandHeader
import com.akilimo.mobile.ui.components.compose.InfoCard
import com.akilimo.mobile.ui.components.compose.InfoCardType
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun WelcomeStep(
    languageCode: String,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locales = Locales.supportedLocales
    val selectedLocale = locales.find { it.toLanguageTag() == languageCode } ?: Locales.english

    Column(modifier = modifier) {
        BrandHeader(
            title = stringResource(R.string.welcome_title),
            subtitle = stringResource(R.string.welcome_subtitle),
        )

        Column(
            modifier = Modifier
                .padding(AkilimoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.md),
        ) {
            InfoCard(
                title = stringResource(R.string.lbl_select_language),
                message = stringResource(R.string.msg_language_selection_impact),
                icon = Icons.Outlined.Language,
                type = InfoCardType.Info,
            )
            AkilimoDropdown(
                label = stringResource(R.string.lbl_language),
                options = locales,
                selectedOption = selectedLocale,
                onOptionSelected = { locale ->
                    val tag = locale.toLanguageTag()
                    onEvent(OnboardingViewModel.Event.LanguageSelected(tag))
                },
                displayText = { locale ->
                    locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercaseChar() }
                },
            )
        }
    }
}
