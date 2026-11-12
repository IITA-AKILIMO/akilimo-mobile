package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.StayCurrentLandscape
import androidx.compose.material.icons.filled.StayCurrentPortrait
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

private const val ORIENTATION_RELEASE_DELAY_MS = 500L
private const val INACTIVE_ACTION_ALPHA = 0.7f
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
) {
    // Title always sits above the text, in every orientation; the width cap keeps lines readable
    Box(modifier = Modifier.fillMaxSize()) {
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
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = TITLE_FONT_SP.sp,
                    lineHeight = TITLE_LINE_HEIGHT_SP.sp,
                ),
            )
            Spacer(Modifier.height(AkilimoSpacing.md))
            RecommendationCard(paragraphs = paragraphs)
        }
    }
}

@Composable
private fun RecommendationCard(paragraphs: List<String>) {
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
                        fontSize = BODY_FONT_SP.sp,
                        lineHeight = BODY_LINE_HEIGHT_SP.sp,
                    ),
                )
            }
        }
    }
}

/** Top-bar actions: text size menu and the landscape toggle, kept out of the reading area. */
@Composable
private fun RowScope.ReadingActions(
    textScale: Float,
    onTextScaleChange: (Float) -> Unit,
    landscapeLocked: Boolean,
    onLandscapeToggle: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val tint = MaterialTheme.colorScheme.onPrimary

    IconButton(onClick = { menuOpen = true }) {
        Icon(
            imageVector = Icons.Default.FormatSize,
            contentDescription = stringResource(R.string.lbl_text_size),
            tint = tint,
        )
    }
    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        ReadingTextScale.options.forEach { option ->
            DropdownMenuItem(
                text = { Text(stringResource(textScaleLabelRes(option))) },
                leadingIcon = {
                    if (option == textScale) Icon(Icons.Default.Check, contentDescription = null)
                },
                onClick = {
                    onTextScaleChange(option)
                    menuOpen = false
                },
            )
        }
    }

    IconToggleButton(checked = landscapeLocked, onCheckedChange = { onLandscapeToggle() }) {
        // The icon shows the orientation a tap switches to
        Icon(
            imageVector = if (landscapeLocked) {
                Icons.Default.StayCurrentPortrait
            } else {
                Icons.Default.StayCurrentLandscape
            },
            contentDescription = stringResource(
                if (landscapeLocked) R.string.lbl_portrait_view else R.string.lbl_landscape_view
            ),
            tint = tint.copy(alpha = if (landscapeLocked) 1f else INACTIVE_ACTION_ALPHA),
        )
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
    val landscapeLocked by viewModel.landscapeLocked.collectAsStateWithLifecycle()

    // The reader can force landscape for this screen only; always hand control back on leaving.
    val activity = LocalContext.current.findActivity()
    // Switching off must actively return to portrait: with auto-rotate off, "unspecified" would
    // keep the phone stuck in landscape.
    var wasLocked by remember { mutableStateOf(false) }
    DisposableEffect(landscapeLocked) {
        if (landscapeLocked) wasLocked = true
        activity?.requestedOrientation = when {
            landscapeLocked -> ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
            wasLocked -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {}
    }
    // In landscape the status and navigation bars are hidden too (swipe from the edge shows them).
    val view = LocalView.current
    DisposableEffect(landscapeLocked) {
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        if (landscapeLocked) {
            controller?.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller?.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose { controller?.show(WindowInsetsCompat.Type.systemBars()) }
    }
    // Leaving the screen: go back to portrait, then hand rotation back to the system.
    DisposableEffect(Unit) {
        onDispose {
            // Only undo what this screen forced; a reader who never locked keeps natural rotation
            if (activity != null && !activity.isChangingConfigurations &&
                activity.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            ) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                activity.window.decorView.postDelayed(
                    { activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED },
                    ORIENTATION_RELEASE_DELAY_MS,
                )
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

    // The app bar slides away while reading down and returns on the slightest scroll up,
    // giving the advice the whole screen.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                onBack = { navController.popBackStackSafely() },
                scrollBehavior = scrollBehavior,
                actions = {
                    if (state is GetRecommendationViewModel.UiState.Success) {
                        ReadingActions(
                            textScale = textScale,
                            onTextScaleChange = viewModel::setTextScale,
                            landscapeLocked = landscapeLocked,
                            onLandscapeToggle = { viewModel.setLandscapeLocked(!landscapeLocked) },
                        )
                    }
                },
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
