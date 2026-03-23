package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.AdviceCompletionDto
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus
import com.akilimo.mobile.ui.viewmodels.InvestmentAmountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentAmountScreen(
    navController: NavHostController,
    viewModel: InvestmentAmountViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedItem by remember { mutableStateOf<InvestmentAmount?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lbl_investment_amount)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.lbl_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = {
                        val item = selectedItem ?: return@Button
                        viewModel.saveInvestment(item, item.investmentAmount)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                "completed_task",
                                AdviceCompletionDto(EnumAdviceTask.INVESTMENT_AMOUNT, EnumStepStatus.COMPLETED)
                            )
                        navController.popBackStack()
                    },
                    enabled = selectedItem != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.lbl_save))
                }
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(state.investments, key = { it.id }) { item ->
                val isSelected = selectedItem?.id == item.id ||
                        (selectedItem == null && state.selectedInvestment?.investmentId == item.id)
                if (isSelected && selectedItem == null) {
                    selectedItem = item
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { selectedItem = item }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        RadioButton(
                            selected = selectedItem?.id == item.id,
                            onClick = { selectedItem = item }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = "${item.currencySymbol} ${String.format("%.0f", item.investmentAmount)}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = item.areaUnit,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
