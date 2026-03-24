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
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Gavel
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.SettingsViewModel
import dev.b3nedikt.app_locale.AppLocale
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
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
            }
        }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_settings),
                onBack = { navController.popBackStack() },
            )
        },
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
        }
    }
}
