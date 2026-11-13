package com.akilimo.mobile.ui.screens.account

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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

/** Start loading the next screenful this many rows before the end of the list. */
private const val LOAD_MORE_AHEAD = 8

/**
 * The advice a farmer saved: read from the phone a screenful at a time (so a history of thousands stays quick and opens
 * offline), narrowed by use case and country, and refreshed from the account.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAdviceScreen(
    navController: NavHostController,
    viewModel: SavedAdviceViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val count by viewModel.count.collectAsStateWithLifecycle()
    val useCases by viewModel.useCases.collectAsStateWithLifecycle()
    val countries by viewModel.countries.collectAsStateWithLifecycle()
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val filtered = state.filter.useCase != null || state.filter.country != null

    // Reaching the end shows the next screenful
    LaunchedEffect(listState, items?.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { last -> if (last >= (items?.size ?: 0) - LOAD_MORE_AHEAD) viewModel.loadMore() }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.saved_advice_title),
                onBack = { navController.popBackStackSafely() },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !state.refreshing) {
                        Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.lbl_retry))
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.saved_advice_more_options))
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.saved_advice_delete_all), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuOpen = false
                                    confirmDeleteAll = true
                                },
                                enabled = !state.deleting,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.refreshing || state.deleting) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            state.messageRes?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
                )
            }
            if (useCases.isNotEmpty() || countries.size > 1) {
                FilterRow(
                    useCases = useCases,
                    countries = countries,
                    selectedUseCase = state.filter.useCase,
                    selectedCountry = state.filter.country,
                    onUseCase = viewModel::setUseCase,
                    onCountry = viewModel::setCountry,
                )
            }
            if (count > 0) {
                Text(
                    text = pluralStringResource(R.plurals.saved_advice_count, count, count),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xxs),
                )
            }
            val shown = items
            when {
                shown == null -> SkeletonList(modifier = Modifier.padding(AkilimoSpacing.md))
                shown.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    StateMessage(
                        icon = Icons.Outlined.Lightbulb,
                        title = stringResource(if (filtered) R.string.saved_advice_no_match_title else R.string.saved_advice_empty_title),
                        message = stringResource(if (filtered) R.string.saved_advice_no_match_msg else R.string.saved_advice_empty_msg),
                    )
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AkilimoSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
                ) {
                    items(shown, key = { it.requestId }) { advice ->
                        SavedAdviceRow(advice) { navController.navigateIfResumed(SavedAdviceDetailRoute(advice.requestId)) }
                    }
                }
            }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.saved_advice_delete_all_title)) },
            text = { Text(stringResource(R.string.saved_advice_delete_all_body)) },
            confirmButton = {
                TextButton(onClick = { confirmDeleteAll = false; viewModel.deleteAll() }) {
                    Text(stringResource(R.string.saved_advice_delete_all_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text(stringResource(R.string.lbl_cancel)) } },
        )
    }
}

/** Chips to narrow the list: the use cases the farmer has, then (with more than one) the countries. */
@Composable
private fun FilterRow(
    useCases: List<String>,
    countries: List<String>,
    selectedUseCase: String?,
    selectedCountry: String?,
    onUseCase: (String?) -> Unit,
    onCountry: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs)) {
        if (useCases.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AkilimoSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
            ) {
                item {
                    FilterChip(
                        selected = selectedUseCase == null,
                        onClick = { onUseCase(null) },
                        label = { Text(stringResource(R.string.saved_advice_filter_all)) },
                    )
                }
                items(useCases) { useCase ->
                    FilterChip(
                        selected = selectedUseCase == useCase,
                        onClick = { onUseCase(useCase) },
                        label = { Text(adviceKindLabel(useCase)) },
                    )
                }
            }
        }
        if (countries.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = AkilimoSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs),
            ) {
                item {
                    FilterChip(
                        selected = selectedCountry == null,
                        onClick = { onCountry(null) },
                        label = { Text(stringResource(R.string.saved_advice_filter_all)) },
                    )
                }
                items(countries) { country ->
                    FilterChip(
                        selected = selectedCountry == country,
                        onClick = { onCountry(country) },
                        label = { Text(country) },
                    )
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
