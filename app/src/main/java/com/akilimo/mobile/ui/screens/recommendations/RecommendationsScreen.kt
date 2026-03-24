package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.RecommendationsViewModel

private fun EnumAdvice.icon(): ImageVector = when (this) {
    EnumAdvice.FERTILIZER_RECOMMENDATIONS -> Icons.Outlined.Science
    EnumAdvice.BEST_PLANTING_PRACTICES -> Icons.Outlined.Eco
    EnumAdvice.INTERCROPPING_MAIZE -> Icons.Outlined.Grass
    EnumAdvice.INTERCROPPING_SWEET_POTATO -> Icons.Outlined.Spa
    EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> Icons.Outlined.CalendarMonth
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<RecommendationsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val heroPx = with(LocalDensity.current) { 240.dp.toPx() }

    val scrollOffsetPx by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex == 0)
                listState.firstVisibleItemScrollOffset.toFloat()
            else heroPx
        }
    }
    val appBarAlpha by remember {
        derivedStateOf { (scrollOffsetPx / heroPx).coerceIn(0f, 1f) }
    }
    val heroContentAlpha by remember {
        derivedStateOf { (1f - scrollOffsetPx / heroPx * 1.5f).coerceIn(0f, 1f) }
    }
    val parallaxOffsetPx by remember {
        derivedStateOf { scrollOffsetPx * 0.35f }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            item(key = "hero") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(MaterialTheme.colorScheme.primary)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.15f),
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.graphicsLayer {
                            translationY = -parallaxOffsetPx
                            alpha = heroContentAlpha
                        },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_akilimo_logo_white),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                            modifier = Modifier.size(120.dp),
                        )
                        Text(
                            text = stringResource(R.string.lbl_recommendations),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
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

        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.lbl_recommendations),
                    modifier = Modifier.graphicsLayer { alpha = appBarAlpha },
                )
            },
            navigationIcon = {
                Image(
                    painter = painterResource(R.drawable.ic_akilimo_logo_white),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier
                        .padding(start = AkilimoSpacing.md)
                        .size(AkilimoSpacing.xxl),
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = appBarAlpha),
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}

@Composable
private fun AdviceOptionCard(option: AdviceOption, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(AkilimoSpacing.xxxl),
            ) {
                Icon(
                    imageVector = option.valueOption.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(AkilimoSpacing.sm),
                )
            }
            Spacer(Modifier.width(AkilimoSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.valueOption.title(context),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = option.valueOption.label(context),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(AkilimoSpacing.xs))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
