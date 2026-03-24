package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumUseCase
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.viewmodels.GetRecommendationViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetRecommendationScreen(useCase: EnumUseCase, navController: NavHostController) {
    val viewModel = hiltViewModel<GetRecommendationViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val noRecsLabel = stringResource(R.string.lbl_no_recommendations_prompt)
    val errorLabel = stringResource(R.string.error_fetch_recommendation)
    var showFeedback by remember { mutableStateOf(false) }

    LaunchedEffect(useCase) {
        viewModel.fetchRecommendation(useCase, noRecsLabel, errorLabel)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.feedbackResult.collect { success ->
            snackbarHostState.showSnackbar(
                if (success) "Thank you for your feedback!" else "Failed to submit feedback"
            )
        }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            if (state is GetRecommendationViewModel.UiState.Success) {
                FloatingActionButton(onClick = { showFeedback = true }) {
                    Icon(Icons.Default.StarRate, contentDescription = "Feedback")
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
                            .padding(AkilimoSpacing.md)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(text = title, style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(AkilimoSpacing.sm))
                        Text(text = s.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                is GetRecommendationViewModel.UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(s.message, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(AkilimoSpacing.md))
                        Button(
                            onClick = {
                                viewModel.fetchRecommendation(useCase, noRecsLabel, errorLabel)
                            }
                        ) {
                            Text("Retry")
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
