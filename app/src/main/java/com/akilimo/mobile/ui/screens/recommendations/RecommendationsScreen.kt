package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.dto.AdviceOption
import com.akilimo.mobile.enums.EnumAdvice
import com.akilimo.mobile.navigation.BppRoute
import com.akilimo.mobile.navigation.FrRoute
import com.akilimo.mobile.navigation.IcMaizeRoute
import com.akilimo.mobile.navigation.IcSweetPotatoRoute
import com.akilimo.mobile.navigation.SphRoute
import com.akilimo.mobile.ui.viewmodels.RecommendationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<RecommendationsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.lbl_recommendations)) })
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(state.adviceOptions) { option ->
                AdviceOptionCard(
                    option = option,
                    onClick = {
                        viewModel.trackActiveAdvice(option.valueOption)
                        val route: Any = when (option.valueOption) {
                            EnumAdvice.FERTILIZER_RECOMMENDATIONS -> FrRoute
                            EnumAdvice.BEST_PLANTING_PRACTICES -> BppRoute
                            EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> SphRoute
                            EnumAdvice.INTERCROPPING_MAIZE -> IcMaizeRoute
                            EnumAdvice.INTERCROPPING_SWEET_POTATO -> IcSweetPotatoRoute
                        }
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}

@Composable
private fun AdviceOptionCard(option: AdviceOption, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = option.valueOption.label(context),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
