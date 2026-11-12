package com.akilimo.mobile.ui.screens.hub

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.FarmRoute
import com.akilimo.mobile.navigation.ProfileRoute
import com.akilimo.mobile.navigation.RecommendationsRoute
import com.akilimo.mobile.navigation.SettingsRoute
import com.akilimo.mobile.ui.components.compose.ExitConfirmDialog
import com.akilimo.mobile.ui.components.compose.IconTile
import com.akilimo.mobile.ui.components.compose.SyncStatusStrip
import com.akilimo.mobile.ui.screens.recommendations.navigateIfResumed
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.HubViewModel
import com.akilimo.mobile.workers.SyncPhase

/** How far the Recommendations card overlaps the bottom edge of the green header. */
private val CARD_OVERLAP = 40.dp

/**
 * The app home after onboarding: one prominent card for Recommendations, and cards for the
 * farmer's Profile, their farm and the app Settings.
 */
@Composable
fun HubScreen(
    navController: NavHostController,
    viewModel: HubViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // This is the home screen (onboarding is cleared from the back stack), so Back would close
    // the app: ask first.
    BackHandler { showExitDialog = true }
    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirm = {
                showExitDialog = false
                // Finish normally rather than killing the process, so background syncs complete
                (context as? Activity)?.finish()
            },
            onDismiss = { showExitDialog = false },
        )
    }

    Scaffold(
        bottomBar = {
            state.displayStatus?.let { status ->
                SyncStatusStrip(
                    status = status,
                    onRetry = if (status.phase == SyncPhase.FAILED) viewModel::retrySyncs else null,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
                .verticalScroll(rememberScrollState()),
        ) {
            HubHeader()
            Column(
                modifier = Modifier
                    // Lift the cards over the header's bottom edge without leaving a gap below them
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val overlap = CARD_OVERLAP.roundToPx()
                        layout(placeable.width, placeable.height - overlap) { placeable.place(0, -overlap) }
                    }
                    .padding(horizontal = AkilimoSpacing.md)
                    .padding(bottom = AkilimoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
            ) {
                RecommendationsCard(onClick = { navController.navigateIfResumed(RecommendationsRoute) })
                HubLinkCard(
                    icon = Icons.Outlined.AccountCircle,
                    title = stringResource(R.string.lbl_profile),
                    subtitle = stringResource(R.string.msg_hub_profile_desc),
                    onClick = { navController.navigateIfResumed(ProfileRoute) },
                )
                HubLinkCard(
                    icon = Icons.Outlined.Place,
                    title = stringResource(R.string.lbl_your_farm),
                    subtitle = stringResource(R.string.msg_hub_farm_desc),
                    onClick = { navController.navigateIfResumed(FarmRoute) },
                )
                HubLinkCard(
                    icon = Icons.Outlined.Settings,
                    title = stringResource(R.string.lbl_settings),
                    subtitle = stringResource(R.string.msg_hub_settings_desc),
                    onClick = { navController.navigateIfResumed(SettingsRoute) },
                )
            }
        }
    }
}

@Composable
private fun HubHeader() {
    Column(
        // The logo is centred in the header
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
            .padding(horizontal = AkilimoSpacing.md)
            // Room under the logo for the Recommendations card to overlap without touching it
            .padding(top = AkilimoSpacing.lg, bottom = AkilimoSpacing.xl + CARD_OVERLAP),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_akilimo_logo_white),
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier.size(width = 180.dp, height = 121.dp),
        )
    }
}

@Composable
private fun RecommendationsCard(onClick: () -> Unit) {
    // A deeper green than the header in the light theme; the dark theme's own container colour
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val container = if (dark) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        lerp(MaterialTheme.colorScheme.primary, Color.Black, 0.25f)
    }
    val content = if (dark) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(Icons.Outlined.Lightbulb)
            Spacer(Modifier.width(AkilimoSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.lbl_recommendations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.msg_hub_recommendations_desc),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
private fun HubLinkCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon)
            Spacer(Modifier.width(AkilimoSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
