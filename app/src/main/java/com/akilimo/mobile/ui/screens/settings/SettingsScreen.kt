package com.akilimo.mobile.ui.screens.settings

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.Role
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.Locales
import com.akilimo.mobile.R
import com.akilimo.mobile.data.ThemeMode
import com.akilimo.mobile.ui.components.compose.SegmentedTabRow
import com.akilimo.mobile.data.ReadingTextScale
import androidx.compose.material.icons.outlined.AccountCircle
import com.akilimo.mobile.navigation.AccountRoute
import com.akilimo.mobile.navigation.WebViewRoute
import com.akilimo.mobile.ui.viewmodels.AccountEntryViewModel
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.IconTile
import com.akilimo.mobile.ui.components.compose.PartnerLockup
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.SettingsViewModel
import com.akilimo.mobile.data.AppLanguage
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.akilimo.mobile.ui.components.compose.popBackStackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
    accountEntry: AccountEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val signedInAs by accountEntry.signedInAs.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearRecsDialog by remember { mutableStateOf(false) }
    var showResetBadgeDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val clearedMessage = stringResource(R.string.msg_recommendations_cleared)
    val badgeResetMessage = stringResource(R.string.msg_badge_reset)

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                // Settings are already persisted by the ViewModel before these effects
                is SettingsViewModel.Effect.LanguageChanged -> {
                    // Picking a language turns the override on, so it always applies
                    AppLanguage.apply(effect.languageTag)
                    if (AppLanguage.needsManualRecreate) (context as? Activity)?.recreate()
                }
                is SettingsViewModel.Effect.LockAppLanguageChanged -> {
                    // On: use the picked language. Off: follow the phone's language.
                    AppLanguage.apply(if (effect.locked) effect.languageTag else null)
                    if (AppLanguage.needsManualRecreate) (context as? Activity)?.recreate()
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

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.account_signout_title)) },
            text = { Text(stringResource(R.string.account_signout_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    accountEntry.signOut()
                }) { Text(stringResource(R.string.account_sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text(stringResource(R.string.lbl_cancel)) }
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
                onBack = { navController.popBackStackSafely() },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        ScrollableFormColumn(padding = paddingValues) {

            // ── Account (optional) ────────────────────────────────────────
            SettingsSection(stringResource(R.string.account_settings_section)) {
                ListItem(
                    headlineContent = {
                        Text(signedInAs ?: stringResource(R.string.account_settings_out_title))
                    },
                    supportingContent = {
                        if (signedInAs == null) Text(stringResource(R.string.account_settings_out_sub))
                    },
                    leadingContent = { IconTile(Icons.Outlined.AccountCircle) },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.clickable(role = Role.Button) { navController.navigate(AccountRoute()) },
                )
                // Signed in: sign out lives here, next to the account
                if (signedInAs != null) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.account_sign_out)) },
                        leadingContent = { IconTile(Icons.AutoMirrored.Outlined.Logout) },
                        modifier = Modifier.clickable(role = Role.Button) { showSignOutDialog = true },
                    )
                }
            }

            // ── Appearance ────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.lbl_appearance)) {
            // Light first: it is the default; System and Dark are the user's choice
            val themeOrder = listOf(ThemeMode.LIGHT, ThemeMode.SYSTEM, ThemeMode.DARK)
            Text(
                text = stringResource(R.string.lbl_theme),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xxs),
            )
            SegmentedTabRow(
                options = listOf(
                    stringResource(R.string.lbl_theme_light),
                    stringResource(R.string.lbl_theme_system),
                    stringResource(R.string.lbl_theme_dark),
                ),
                selectedIndex = themeOrder.indexOf(state.themeMode),
                onSelect = { viewModel.setThemeMode(themeOrder[it]) },
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md),
            )
            Text(
                text = stringResource(R.string.lbl_text_size),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(
                    start = AkilimoSpacing.md,
                    end = AkilimoSpacing.md,
                    top = AkilimoSpacing.sm,
                    bottom = AkilimoSpacing.xxs,
                ),
            )
            SegmentedTabRow(
                options = listOf(
                    stringResource(R.string.lbl_size_normal),
                    stringResource(R.string.lbl_size_large),
                    stringResource(R.string.lbl_size_largest),
                ),
                selectedIndex = ReadingTextScale.options.indexOf(state.textScale).coerceAtLeast(0),
                onSelect = { viewModel.setTextScale(ReadingTextScale.options[it]) },
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md),
            )
            Text(
                text = stringResource(R.string.lbl_text_size_sample),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(AkilimoSpacing.md),
            )

            }

            // ── Display ───────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.lbl_display_section)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_fertilizer_grid)) },
                leadingContent = { IconTile(Icons.Outlined.GridView) },
                trailingContent = {
                    Switch(checked = state.fertilizerGrid, onCheckedChange = null)
                },
                modifier = Modifier.toggleable(
                    value = state.fertilizerGrid,
                    role = Role.Switch,
                    onValueChange = { viewModel.setFertilizerGrid(it) },
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_remember_area_unit)) },
                leadingContent = { IconTile(Icons.Outlined.AspectRatio) },
                trailingContent = {
                    Switch(checked = state.rememberAreaUnit, onCheckedChange = null)
                },
                modifier = Modifier.toggleable(
                    value = state.rememberAreaUnit,
                    role = Role.Switch,
                    onValueChange = { viewModel.setRememberAreaUnit(it) },
                ),
            )

            }

            // ── Language ──────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.lbl_language)) {
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
                leadingContent = { IconTile(Icons.Outlined.Language) },
                trailingContent = {
                    Switch(checked = state.lockAppLanguage, onCheckedChange = null)
                },
                modifier = Modifier.toggleable(
                    value = state.lockAppLanguage,
                    role = Role.Switch,
                    onValueChange = { viewModel.setLockAppLanguage(it) },
                ),
            )

            }

            // ── About ─────────────────────────────────────────────────────
            SettingsSection(stringResource(R.string.lbl_about_section)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_app_version)) },
                supportingContent = { Text(BuildConfig.VERSION_NAME) },
                leadingContent = { IconTile(Icons.Outlined.Info) },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_about_title)) },
                supportingContent = { Text(stringResource(R.string.settings_about_body)) },
                leadingContent = { IconTile(Icons.Outlined.Lightbulb) },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_nigeria_site)) },
                supportingContent = { Text(stringResource(R.string.settings_nigeria_site_sub)) },
                leadingContent = { IconTile(Icons.Outlined.Language) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(role = Role.Button) {
                    navController.navigate(
                        WebViewRoute(
                            url = "https://akilimonigeria.org",
                            title = context.getString(R.string.settings_nigeria_site),
                        )
                    )
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_privacy_policy)) },
                leadingContent = { IconTile(Icons.Outlined.PrivacyTip) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(role = Role.Button) {
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
                leadingContent = { IconTile(Icons.Outlined.Gavel) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(role = Role.Button) {
                    navController.navigate(
                        WebViewRoute(
                            url = "file:///android_asset/terms.html",
                            title = context.getString(R.string.lbl_terms),
                        )
                    )
                },
            )

            }

            // ── Data & Storage ────────────────────────────────────────────
            SettingsSection(stringResource(R.string.lbl_data_storage)) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_clear_recommendations)) },
                supportingContent = { Text(stringResource(R.string.lbl_clear_recommendations_desc)) },
                leadingContent = { IconTile(Icons.Outlined.CleaningServices) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(role = Role.Button) { showClearRecsDialog = true },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.lbl_reset_notification_badge)) },
                supportingContent = { Text(stringResource(R.string.lbl_reset_notification_badge_desc)) },
                leadingContent = { IconTile(Icons.Outlined.NotificationsNone) },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable(role = Role.Button) { showResetBadgeDialog = true },
            )
            }

            // The IITA and CGIAR logo: last on the page, on a card of its own
            PartnerLockup(modifier = Modifier.padding(vertical = AkilimoSpacing.md))
        }
    }
}

/** A titled group of settings rows on one raised card. */
@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.sm),
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AkilimoSpacing.md),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = AkilimoSpacing.xs), content = content)
    }
    Spacer(Modifier.height(AkilimoSpacing.xs))
}
