package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.GetRecommendationViewModel

/** FAB height (56dp) + its margin (16dp) + breathing room, so the FAB never covers text. */
private val FAB_CLEARANCE = 88.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetRecommendationScreen(useCase: EnumUseCase, navController: NavHostController) {
    val viewModel = hiltViewModel<GetRecommendationViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feedbackSuccessMessage = stringResource(R.string.msg_feedback_submit_success)
    val feedbackFailureMessage = stringResource(R.string.msg_feedback_submit_failure)
    var showFeedback by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(useCase) {
        viewModel.fetchRecommendation(useCase)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.feedbackResult.collect { success ->
            snackbarHostState.showSnackbar(
                if (success) feedbackSuccessMessage else feedbackFailureMessage
            )
        }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                onBack = { navController.popBackStackSafely() }
            )
        },
        floatingActionButton = {
            if (state is GetRecommendationViewModel.UiState.Success) {
                FloatingActionButton(onClick = { showFeedback = true }) {
                    Icon(Icons.Default.StarRate, contentDescription = stringResource(R.string.lbl_feedback))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is GetRecommendationViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is GetRecommendationViewModel.UiState.Success -> {
                    val title = when (s.title) {
                        "FR" -> stringResource(R.string.lbl_fertilizer_rec)
                        "IC" -> stringResource(R.string.lbl_intercrop_rec)
                        "PP" -> stringResource(R.string.lbl_planting_practices_rec)
                        "SP" -> stringResource(R.string.lbl_scheduled_planting_rec)
                        else -> s.title
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = AkilimoSpacing.md,
                                end = AkilimoSpacing.md,
                                top = AkilimoSpacing.md,
                                bottom = FAB_CLEARANCE,
                            )
                    ) {
                        Text(text = title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(AkilimoSpacing.sm))
                        Text(text = s.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is GetRecommendationViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = AkilimoSpacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(s.messageRes),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(AkilimoSpacing.md))
                        Button(onClick = { viewModel.fetchRecommendation(useCase) }) {
                            Text(stringResource(R.string.lbl_retry))
                        }
                    }
                }

                else -> Unit // Idle
            }
        }
    }

    if (showFeedback) {
        FeedbackBottomSheet(
            onDismiss = { showFeedback = false },
            onSubmit = { rating, nps ->
                showFeedback = false
                viewModel.submitFeedback(useCase, rating, nps)
            }
        )
    }
}
