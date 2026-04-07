package com.akilimo.mobile.ui.screens.settings

import android.app.Activity
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.OnboardingRoute
import com.akilimo.mobile.navigation.WebViewRoute
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.SettingsViewModel
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearRecsDialog by remember { mutableStateOf(false) }
    var showResetBadgeDialog by remember { mutableStateOf(false) }

    val clearedMessage = stringResource(R.string.msg_recommendations_cleared)
    val badgeResetMessage = stringResource(R.string.msg_badge_reset)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SettingsViewModel.Effect.LanguageChanged -> {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(effect.languageTag),
                    )
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        (context as? Activity)?.recreate()
                    }
                }
                is SettingsViewModel.Effect.LockAppLanguageChanged -> {
                    if (effect.locked) {
                        AppLocale.supportedLocales = Locales.supportedLocales
                        AppLocale.desiredLocale = Locales.supportedLocales
                            .find { it.toLanguageTag() == effect.languageTag } ?: Locales.english
                    } else {
                        AppLocale.desiredLocale = java.util.Locale.getDefault()
                    }
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(effect.languageTag),
                    )
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                        (context as? Activity)?.recreate()
                    }
                }
                is SettingsViewModel.Effect.ShowSnackbar -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        }
    }

    if (showClearRecsDialog) {
        AlertDialog(
            onDismissRequest = { showClearRecsDialog = false },
            title = { Text(stringResource(R.string.lbl_clear_recommendations)) },
            text = { Text(stringResource(R.string.msg_clear_recommendations_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearRecsDialog = false
                    viewModel.clearRecommendations(clearedMessage)
                }) { Text(stringResource(R.string.lbl_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearRecsDialog = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            },
        )
    }

    if (showResetBadgeDialog) {
        AlertDialog(
            onDismissRequest = { showResetBadgeDialog = false },
            title = { Text(stringResource(R.string.lbl_reset_notification_badge)) },
            text = { Text(stringResource(R.string.msg_reset_badge_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showResetBadgeDialog = false
                    viewModel.resetNotificationCount(badgeResetMessage)
                }) { Text(stringResource(R.string.lbl_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetBadgeDialog = false }) {
                    Text(stringResource(R.string.lbl_cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_settings),
                onBack = { navController.popBackStack() },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        ScrollableFormColumn(padding = paddingValues) {

            // ── Appearance ────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.lbl_appearance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = AkilimoSpacing.md,
                    vertical = AkilimoSpacing.sm,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_dark_mode)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.DarkMode, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = state.darkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AkilimoSpacing.sm))

            // ── Display ───────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.lbl_display_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = AkilimoSpacing.md,
                    vertical = AkilimoSpacing.sm,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_fertilizer_grid)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.GridView, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = state.fertilizerGrid,
                        onCheckedChange = { viewModel.setFertilizerGrid(it) },
                    )
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_remember_area_unit)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.AspectRatio, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = state.rememberAreaUnit,
                        onCheckedChange = { viewModel.setRememberAreaUnit(it) },
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AkilimoSpacing.sm))

            // ── Language ──────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.lbl_language),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = AkilimoSpacing.md,
                    vertical = AkilimoSpacing.sm,
                ),
            )
            AkilimoDropdown(
                label = stringResource(R.string.lbl_select_language),
                options = Locales.supportedLocales,
                selectedOption = Locales.supportedLocales
                    .find { it.toLanguageTag() == state.languageTag } ?: Locales.english,
                onOptionSelected = { locale -> viewModel.setLanguage(locale.toLanguageTag()) },
                displayText = { locale ->
                    locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercaseChar() }
                },
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_lock_app_language)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.Language, contentDescription = null)
                },
                trailingContent = {
                    Switch(
                        checked = state.lockAppLanguage,
                        onCheckedChange = { viewModel.setLockAppLanguage(it) },
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AkilimoSpacing.sm))

            // ── Profile ───────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.lbl_profile_settings),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = AkilimoSpacing.md,
                    vertical = AkilimoSpacing.sm,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_edit_profile)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.AccountCircle, contentDescription = null)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable { navController.navigate(OnboardingRoute) },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AkilimoSpacing.sm))

            // ── About ─────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.lbl_about_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = AkilimoSpacing.md,
                    vertical = AkilimoSpacing.sm,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_app_version)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.Info, contentDescription = null)
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_privacy_policy)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.PrivacyTip, contentDescription = null)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable {
                    navController.navigate(
                        WebViewRoute(
                            url = "file:///android_asset/privacy_policy.html",
                            title = context.getString(R.string.lbl_privacy_policy),
                        )
                    )
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_terms)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.Gavel, contentDescription = null)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable {
                    navController.navigate(
                        WebViewRoute(
                            url = "file:///android_asset/terms.html",
                            title = context.getString(R.string.lbl_terms),
                        )
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AkilimoSpacing.sm))

            // ── Data & Storage ────────────────────────────────────────────
            Text(
                text = stringResource(R.string.lbl_data_storage),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = AkilimoSpacing.md,
                    vertical = AkilimoSpacing.sm,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_clear_recommendations)) },
                supportingContent = { Text(stringResource(R.string.lbl_clear_recommendations_desc)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.CleaningServices, contentDescription = null)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable { showClearRecsDialog = true },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_reset_notification_badge)) },
                supportingContent = { Text(stringResource(R.string.lbl_reset_notification_badge_desc)) },
                leadingContent = {
                    Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = null)
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable { showResetBadgeDialog = true },
            )
        }
    }
}
