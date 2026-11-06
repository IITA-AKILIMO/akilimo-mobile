package com.akilimo.mobile.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.navigation.RecommendationsRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ExitConfirmDialog
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
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
    val scrollState = rememberScrollState()
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

                is OnboardingViewModel.Effect.ShowSnackbar ->
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }

                is OnboardingViewModel.Effect.ShowValidationErrors -> {
                    // The first failing field may be far off-screen: bring its section into view
                    scope.launch {
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

    BackHandler {
        if (isEditMode) {
            navController.popBackStackSafely()
        } else {
            viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
        }
    }

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

    fun Modifier.section(section: OnboardingSection): Modifier =
        this.bringIntoViewRequester(sectionRequesters.getValue(section))

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = if (isEditMode) {
                    stringResource(R.string.lbl_edit_profile)
                } else {
                    stringResource(R.string.lbl_initial_onboarding)
                },
                onBack = {
                    if (isEditMode) {
                        navController.popBackStackSafely()
                    } else {
                        viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
                    }
                }
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = if (isEditMode) {
                    stringResource(R.string.lbl_save)
                } else {
                    stringResource(R.string.lbl_complete_onboarding)
                },
                onClick = { viewModel.onEvent(OnboardingViewModel.Event.SubmitClicked) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
        ) {
            // Welcome Section
            WelcomeStep()

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Bio Data Section
            BioDataStep(
                firstName = state.firstName,
                lastName = state.lastName,
                email = state.email,
                phone = state.phone,
                gender = state.gender,
                interest = state.interest,
                recLanguage = state.recommendationLanguage,
                errors = state.errors,
                onEvent = viewModel::onEvent,
                modifier = Modifier.section(OnboardingSection.BIO_DATA),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Country & Location
            CountryStep(
                country = state.country,
                error = state.errors["country"],
                onEvent = viewModel::onEvent,
                modifier = Modifier.section(OnboardingSection.COUNTRY),
            )

            LocationStep(
                latitude = state.latitude,
                longitude = state.longitude,
                altitude = state.altitude,
                zoomLevel = state.zoomLevel,
                country = state.country,
                coverage = state.locationCoverage,
                error = state.errors["location"],
                onEvent = viewModel::onEvent,
                navController = navController,
                modifier = Modifier.section(OnboardingSection.LOCATION),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Field details: always shown; a remembered unit only hides the unit picker
            AreaUnitStep(
                areaUnit = state.areaUnit,
                farmSize = state.farmSize,
                customFarmSize = state.customFarmSize,
                rememberAreaUnit = state.rememberAreaUnit,
                showUnitPicker = !state.areaUnitRemembered,
                country = state.country,
                errors = state.errors,
                onEvent = viewModel::onEvent,
                modifier = Modifier.section(OnboardingSection.AREA_UNIT),
            )

            PlantingDateStep(
                plantingDate = state.plantingDate,
                harvestDate = state.harvestDate,
                plantingFlex = state.plantingFlex,
                harvestFlex = state.harvestFlex,
                showFlexOptions = state.showFlexOptions,
                errors = state.errors,
                onEvent = viewModel::onEvent,
                modifier = Modifier.section(OnboardingSection.PLANTING_DATE),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Tillage & Investment
            TillageStep(
                tillageOperations = state.tillageOperations,
                weedControlEnabled = state.weedControlEnabled,
                weedControlMethod = state.weedControlMethod,
                errors = state.errors,
                onEvent = viewModel::onEvent,
                modifier = Modifier.section(OnboardingSection.TILLAGE),
            )

            InvestmentPrefStep(
                investmentPref = state.investmentPref,
                error = state.errors["investmentPref"],
                onEvent = viewModel::onEvent,
                modifier = Modifier.section(OnboardingSection.INVESTMENT_PREF),
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SummaryStep(state = state)

            Spacer(Modifier.height(32.dp))
        }
    }
}
