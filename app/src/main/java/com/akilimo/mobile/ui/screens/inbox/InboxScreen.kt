package com.akilimo.mobile.ui.screens.inbox

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.entities.InboxMessage
import com.akilimo.mobile.navigation.toRoute
import com.akilimo.mobile.notifications.NotificationRouting
import com.akilimo.mobile.notifications.NotificationType
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.IconTile
import com.akilimo.mobile.ui.components.compose.SkeletonList
import com.akilimo.mobile.ui.components.compose.StateMessage
import com.akilimo.mobile.ui.components.compose.cardSurface
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.screens.account.savedWhen
import com.akilimo.mobile.ui.screens.recommendations.navigateIfResumed
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.InboxViewModel

/** Every message AKILIMO sent to this phone, newest first. Tap one to read it (that marks it read). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    navController: NavHostController,
    viewModel: InboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var confirmDeleteAll by rememberSaveable { mutableStateOf(false) }
    val messages = state.messages

    // The view model already fetched once when it was created; every later arrival on the screen (back from another app,
    // a tapped notification while the app was in the background) fetches again so the list is current
    var firstResume by remember { mutableStateOf(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (firstResume) firstResume = false else viewModel.refresh()
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.inbox_title),
                onBack = { navController.popBackStackSafely() },
                actions = {
                    if (!messages.isNullOrEmpty()) {
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.inbox_more_options))
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.inbox_mark_all_read)) },
                                    leadingIcon = { Icon(Icons.Outlined.DoneAll, contentDescription = null) },
                                    enabled = state.unreadCount > 0,
                                    onClick = {
                                        menuOpen = false
                                        viewModel.markAllRead()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.inbox_delete_all), color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    },
                                    onClick = {
                                        menuOpen = false
                                        confirmDeleteAll = true
                                    },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        // Pull down to fetch again; the spinner is the status while it runs and a note says when it did not work
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
        when {
            messages == null -> SkeletonList(modifier = Modifier.padding(AkilimoSpacing.md))
            messages.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AkilimoSpacing.md),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.refreshFailed) RefreshFailedNote()
                StateMessage(
                    icon = Icons.Outlined.Inbox,
                    title = stringResource(R.string.inbox_empty_title),
                    message = stringResource(R.string.inbox_empty_msg),
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AkilimoSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
            ) {
                if (state.refreshFailed) item(key = "refresh_failed") { RefreshFailedNote() }
                items(messages, key = { it.id }) { message ->
                    InboxRow(
                        message = message,
                        expanded = state.expandedId == message.id,
                        onClick = { viewModel.toggle(message.id) },
                        onDelete = { viewModel.delete(message.id) },
                        onOpenTarget = {
                            val target = NotificationRouting.resolveTarget(
                                NotificationType.fromWireName(message.type),
                                message.target,
                            )
                            navController.navigateIfResumed(target.toRoute())
                        },
                    )
                }
            }
        }
        }
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            title = { Text(stringResource(R.string.inbox_delete_all_title)) },
            text = { Text(stringResource(R.string.inbox_delete_all_body)) },
            confirmButton = {
                TextButton(onClick = { confirmDeleteAll = false; viewModel.deleteAll() }) {
                    Text(stringResource(R.string.inbox_delete_all_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteAll = false }) { Text(stringResource(R.string.lbl_cancel)) } },
        )
    }
}

/** Shown above the list when the last fetch failed or timed out: the saved messages are still there. */
@Composable
private fun RefreshFailedNote() {
    Text(
        text = stringResource(R.string.inbox_refresh_failed),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth().padding(bottom = AkilimoSpacing.sm),
    )
}

@Composable
private fun InboxRow(
    message: InboxMessage,
    expanded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onOpenTarget: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cardSurface()
            .clickable(onClick = onClick)
            .padding(AkilimoSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
    ) {
        IconTile(typeIcon(message.type))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs)) {
            Text(
                text = message.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (message.isRead) FontWeight.Normal else FontWeight.Bold,
            )
            Text(
                text = savedWhen(message.receivedAt) + if (message.isRead) "" else " · " + stringResource(R.string.inbox_unread),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
            )
            if (expanded) {
                Row(horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.xs)) {
                    TextButton(onClick = onOpenTarget) { Text(stringResource(R.string.inbox_open)) }
                    TextButton(onClick = onDelete) {
                        Text(stringResource(R.string.inbox_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

private fun typeIcon(type: String): ImageVector = when (NotificationType.fromWireName(type)) {
    NotificationType.ADVICE -> Icons.Outlined.Lightbulb
    NotificationType.REMINDER -> Icons.Outlined.Notifications
    NotificationType.ANNOUNCEMENT -> Icons.Outlined.Campaign
}
