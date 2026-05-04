package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R

/**
 * Enhanced TopAppBar with back navigation and subtitle support.
 *
 * When [collapsedTitle] and [scrollBehavior] are both provided, renders a
 * [LargeTopAppBar] that crossfades between the full [title]/[subtitle] in the
 * expanded state and [collapsedTitle] (a single short word) in the collapsed
 * state. The caller must apply `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`
 * to the enclosing Scaffold so the bar actually collapses on scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopAppBar(
    title: String,
    subtitle: String? = null,
    collapsedTitle: String? = null,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val navIcon: @Composable () -> Unit = {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.lbl_back),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }

    if (collapsedTitle != null && scrollBehavior != null) {
        val fraction = scrollBehavior.state.collapsedFraction

        LargeTopAppBar(
            title = {
                Box {
                    // Expanded content: shrinks its reported height to 0 as bar collapses
                    // so the Box never reserves invisible space in the collapsed state.
                    Column(
                        modifier = Modifier
                            .graphicsLayer { alpha = 1f - fraction }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                val height = (placeable.height * (1f - fraction))
                                    .toInt()
                                    .coerceAtLeast(0)
                                layout(placeable.width, height) {
                                    placeable.place(0, 0)
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    // Collapsed content: a single short word fades in as the bar collapses
                    Text(
                        text = collapsedTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.graphicsLayer { alpha = fraction },
                    )
                }
            },
            navigationIcon = navIcon,
            actions = actions,
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                scrolledContainerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    } else {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            },
            navigationIcon = navIcon,
            actions = actions,
            modifier = modifier,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        )
    }
}
