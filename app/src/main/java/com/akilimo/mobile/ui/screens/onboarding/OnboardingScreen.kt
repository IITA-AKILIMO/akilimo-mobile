package com.akilimo.mobile.ui.screens.onboarding

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.navigation.RecommendationsRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ExitConfirmDialog
import com.akilimo.mobile.ui.components.compose.TopBarNavigation
import com.akilimo.mobile.ui.components.compose.WizardBottomBar
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.screens.onboarding.steps.AreaUnitStep
import com.akilimo.mobile.ui.screens.onboarding.steps.BioDataStep
import com.akilimo.mobile.ui.screens.onboarding.steps.CountryStep
import com.akilimo.mobile.ui.screens.onboarding.steps.InvestmentPrefStep
import com.akilimo.mobile.ui.screens.onboarding.steps.LocationStep
import com.akilimo.mobile.ui.screens.onboarding.steps.PlantingDateStep
import com.akilimo.mobile.ui.screens.onboarding.steps.SummaryStep
import com.akilimo.mobile.ui.screens.onboarding.steps.TillageStep
import com.akilimo.mobile.ui.screens.onboarding.steps.WelcomeStep
import com.akilimo.mobile.ui.screens.settings.LocationResult
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** The section of the form that shows the field behind an error key. */
private fun sectionForField(field: String): OnboardingSection = when (field) {
    "country" -> OnboardingSection.COUNTRY
    "location" -> OnboardingSection.LOCATION
    "areaUnit", "farmSize" -> OnboardingSection.AREA_UNIT
    "plantingDate", "harvestDate" -> OnboardingSection.PLANTING_DATE
    "weedControl" -> OnboardingSection.TILLAGE
    "investmentPref" -> OnboardingSection.INVESTMENT_PREF
    else -> OnboardingSection.BIO_DATA
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
    appSettings: AppSettingsDataStore,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sectionRequesters = remember {
        OnboardingSection.entries.associateWith { BringIntoViewRequester() }
    }

    val isEditMode = remember { !appSettings.isFirstRun }

    // Collect one-shot effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OnboardingViewModel.Effect.NavigateToRecommendations ->
                    if (isEditMode) {
                        navController.popBackStackSafely()
                    } else {
                        navController.navigate(RecommendationsRoute) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }

                is OnboardingViewModel.Effect.ExitApp -> showExitDialog = true

                is OnboardingViewModel.Effect.Close -> navController.popBackStackSafely()

                is OnboardingViewModel.Effect.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }

                is OnboardingViewModel.Effect.ShowValidationErrors -> {
                    // The first failing field may be far off-screen: bring its section into view
                    scope.launch {
                        // Submit may have just switched wizard pages: let the page compose first
                        withFrameNanos { }
                        sectionRequesters[sectionForField(effect.firstField)]?.bringIntoView()
                    }
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.resources.getQuantityString(
                                R.plurals.msg_onboarding_errors, effect.count, effect.count,
                            )
                        )
                    }
                }
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
                        OnboardingViewModel.Event.LocationUpdated(
                            result.lat,
                            result.lon,
                            result.zoom
                        )
                    )
                    navController.currentBackStackEntry?.savedStateHandle?.remove<LocationResult>("location_result")
                }
            }
    }

    // Previous page; on page 1 the VM closes (editing) or asks to exit (first run)
    BackHandler { viewModel.onEvent(OnboardingViewModel.Event.BackClicked) }

    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirm = {
                showExitDialog = false
                // Finish normally; killing the process (exitProcess) would also kill
                // in-flight WorkManager syncs and skip lifecycle callbacks.
                (context as? android.app.Activity)?.finish()
            },
            onDismiss = { showExitDialog = false },
        )
    }

    state.pendingCountry?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(OnboardingViewModel.Event.CountryChangeCancelled) },
            title = { Text(stringResource(R.string.lbl_change_country_title)) },
            text = { Text(stringResource(R.string.msg_change_country_clears_fertilizers)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(OnboardingViewModel.Event.CountryChangeConfirmed) }) {
                    Text(stringResource(R.string.lbl_change_country))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(OnboardingViewModel.Event.CountryChangeCancelled) }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            },
        )
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val sectionModifier: (OnboardingSection) -> Modifier = { section ->
        Modifier.bringIntoViewRequester(sectionRequesters.getValue(section))
    }
    val step = OnboardingStep.entries[state.stepIndex.coerceIn(0, OnboardingStep.entries.lastIndex)]
    // Collapsing bar: the page title sits large in the expanded area, and only moves into the
    // toolbar row once the page is scrolled up. A fresh state per page starts each page expanded.
    val appBarState = key(step) { rememberTopAppBarState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    val pageTitle = stringResource(step.titleRes)
    // Each page starts at the top; a fresh scroll state per page does that
    val pageScroll = key(step) { rememberScrollState() }

    // First-run page 1 opens on the welcome banner, which already acts as the hero. A large
    // title above it would stack two green headers, so this page uses the slim bar instead and
    // keeps its title hidden until most of the banner has scrolled away.
    val isWelcomePage = !isEditMode && step == OnboardingStep.ABOUT_YOU
    var bannerHeightPx by remember { mutableIntStateOf(0) }
    val bannerScrolledAway by remember(pageScroll) {
        derivedStateOf { bannerHeightPx > 0 && pageScroll.value >= bannerHeightPx * BANNER_GONE_FRACTION }
    }

    Scaffold(
        modifier = if (isWelcomePage) Modifier else Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = pageTitle,
                subtitle = if (isEditMode) stringResource(R.string.lbl_edit_profile) else null,
                collapsedTitle = if (isWelcomePage) null else pageTitle,
                scrollBehavior = if (isWelcomePage) null else scrollBehavior,
                titleVisible = !isWelcomePage || bannerScrolledAway,
                // The bottom bar's Back moves between pages, so the top bar doesn't repeat it:
                // editing gets a ✕ that leaves without saving; first run has nowhere to go up to
                // (the phone's back button still steps back, and asks to exit on page 1)
                navigation = if (isEditMode) TopBarNavigation.CLOSE else TopBarNavigation.NONE,
                onBack = { navController.popBackStackSafely() },
                actions = {
                    // A returning user changing one answer shouldn't have to page to the end
                    if (isEditMode && step != OnboardingStep.REVIEW) {
                        TextButton(
                            onClick = { viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text(stringResource(R.string.lbl_save))
                        }
                    }
                },
            )
        },
        bottomBar = {
            WizardBottomBar(
                currentStep = state.stepIndex,
                totalSteps = OnboardingStep.entries.size,
                isFirstStep = state.stepIndex == 0,
                isLastStep = step == OnboardingStep.REVIEW,
                onBack = { viewModel.onEvent(OnboardingViewModel.Event.BackClicked) },
                onNext = { viewModel.onEvent(OnboardingViewModel.Event.NextClicked) },
                finishLabel = if (isEditMode) stringResource(R.string.lbl_save) else null,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(pageScroll),
        ) {
            when (step) {
                OnboardingStep.ABOUT_YOU -> {
                    // The welcome banner is for new users only
                    if (!isEditMode) {
                        WelcomeStep(modifier = Modifier.onSizeChanged { bannerHeightPx = it.height })
                    }
                    BioDataSection(state, viewModel::onEvent, sectionModifier)
                }

                OnboardingStep.FARM_LOCATION ->
                    FarmLocationSection(state, viewModel::onEvent, navController, sectionModifier)

                OnboardingStep.FIELD -> FieldSection(state, viewModel::onEvent, sectionModifier)
                OnboardingStep.PRACTICES -> PracticesSection(state, viewModel::onEvent, sectionModifier)
                OnboardingStep.REVIEW -> SummaryStep(state = state)
            }
            Spacer(Modifier.height(AkilimoSpacing.xxl))
        }
    }
}

