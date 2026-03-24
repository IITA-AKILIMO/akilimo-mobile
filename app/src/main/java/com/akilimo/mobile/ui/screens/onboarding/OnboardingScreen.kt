package com.akilimo.mobile.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.RecommendationsRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ExitConfirmDialog
import com.akilimo.mobile.ui.components.compose.WizardBottomBar
import com.akilimo.mobile.ui.screens.onboarding.steps.AreaUnitStep
import com.akilimo.mobile.ui.screens.onboarding.steps.BioDataStep
import com.akilimo.mobile.ui.screens.onboarding.steps.CountryStep
import com.akilimo.mobile.ui.screens.onboarding.steps.DisclaimerStep
import com.akilimo.mobile.ui.screens.onboarding.steps.InvestmentPrefStep
import com.akilimo.mobile.ui.screens.onboarding.steps.LocationStep
import com.akilimo.mobile.ui.screens.onboarding.steps.PlantingDateStep
import com.akilimo.mobile.ui.screens.onboarding.steps.SummaryStep
import com.akilimo.mobile.ui.screens.onboarding.steps.TermsStep
import com.akilimo.mobile.ui.screens.onboarding.steps.TillageStep
import com.akilimo.mobile.ui.screens.onboarding.steps.WelcomeStep
import com.akilimo.mobile.ui.screens.settings.LocationResult
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.wizard.OnboardingStep
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.flow.collectLatest
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val stepProgress by animateFloatAsState(
        targetValue = if (state.visibleSteps.isNotEmpty())
            (state.currentStepIndex + 1f) / state.visibleSteps.size
        else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "OnboardingProgress",
    )

    // Collect one-shot effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OnboardingViewModel.Effect.NavigateToRecommendations ->
                    navController.navigate(RecommendationsRoute) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                is OnboardingViewModel.Effect.ExitApp -> showExitDialog = true
                is OnboardingViewModel.Effect.LanguageChangeRequested -> {
                    val locale = Locales.supportedLocales
                        .find { it.toLanguageTag() == effect.languageTag }
                        ?: Locales.english
                    AppLocale.desiredLocale = locale
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(effect.languageTag),
                    )
                }
                else -> Unit
            }
        }
    }

    // Observe location results returned from LocationPickerScreen via savedStateHandle
    LaunchedEffect(Unit) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<LocationResult?>("location_result", null)
            ?.collectLatest { result ->
                if (result != null) {
                    viewModel.onEvent(
                        OnboardingViewModel.Event.LocationUpdated(result.lat, result.lon, result.zoom)
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.remove<LocationResult>("location_result")
                }
            }
    }

    BackHandler {
        viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
    }

    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirm = {
                showExitDialog = false
                (context as? android.app.Activity)?.finish()
                System.gc()
                exitProcess(0)
            },
            onDismiss = { showExitDialog = false },
        )
    }

    if (state.isLoading) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            if (state.currentStep != OnboardingStep.WELCOME) {
                Column {
                    BackTopAppBar(
                        title = stepTitle(state.currentStep),
                        onBack = { viewModel.onEvent(OnboardingViewModel.Event.BackClicked) },
                    )
                    LinearProgressIndicator(
                        progress = { stepProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = state.currentStepIndex,
                totalSteps = state.visibleSteps.size,
                isFirstStep = state.isFirstStep,
                isLastStep = state.isLastStep,
                onBack = { viewModel.onEvent(OnboardingViewModel.Event.BackClicked) },
                onNext = { viewModel.onEvent(OnboardingViewModel.Event.NextClicked) },
            )
        },
    ) { paddingValues ->
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            label = "WizardStep",
        ) { step ->
            when (step) {
                OnboardingStep.WELCOME -> WelcomeStep(
                    languageCode = state.languageCode,
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.DISCLAIMER -> DisclaimerStep(
                    disclaimerRead = state.disclaimerRead,
                    error = state.errors["disclaimer"],
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.TERMS -> TermsStep(
                    termsAccepted = state.termsAccepted,
                    termsUrl = state.termsUrl,
                    error = state.errors["terms"],
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.BIO_DATA -> BioDataStep(
                    firstName = state.firstName,
                    lastName = state.lastName,
                    email = state.email,
                    phone = state.phone,
                    gender = state.gender,
                    interest = state.interest,
                    errors = state.errors,
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.COUNTRY -> CountryStep(
                    country = state.country,
                    error = state.errors["country"],
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.LOCATION -> LocationStep(
                    latitude = state.latitude,
                    longitude = state.longitude,
                    altitude = state.altitude,
                    zoomLevel = state.zoomLevel,
                    onEvent = viewModel::onEvent,
                    navController = navController,
                )
                OnboardingStep.AREA_UNIT -> AreaUnitStep(
                    areaUnit = state.areaUnit,
                    farmSize = state.farmSize,
                    customFarmSize = state.customFarmSize,
                    rememberAreaUnit = state.rememberAreaUnit,
                    country = state.country,
                    errors = state.errors,
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.PLANTING_DATE -> PlantingDateStep(
                    plantingDate = state.plantingDate,
                    harvestDate = state.harvestDate,
                    plantingFlex = state.plantingFlex,
                    harvestFlex = state.harvestFlex,
                    showFlexOptions = state.showFlexOptions,
                    errors = state.errors,
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.TILLAGE -> TillageStep(
                    tillageOperations = state.tillageOperations,
                    weedControlEnabled = state.weedControlEnabled,
                    weedControlMethod = state.weedControlMethod,
                    errors = state.errors,
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.INVESTMENT_PREF -> InvestmentPrefStep(
                    investmentPref = state.investmentPref,
                    error = state.errors["investmentPref"],
                    onEvent = viewModel::onEvent,
                )
                OnboardingStep.SUMMARY -> SummaryStep(state = state)
            }
        }
    }
}

@Composable
private fun stepTitle(step: OnboardingStep): String = stringResource(
    when (step) {
        OnboardingStep.WELCOME -> R.string.welcome_title
        OnboardingStep.DISCLAIMER -> R.string.lbl_disclaimer
        OnboardingStep.TERMS -> R.string.lbl_terms
        OnboardingStep.BIO_DATA -> R.string.lbl_self_intro
        OnboardingStep.COUNTRY -> R.string.lbl_country
        OnboardingStep.LOCATION -> R.string.lbl_location
        OnboardingStep.AREA_UNIT -> R.string.lbl_field
        OnboardingStep.PLANTING_DATE -> R.string.lbl_planting_harvest_dates
        OnboardingStep.TILLAGE -> R.string.lbl_tillage_operation
        OnboardingStep.INVESTMENT_PREF -> R.string.lbl_investment_pref_prompt
        OnboardingStep.SUMMARY -> R.string.lbl_summary_title
    }
)
