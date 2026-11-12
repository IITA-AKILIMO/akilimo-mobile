package com.akilimo.mobile.ui.screens.farm

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.screens.onboarding.FarmLocationSection
import com.akilimo.mobile.ui.screens.onboarding.FieldSection
import com.akilimo.mobile.ui.screens.onboarding.PracticesSection
import com.akilimo.mobile.ui.screens.profile.DetailsEditorScreen

/**
 * The farm: country and location, field size, and the investment preference (how often the farmer
 * invests in new farm products). Planting and harvest dates, tillage and weed control are edited in
 * their own recommendation steps, so they are not repeated here.
 */
@Composable
fun FarmScreen(navController: NavHostController) {
    DetailsEditorScreen(
        navController = navController,
        title = stringResource(R.string.lbl_your_farm),
        subtitle = stringResource(R.string.lbl_edit_farm),
    ) { state, viewModel, section ->
        FarmLocationSection(state, viewModel::onEvent, navController, section)
        FieldSection(state, viewModel::onEvent, section, includeDates = false)
        PracticesSection(state, viewModel::onEvent, section, includeTillage = false)
    }
}
