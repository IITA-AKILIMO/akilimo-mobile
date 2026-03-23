package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumAreaUnit
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
import com.akilimo.mobile.ui.components.compose.SelectionCard
import com.akilimo.mobile.ui.components.compose.completeTask
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
            BackTopAppBar(
                title = stringResource(R.string.lbl_typical_yield),
                onBack = { navController.popBackStack() },
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
            SaveBottomBar(
                label = stringResource(R.string.lbl_save),
                enabled = state.selectedYieldId != null,
                onClick = { navController.completeTask(EnumAdviceTask.CURRENT_CASSAVA_YIELD) }
            )
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
                    val imageRes = yield.imageRes.takeIf { it != 0 }
                        ?: yieldImageByOrder(yield.sortOrder)
                    SelectionCard(
                        imageRes = imageRes,
                        title = yield.yieldLabel,
                        subtitle = yield.amountLabel,
                        description = yield.description,
                        isSelected = state.selectedYieldId == yield.id,
                        isGridLayout = true,
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
                    val imageRes = yield.imageRes.takeIf { it != 0 }
                        ?: yieldImageByOrder(yield.sortOrder)
                    SelectionCard(
                        imageRes = imageRes,
                        title = yield.yieldLabel,
                        subtitle = yield.amountLabel,
                        description = yield.description,
                        isSelected = state.selectedYieldId == yield.id,
                        isGridLayout = false,
                        onClick = { viewModel.selectYield(yield) }
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
