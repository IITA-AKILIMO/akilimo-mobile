package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.BrandHeader

@Composable
fun WelcomeStep(modifier: Modifier = Modifier) {
    BrandHeader(
        title = stringResource(R.string.welcome_title),
        subtitle = stringResource(R.string.welcome_subtitle),
        modifier = modifier,
    )
}
