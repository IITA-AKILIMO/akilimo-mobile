package com.akilimo.mobile.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.OnboardingRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.ScrollableFormColumn
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_settings),
                onBack = { navController.popBackStack() },
            )
        },
    ) { paddingValues ->
        ScrollableFormColumn(padding = paddingValues) {
            // ── Appearance ────────────────────────────────────────────────────
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
                    Icon(
                        imageVector = Icons.Outlined.DarkMode,
                        contentDescription = null,
                    )
                },
                trailingContent = {
                    Switch(
                        checked = state.darkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = AkilimoSpacing.sm))

            // ── Profile ───────────────────────────────────────────────────────
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
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = null,
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable { navController.navigate(OnboardingRoute) },
            )
        }
    }
}
