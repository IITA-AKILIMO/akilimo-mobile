package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/** What the leading icon of [BackTopAppBar] shows. */
enum class TopBarNavigation {
    /** Arrow: go back / up (the default) */
    BACK,

    /** ✕: leave the screen without saving (e.g. a multi-page edit with its own Back button) */
    CLOSE,

    /** No icon: nowhere to go up to (e.g. a start destination with its own Back button) */
    NONE,
}

/**
 * Enhanced TopAppBar with back navigation and subtitle support.
 *
 * When [collapsedTitle] and [scrollBehavior] are both provided, renders a
 * [LargeTopAppBar] that crossfades between the full [title]/[subtitle] in the
 * expanded state and [collapsedTitle] (a single short word) in the collapsed
 * state. The caller must apply `Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)`
 * to the enclosing Scaffold so the bar actually collapses on scroll.
 *
 * [expandedHeight] and [expandedMaxLines] (large bar only) make room for a longer expanded
 * title/subtitle, e.g. a welcome message used as the screen's hero. [expandedLeading] (large
 * bar only) is drawn above the expanded title, e.g. a logo, and collapses away with it.
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
    navigation: TopBarNavigation = TopBarNavigation.BACK,
    expandedHeight: Dp = TopAppBarDefaults.LargeAppBarExpandedHeight,
    expandedMaxLines: Int = 2,
    expandedLeading: (@Composable () -> Unit)? = null,
) {
    val navIcon: @Composable () -> Unit = {
        if (navigation != TopBarNavigation.NONE) {
            val isClose = navigation == TopBarNavigation.CLOSE
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = if (isClose) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(if (isClose) R.string.cmp_close else R.string.lbl_back),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }

    if (collapsedTitle != null && scrollBehavior != null) {
        // collapsedFraction changes every scroll frame: read it only inside the
        // graphicsLayer / layout lambdas so scrolling triggers redraw/relayout,
        // never recomposition.
        val state = scrollBehavior.state

        LargeTopAppBar(
            title = {
                Box {
                    // Expanded content: shrinks its reported height to 0 as bar collapses
                    // so the Box never reserves invisible space in the collapsed state.
                    Column(
                        modifier = Modifier
                            .graphicsLayer { alpha = 1f - state.collapsedFraction }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(constraints)
                                val height = (placeable.height * (1f - state.collapsedFraction))
                                    .toInt()
                                    .coerceAtLeast(0)
                                layout(placeable.width, height) {
                                    placeable.place(0, 0)
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (expandedLeading != null) {
                            expandedLeading()
                            Spacer(Modifier.height(AkilimoSpacing.xs))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            maxLines = expandedMaxLines,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                maxLines = expandedMaxLines,
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
                        modifier = Modifier.graphicsLayer { alpha = state.collapsedFraction },
                    )
                }
            },
            navigationIcon = navIcon,
            actions = actions,
            modifier = modifier,
            expandedHeight = expandedHeight,
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
                        // Two 28sp lines fit the 64dp bar; with a subtitle only one does.
                        maxLines = if (subtitle == null) 2 else 1,
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
                // Title/icons are onPrimary, so the scrolled colour must stay primary
                // (primaryContainer would make them unreadable).
                scrolledContainerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        )
    }
}