private typealias OnEvent = (OnboardingViewModel.Event) -> Unit

/** Show the page-1 title once this much of the welcome banner has scrolled under the bar */
private const val BANNER_GONE_FRACTION = 0.75f

@Composable
private fun BioDataSection(
    state: OnboardingViewModel.UiState,
    onEvent: OnEvent,
    section: (OnboardingSection) -> Modifier,
) {
    BioDataStep(
        firstName = state.firstName,
        lastName = state.lastName,
        email = state.email,
        phone = state.phone,
        gender = state.gender,
        interest = state.interest,
        recLanguage = state.recommendationLanguage,
        errors = state.errors,
        onEvent = onEvent,
        modifier = section(OnboardingSection.BIO_DATA),
    )
}

@Composable
private fun FarmLocationSection(
    state: OnboardingViewModel.UiState,
    onEvent: OnEvent,
    navController: NavHostController,
    section: (OnboardingSection) -> Modifier,
) {
    CountryStep(
        country = state.country,
        error = state.errors["country"],
        onEvent = onEvent,
        modifier = section(OnboardingSection.COUNTRY),
    )
    LocationStep(
        latitude = state.latitude,
        longitude = state.longitude,
        altitude = state.altitude,
        zoomLevel = state.zoomLevel,
        country = state.country,
        coverage = state.locationCoverage,
        error = state.errors["location"],
        onEvent = onEvent,
        navController = navController,
        modifier = section(OnboardingSection.LOCATION),
    )
}

@Composable
private fun FieldSection(
    state: OnboardingViewModel.UiState,
    onEvent: OnEvent,
    section: (OnboardingSection) -> Modifier,
) {
    // A remembered unit only hides the unit picker, never the farm size
    AreaUnitStep(
        areaUnit = state.areaUnit,
        farmSize = state.farmSize,
        customFarmSize = state.customFarmSize,
        rememberAreaUnit = state.rememberAreaUnit,
        showUnitPicker = !state.areaUnitRemembered,
        country = state.country,
        errors = state.errors,
        onEvent = onEvent,
        modifier = section(OnboardingSection.AREA_UNIT),
    )
    PlantingDateStep(
        plantingDate = state.plantingDate,
        harvestDate = state.harvestDate,
        plantingFlex = state.plantingFlex,
        harvestFlex = state.harvestFlex,
        showFlexOptions = state.showFlexOptions,
        errors = state.errors,
        onEvent = onEvent,
        modifier = section(OnboardingSection.PLANTING_DATE),
    )
}

@Composable
private fun PracticesSection(
    state: OnboardingViewModel.UiState,
    onEvent: OnEvent,
    section: (OnboardingSection) -> Modifier,
) {
    TillageStep(
        tillageOperations = state.tillageOperations,
        weedControlEnabled = state.weedControlEnabled,
        weedControlMethod = state.weedControlMethod,
        errors = state.errors,
        onEvent = onEvent,
        modifier = section(OnboardingSection.TILLAGE),
    )
    InvestmentPrefStep(
        investmentPref = state.investmentPref,
        error = state.errors["investmentPref"],
        onEvent = onEvent,
        modifier = section(OnboardingSection.INVESTMENT_PREF),
    )
}
