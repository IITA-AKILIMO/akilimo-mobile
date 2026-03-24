package com.akilimo.mobile.ui.screens.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets as LayoutWindowInsets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.OnboardingRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.WizardBottomBar
import com.akilimo.mobile.ui.screens.onboarding.steps.DisclaimerStep
import com.akilimo.mobile.ui.screens.onboarding.steps.TermsStep
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalWizardScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = listOf("DISCLAIMER", "TERMS")

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // If already read/accepted, we might want to skip, but usually MainActivity handles this.
    // If the user finishes both, we navigate to Onboarding.

    val disclaimerError = stringResource(R.string.lbl_disclaimer_agreement_required)
    val termsError = stringResource(R.string.lbl_terms_agreement_required)

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_initial_onboarding),
                onBack = {
                    if (currentStepIndex > 0) {
                        currentStepIndex--
                    } else {
                        // Maybe exit app or go back?
                        navController.popBackStack()
                    }
                }
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = currentStepIndex,
                totalSteps = steps.size,
                isFirstStep = currentStepIndex == 0,
                isLastStep = currentStepIndex == steps.size - 1,
                onBack = { currentStepIndex-- },
                onNext = {
                    if (currentStepIndex == 0) {
                        if (state.disclaimerRead) {
                            currentStepIndex++
                        } else {
                            viewModel.onEvent(OnboardingViewModel.Event.ShowError("disclaimer", disclaimerError))
                        }
                    } else if (currentStepIndex == 1) {
                        if (state.termsAccepted) {
                            navController.navigate(OnboardingRoute) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        } else {
                            viewModel.onEvent(OnboardingViewModel.Event.ShowError("terms", termsError))
                        }
                    }
                }
            )
        },
        contentWindowInsets = LayoutWindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentStepIndex) {
                0 -> DisclaimerStep(
                    disclaimerRead = state.disclaimerRead,
                    error = state.errors["disclaimer"],
                    onEvent = viewModel::onEvent
                )
                1 -> TermsStep(
                    termsAccepted = state.termsAccepted,
                    termsUrl = state.termsUrl,
                    error = state.errors["terms"],
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}
