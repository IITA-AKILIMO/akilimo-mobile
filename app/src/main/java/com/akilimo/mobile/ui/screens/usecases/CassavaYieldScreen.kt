package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.viewmodels.CassavaYieldViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CassavaYieldScreen(
    navController: NavHostController,
    viewModel: CassavaYieldViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isGridLayout by remember { mutableStateOf(true) }

    LaunchedEffect(state.seedRequest) {
        val areaUnit = state.seedRequest ?: return@LaunchedEffect
        viewModel.seedYields(buildCassavaYieldSeeds(context, areaUnit))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_typical_yield)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.lbl_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isGridLayout = !isGridLayout }) {
                        Icon(
                            painter = painterResource(
                                if (isGridLayout) R.drawable.ic_list else R.drawable.ic_grid
                            ),
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "completed_task",
                                AdviceCompletionDto(EnumAdviceTask.CURRENT_CASSAVA_YIELD, EnumStepStatus.COMPLETED)
                            )
                        navController.popBackStack()
                    },
                    enabled = state.selectedYieldId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.lbl_save))
                }
            }
        }
    ) { padding ->
        if (isGridLayout) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = padding,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(state.yields, key = { it.id }) { yield ->
                    YieldGridCard(
                        yield = yield,
                        isSelected = state.selectedYieldId == yield.id,
                        onClick = { viewModel.selectYield(yield) }
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(state.yields, key = { it.id }) { yield ->
                    YieldListCard(
                        yield = yield,
                        isSelected = state.selectedYieldId == yield.id,
                        onClick = { viewModel.selectYield(yield) }
                    )
                }
            }
        }
    }
}

@Composable
private fun YieldGridCard(
    yield: CassavaYield,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val imageRes = yield.imageRes.takeIf { it != 0 } ?: yieldImageByOrder(yield.sortOrder)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (imageRes != 0) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = yield.yieldLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(12.dp)
                )
            }
            Text(
                text = yield.yieldLabel,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            if (yield.amountLabel != null) {
                Text(
                    text = yield.amountLabel!!,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun YieldListCard(
    yield: CassavaYield,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val imageRes = yield.imageRes.takeIf { it != 0 } ?: yieldImageByOrder(yield.sortOrder)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            if (imageRes != 0) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = yield.yieldLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(88.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = yield.yieldLabel,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (yield.amountLabel != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = yield.amountLabel!!,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (yield.description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = yield.description!!,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun yieldImageByOrder(sortOrder: Int): Int = when (sortOrder) {
    1 -> R.drawable.yield_less_than_7point5
    2 -> R.drawable.yield_7point5_to_15
    3 -> R.drawable.yield_15_to_22point5
    4 -> R.drawable.yield_22_to_30
    5 -> R.drawable.yield_more_than_30
    else -> 0
}

private fun buildCassavaYieldSeeds(
    context: android.content.Context,
    areaUnit: EnumAreaUnit
): List<CassavaYield> {
    data class YieldDef(
        val imageRes: Int,
        val labelRes: Int,
        val yieldAmount: Double,
        val descRes: Int,
        val sortOrder: Int
    )

    val amountLabelRes: IntArray = when (areaUnit) {
        EnumAreaUnit.ACRE -> intArrayOf(
            R.string.yield_less_than_3_tonnes_per_acre, R.string.yield_3_to_6_tonnes_per_acre,
            R.string.yield_6_to_9_tonnes_per_acre, R.string.yield_9_to_12_tonnes_per_acre,
            R.string.yield_more_than_12_tonnes_per_acre
        )
        EnumAreaUnit.HA -> intArrayOf(
            R.string.yield_less_than_3_tonnes_per_hectare, R.string.yield_3_to_6_tonnes_per_hectare,
            R.string.yield_6_to_9_tonnes_per_hectare, R.string.yield_9_to_12_tonnes_per_hectare,
            R.string.yield_more_than_12_tonnes_per_hectare
        )
        EnumAreaUnit.ARE -> intArrayOf(
            R.string.yield_less_than_3_tonnes_per_are, R.string.yield_3_to_6_tonnes_per_are,
            R.string.yield_6_to_9_tonnes_per_are, R.string.yield_9_to_12_tonnes_per_are,
            R.string.yield_more_than_12_tonnes_per_are
        )
        EnumAreaUnit.M2 -> intArrayOf(
            R.string.yield_less_than_3_tonnes_per_meter, R.string.yield_3_to_6_tonnes_per_meter,
            R.string.yield_6_to_9_tonnes_per_meter, R.string.yield_9_to_12_tonnes_per_meter,
            R.string.yield_more_than_12_tonnes_per_meter
        )
    }

    val defs = listOf(
        YieldDef(R.drawable.yield_less_than_7point5, R.string.fcy_lower, 3.75, R.string.lbl_low_yield, 1),
        YieldDef(R.drawable.yield_7point5_to_15, R.string.fcy_about_the_same, 11.25, R.string.lbl_normal_yield, 2),
        YieldDef(R.drawable.yield_15_to_22point5, R.string.fcy_somewhat_higher, 18.75, R.string.lbl_high_yield, 3),
        YieldDef(R.drawable.yield_22_to_30, R.string.fcy_2_3_times_higher, 26.25, R.string.lbl_very_high_yield, 4),
        YieldDef(R.drawable.yield_more_than_30, R.string.fcy_more_than_3_times_higher, 33.75, R.string.lbl_very_high_yield, 5)
    )

    return defs.mapIndexed { index, def ->
        val desc = context.getString(def.descRes)
        val amountLabel = context.getString(amountLabelRes[index])
        CassavaYield.create(
            yieldAmount = def.yieldAmount,
            yieldLabel = context.getString(def.labelRes),
            imageRes = def.imageRes,
            description = desc
        ).copy(sortOrder = def.sortOrder).also {
            it.description = desc
            it.amountLabel = amountLabel
        }
    }
}
