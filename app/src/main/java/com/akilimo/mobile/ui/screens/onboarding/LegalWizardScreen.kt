package com.akilimo.mobile.ui.screens.onboarding

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets as LayoutWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.OnboardingRoute
import com.akilimo.mobile.ui.components.compose.ExitConfirmDialog
import com.akilimo.mobile.ui.components.compose.WizardBottomBar
import com.akilimo.mobile.ui.screens.onboarding.steps.DisclaimerStep
import com.akilimo.mobile.ui.screens.onboarding.steps.TermsStep
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

private const val STEP_DISCLAIMER = 0
private const val STEP_TERMS = 1
private const val STEP_COUNT = 2

/**
 * First-run legal wizard: the disclaimer, then the terms. Each page is full screen under a green
 * page header (like the onboarding pages) with no app bar; Back and the step count live in the
 * bottom bar. Back on the first page asks before leaving the app.
 */
@Composable
fun LegalWizardScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // rememberSaveable: rotating on the Terms step must not jump back to the Disclaimer
    var currentStepIndex by rememberSaveable { mutableIntStateOf(STEP_DISCLAIMER) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // Back on the Terms step returns to the Disclaimer; on the first page it asks before exiting
    BackHandler {
        if (currentStepIndex > STEP_DISCLAIMER) currentStepIndex-- else showExitDialog = true
    }

    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirm = {
                showExitDialog = false
                // Finish normally rather than killing the process
                (context as? Activity)?.finish()
            },
            onDismiss = { showExitDialog = false },
        )
    }

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val disclaimerError = stringResource(R.string.lbl_disclaimer_agreement_required)
    val termsError = stringResource(R.string.lbl_terms_agreement_required)

    Scaffold(
        bottomBar = {
            WizardBottomBar(
                currentStep = currentStepIndex,
                totalSteps = STEP_COUNT,
                isFirstStep = currentStepIndex == STEP_DISCLAIMER,
                isLastStep = currentStepIndex == STEP_TERMS,
                onBack = { currentStepIndex-- },
                onNext = {
                    if (currentStepIndex == STEP_DISCLAIMER) {
                        if (state.disclaimerRead) {
                            currentStepIndex++
                        } else {
                            viewModel.onEvent(OnboardingViewModel.Event.ShowError("disclaimer", disclaimerError))
                        }
                    } else if (state.termsAccepted) {
                        navController.navigate(OnboardingRoute) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    } else {
                        viewModel.onEvent(OnboardingViewModel.Event.ShowError("terms", termsError))
                    }
                }
            )
        },
        // The page header draws under the status bar itself
        contentWindowInsets = LayoutWindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            OnboardingPageHeader(
                title = stringResource(
                    if (currentStepIndex == STEP_DISCLAIMER) R.string.important_instructions_title
                    else R.string.lbl_agree_to_terms
                ),
                subtitle = null,
                progress = (currentStepIndex + 1f) / STEP_COUNT,
            )
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                when (currentStepIndex) {
                    STEP_DISCLAIMER -> DisclaimerStep(
                        disclaimerRead = state.disclaimerRead,
                        error = state.errors["disclaimer"],
                        onEvent = viewModel::onEvent,
                        modifier = Modifier.fillMaxSize(),
                    )

                    STEP_TERMS -> TermsStep(
                        termsAccepted = state.termsAccepted,
                        termsUrl = state.termsUrl,
                        error = state.errors["terms"],
                        onEvent = viewModel::onEvent,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
