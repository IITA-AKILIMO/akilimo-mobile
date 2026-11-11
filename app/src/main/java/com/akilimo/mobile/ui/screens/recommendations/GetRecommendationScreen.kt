package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.akilimo.mobile.data.ReadingTextScale
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

/** Longest comfortable line (about 60-70 characters at the body size) for landscape and tablets. */
private val READING_MAX_WIDTH = 600.dp

/** From this width (tablets, large landscape) the title and controls sit beside the text. */
private val TWO_PANE_MIN_WIDTH = 840.dp

private const val BODY_FONT_SP = 18f
private const val BODY_LINE_HEIGHT_SP = 28f
private const val TITLE_FONT_SP = 24f
private const val TITLE_LINE_HEIGHT_SP = 32f

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun textScaleLabelRes(scale: Float): Int = when {
    scale >= 1.5f -> R.string.lbl_text_size_largest
    scale >= 1.25f -> R.string.lbl_text_size_large
    else -> R.string.lbl_text_size_normal
}

@Composable
private fun RecommendationBody(
    title: String,
    paragraphs: List<String>,
    textScale: Float,
    onTextScaleChange: (Float) -> Unit,
    landscapeLocked: Boolean,
    onLandscapeToggle: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val twoPane = maxWidth >= TWO_PANE_MIN_WIDTH
        val header: @Composable () -> Unit = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = (TITLE_FONT_SP * textScale).sp,
                    lineHeight = (TITLE_LINE_HEIGHT_SP * textScale).sp,
                ),
            )
            Spacer(Modifier.height(AkilimoSpacing.md))
            TextSizeSelector(selected = textScale, onSelect = onTextScaleChange)
            Spacer(Modifier.height(AkilimoSpacing.sm))
            FilterChip(
                selected = landscapeLocked,
                onClick = onLandscapeToggle,
                label = { Text(stringResource(R.string.lbl_landscape_view)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                },
            )
        }
        val content: @Composable () -> Unit = {
            RecommendationCard(paragraphs = paragraphs, textScale = textScale)
        }

        if (twoPane) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AkilimoSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xl),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) { header() }
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = FAB_CLEARANCE),
                ) { content() }
            }
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = READING_MAX_WIDTH)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = AkilimoSpacing.md,
                        end = AkilimoSpacing.md,
                        top = AkilimoSpacing.md,
                        bottom = FAB_CLEARANCE,
                    ),
            ) {
                header()
                Spacer(Modifier.height(AkilimoSpacing.md))
                content()
            }
        }
    }
}

@Composable
private fun RecommendationCard(paragraphs: List<String>, textScale: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.md),
        ) {
            paragraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = (BODY_FONT_SP * textScale).sp,
                        lineHeight = (BODY_LINE_HEIGHT_SP * textScale).sp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TextSizeSelector(selected: Float, onSelect: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
        Text(
            text = stringResource(R.string.lbl_text_size),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm)) {
            ReadingTextScale.options.forEachIndexed { index, option ->
                val description = stringResource(textScaleLabelRes(option))
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = {
                        Text(
                            text = "A",
                            fontSize = (14 + index * 4).sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    modifier = Modifier.semantics { contentDescription = description },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetRecommendationScreen(useCase: EnumUseCase, navController: NavHostController) {
    val viewModel = hiltViewModel<GetRecommendationViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val textScale by viewModel.textScale.collectAsStateWithLifecycle()
    val feedbackSuccessMessage = stringResource(R.string.msg_feedback_submit_success)
    val feedbackFailureMessage = stringResource(R.string.msg_feedback_submit_failure)
    var showFeedback by rememberSaveable { mutableStateOf(false) }
    var landscapeLocked by rememberSaveable { mutableStateOf(false) }

    // The reader can force landscape for this screen only; always hand control back on leaving.
    val activity = LocalContext.current.findActivity()
    DisposableEffect(landscapeLocked) {
        activity?.requestedOrientation = if (landscapeLocked) {
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {}
    }
    DisposableEffect(Unit) {
        onDispose {
            if (activity?.isChangingConfigurations == false) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

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
                    RecommendationBody(
                        title = title,
                        paragraphs = remember(s.description) { splitRecommendationParagraphs(s.description) },
                        textScale = textScale,
                        onTextScaleChange = viewModel::setTextScale,
                        landscapeLocked = landscapeLocked,
                        onLandscapeToggle = { landscapeLocked = !landscapeLocked },
                    )
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
