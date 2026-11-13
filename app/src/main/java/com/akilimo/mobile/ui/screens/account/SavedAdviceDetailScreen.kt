package com.akilimo.mobile.ui.screens.account

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.StateMessage
import com.akilimo.mobile.ui.components.compose.cardSurface
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.screens.recommendations.splitRecommendationParagraphs
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.SavedAdviceDetailViewModel

/** One saved advice: read it again, share it (for example with an extension worker), or delete it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedAdviceDetailScreen(
    navController: NavHostController,
    viewModel: SavedAdviceDetailViewModel = hiltViewModel(),
) {
    val advice by viewModel.advice.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showDelete by rememberSaveable { mutableStateOf(false) }
    val title = advice?.let { adviceKindLabel(it.recType ?: it.useCase) } ?: stringResource(R.string.saved_advice_title)
    val shareSubject = stringResource(R.string.saved_advice_share_subject)
    val shareLabel = stringResource(R.string.saved_advice_share)
    val text = advice?.recommendation

    LaunchedEffect(state.gone) {
        if (state.gone) navController.popBackStackSafely()
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = title,
                onBack = { navController.popBackStackSafely() },
                actions = {
                    if (text != null) {
                        IconButton(onClick = {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, shareSubject)
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(send, shareLabel))
                        }) { Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.saved_advice_share)) }
                    }
                    IconButton(onClick = { showDelete = true }, enabled = !state.deleting) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.saved_advice_delete))
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                text != null -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(AkilimoSpacing.md),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().cardSurface().padding(AkilimoSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.md),
                    ) {
                        remember(text) { splitRecommendationParagraphs(text) }.forEach { paragraph ->
                            Text(text = paragraph, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    state.messageRes?.let {
                        Spacer(Modifier.height(AkilimoSpacing.md))
                        Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                state.loadingText -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                advice == null -> StateMessage(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.saved_advice_title),
                    message = stringResource(R.string.saved_advice_not_found),
                    isError = true,
                    modifier = Modifier.align(Alignment.Center),
                )

                else -> StateMessage(
                    icon = Icons.Outlined.ErrorOutline,
                    title = stringResource(R.string.saved_advice_title),
                    message = state.messageRes?.let { stringResource(it) } ?: stringResource(R.string.saved_advice_text_unavailable),
                    isError = true,
                    actionLabel = stringResource(R.string.lbl_retry),
                    onAction = viewModel::loadText,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.saved_advice_delete_title)) },
            text = { Text(stringResource(R.string.saved_advice_delete_body)) },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.delete() }) {
                    Text(stringResource(R.string.saved_advice_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text(stringResource(R.string.lbl_cancel)) } },
        )
    }
}

private fun stringResource_share(context: android.content.Context): String = context.getString(R.string.saved_advice_share)
