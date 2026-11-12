package com.akilimo.mobile.ui.screens.account

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.SavedAdvice
import com.akilimo.mobile.navigation.SavedAdviceDetailRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.IconTile
import com.akilimo.mobile.ui.components.compose.SkeletonList
import com.akilimo.mobile.ui.components.compose.StateMessage
import com.akilimo.mobile.ui.components.compose.cardSurface
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.screens.recommendations.navigateIfResumed
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.SavedAdviceViewModel

/** The advice a farmer saved: listed from the phone's copy (so it opens offline) and refreshed from the account. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAdviceScreen(
    navController: NavHostController,
    viewModel: SavedAdviceViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.saved_advice_title),
                onBack = { navController.popBackStackSafely() },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.refreshing) {
                        Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.lbl_retry))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.refreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            state.messageRes?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
                )
            }
            when {
                items == null -> SkeletonList(modifier = Modifier.padding(AkilimoSpacing.md))
                items!!.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    StateMessage(
                        icon = Icons.Outlined.Lightbulb,
                        title = stringResource(R.string.saved_advice_empty_title),
                        message = stringResource(R.string.saved_advice_empty_msg),
                    )
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(AkilimoSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
                ) {
                    items(items!!, key = { it.requestId }) { advice ->
                        SavedAdviceRow(advice) { navController.navigateIfResumed(SavedAdviceDetailRoute(advice.requestId)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedAdviceRow(advice: SavedAdvice, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface()
            .clickable(onClick = onClick)
            .padding(AkilimoSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
    ) {
        IconTile(Icons.Outlined.Lightbulb)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.saved_advice_item_title, adviceKindLabel(advice.recType ?: advice.useCase)),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = savedWhen(advice.createdAt) + advice.countryCode?.let { " · $it" }.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            advice.recommendation?.let {
                Text(
                    text = it.lineSequence().firstOrNull { line -> line.isNotBlank() }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** "Fertilizer", "Intercropping", ... as the recommendation screens call them. */
@Composable
fun adviceKindLabel(kind: String?): String = when (kind) {
    "FR" -> stringResource(R.string.lbl_fertilizer_rec)
    "IC" -> stringResource(R.string.lbl_intercrop_rec)
    "PP" -> stringResource(R.string.lbl_planting_practices_rec)
    "SP" -> stringResource(R.string.lbl_scheduled_planting_rec)
    else -> kind ?: stringResource(R.string.lbl_recommendations)
}

@Composable
internal fun savedWhen(createdAt: Long): String =
    DateUtils.getRelativeTimeSpanString(createdAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
