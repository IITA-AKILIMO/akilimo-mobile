package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import com.akilimo.mobile.dto.MaizePerfOption
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.components.compose.SelectionCard
import com.akilimo.mobile.ui.viewmodels.MaizePerformanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaizePerformanceScreen(
    navController: NavHostController,
    viewModel: MaizePerformanceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isGridLayout by remember { mutableStateOf(true) }
    var selectedOption by remember { mutableStateOf<MaizePerfOption?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_maize_performance)) },
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
                        val option = selectedOption ?: return@Button
                        viewModel.saveSelection(option.valueOption)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "completed_task",
                                AdviceCompletionDto(EnumAdviceTask.MAIZE_PERFORMANCE, EnumStepStatus.COMPLETED)
                            )
                        navController.popBackStack()
                    },
                    enabled = selectedOption != null,
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
                items(state.options) { option ->
                    if (option.isSelected && selectedOption == null) selectedOption = option
                    SelectionCard(
                        imageRes = option.valueOption.imageRes,
                        title = context.getString(option.valueOption.label),
                        subtitle = option.valueOption.performanceDesc
                            ?.let { context.getString(it) },
                        isSelected = selectedOption?.valueOption == option.valueOption,
                        isGridLayout = true,
                        onClick = { selectedOption = option }
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
                items(state.options) { option ->
                    if (option.isSelected && selectedOption == null) selectedOption = option
                    SelectionCard(
                        imageRes = option.valueOption.imageRes,
                        title = context.getString(option.valueOption.label),
                        subtitle = option.valueOption.performanceDesc
                            ?.let { context.getString(it) },
                        isSelected = selectedOption?.valueOption == option.valueOption,
                        isGridLayout = false,
                        onClick = { selectedOption = option }
                    )
                }
            }
        }
    }
}
