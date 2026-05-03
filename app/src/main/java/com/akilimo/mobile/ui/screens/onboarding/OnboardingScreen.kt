import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.RecommendationsRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ExitConfirmDialog
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
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
import androidx.compose.material.icons.filled.ArrowBack
import com.akilimo.mobile.data.AppSettingsDataStore
import com.akilimo.mobile.ui.screens.onboarding.OnboardingSection
import kotlinx.coroutines.flow.collectLatest
import kotlin.system.exitProcess

@OptIn(ExperimentalMaterial3Api::class)
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

    val isEditMode = remember { !appSettings.isFirstRun }

    // Collect one-shot effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OnboardingViewModel.Effect.NavigateToRecommendations ->
                    if (isEditMode) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(RecommendationsRoute) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    }

                is OnboardingViewModel.Effect.ExitApp -> showExitDialog = true
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
            navController.popBackStack()
        } else {
            viewModel.onEvent(OnboardingViewModel.Event.BackClicked)
        }
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
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = if (isEditMode) {
                    stringResource(R.string.lbl_settings)
                } else {
                    stringResource(R.string.lbl_initial_onboarding)
                },
                onBack = {
                    if (isEditMode) {
                        navController.popBackStack()
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
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Country & Location
            CountryStep(
                country = state.country,
                error = state.errors["country"],
                onEvent = viewModel::onEvent,
            )

            LocationStep(
                latitude = state.latitude,
                longitude = state.longitude,
                altitude = state.altitude,
                zoomLevel = state.zoomLevel,
                onEvent = viewModel::onEvent,
                navController = navController,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Field details
            AnimatedVisibility(visible = state.visibleSections.contains(OnboardingSection.AREA_UNIT)) {
                AreaUnitStep(
                    areaUnit = state.areaUnit,
                    farmSize = state.farmSize,
                    customFarmSize = state.customFarmSize,
                    rememberAreaUnit = state.rememberAreaUnit,
                    country = state.country,
                    errors = state.errors,
                    onEvent = viewModel::onEvent,
                )
            }

            PlantingDateStep(
                plantingDate = state.plantingDate,
                harvestDate = state.harvestDate,
                plantingFlex = state.plantingFlex,
                harvestFlex = state.harvestFlex,
                showFlexOptions = state.showFlexOptions,
                errors = state.errors,
                onEvent = viewModel::onEvent,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Tillage & Investment
            TillageStep(
                tillageOperations = state.tillageOperations,
                weedControlEnabled = state.weedControlEnabled,
                weedControlMethod = state.weedControlMethod,
                errors = state.errors,
                onEvent = viewModel::onEvent,
            )

            InvestmentPrefStep(
                investmentPref = state.investmentPref,
                error = state.errors["investmentPref"],
                onEvent = viewModel::onEvent,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SummaryStep(state = state)

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun stepTitle(step: OnboardingSection): String = stringResource(
    when (step) {
        OnboardingSection.WELCOME -> R.string.welcome_title
        OnboardingSection.DISCLAIMER -> R.string.lbl_disclaimer
        OnboardingSection.TERMS -> R.string.lbl_terms
        OnboardingSection.BIO_DATA -> R.string.lbl_self_intro
        OnboardingSection.COUNTRY -> R.string.lbl_country
        OnboardingSection.LOCATION -> R.string.lbl_location
        OnboardingSection.AREA_UNIT -> R.string.lbl_field
        OnboardingSection.PLANTING_DATE -> R.string.lbl_planting_harvest_dates
        OnboardingSection.TILLAGE -> R.string.lbl_tillage_operation
        OnboardingSection.INVESTMENT_PREF -> R.string.lbl_investment_pref_prompt
        OnboardingSection.SUMMARY -> R.string.lbl_summary_title
    }
)
