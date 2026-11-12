package com.akilimo.mobile.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.screens.onboarding.BioDataSection
import com.akilimo.mobile.ui.screens.onboarding.CountryChangeDialog
import com.akilimo.mobile.ui.screens.onboarding.FarmLocationSection
import com.akilimo.mobile.ui.screens.onboarding.FieldSection
import com.akilimo.mobile.ui.screens.onboarding.LocationResultEffect
import com.akilimo.mobile.ui.screens.onboarding.OnboardingSection
import com.akilimo.mobile.ui.screens.onboarding.PracticesSection
import com.akilimo.mobile.ui.screens.onboarding.reportValidationErrors
import com.akilimo.mobile.ui.screens.onboarding.sectionModifierFor
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The farmer's profile on one scrolling page, one card per section, with a single Save: about you,
 * farm location, field size and investment preference. It reuses the onboarding sections and view
 * model, so the same fields and validation apply. Planting and harvest dates, tillage and weed
 * control are edited in their own recommendation steps, so they are not repeated here; first-run
 * setup stays in the onboarding wizard.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sectionRequesters = remember {
        OnboardingSection.entries.associateWith { BringIntoViewRequester() }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                // Saved: back to the hub
                is OnboardingViewModel.Effect.NavigateToRecommendations,
                is OnboardingViewModel.Effect.Close,
                is OnboardingViewModel.Effect.ExitApp -> navController.popBackStackSafely()

                is OnboardingViewModel.Effect.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }

                is OnboardingViewModel.Effect.ShowValidationErrors ->
                    scope.launch { reportValidationErrors(effect, sectionRequesters, snackbarHostState, context) }
            }
        }
    }

    LocationResultEffect(navController, viewModel)
    CountryChangeDialog(state, viewModel)

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val sectionModifier = sectionModifierFor(sectionRequesters)

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_profile),
                subtitle = stringResource(R.string.lbl_edit_profile),
                onBack = { navController.popBackStackSafely() },
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = stringResource(R.string.lbl_save),
                onClick = { viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(AkilimoSpacing.xs))
            BioDataSection(
                state = state,
                phoneCountries = viewModel.phoneCountries,
                suggestedPhoneRegions = viewModel.suggestedPhoneRegions,
                onEvent = viewModel::onEvent,
                section = sectionModifier,
            )
            FarmLocationSection(state, viewModel::onEvent, navController, sectionModifier)
            FieldSection(state, viewModel::onEvent, sectionModifier, includeDates = false)
            PracticesSection(state, viewModel::onEvent, sectionModifier, includeTillage = false)
            Spacer(Modifier.height(AkilimoSpacing.md))
        }
    }
}
