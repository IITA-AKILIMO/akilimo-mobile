package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel
import com.akilimo.mobile.utils.DateHelper

@Composable
fun SummaryStep(
    state: OnboardingViewModel.UiState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Completion header — full-width, no horizontal padding
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(AkilimoSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(Modifier.width(AkilimoSpacing.md))
                Text(
                    text = stringResource(R.string.lbl_summary_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Section cards with horizontal padding
        Column(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
        ) {
            SummarySection(title = stringResource(R.string.lbl_self_intro)) {
                SummaryRow(stringResource(R.string.lbl_first_name), state.firstName)
                SummaryRow(stringResource(R.string.lbl_last_name), state.lastName)
                SummaryRow(stringResource(R.string.lbl_email_address), state.email)
                SummaryRow(stringResource(R.string.lbl_phone_number), state.phone)
                SummaryRow(stringResource(R.string.lbl_gender), state.gender)
            }

            SummarySection(title = stringResource(R.string.lbl_field)) {
                SummaryRow(stringResource(R.string.lbl_country), state.country.countryName)
                SummaryRow(
                    stringResource(R.string.lbl_farm_size),
                    "%.2f %s".format(state.farmSize, state.areaUnit.label(context))
                )
                if (state.latitude != 0.0) {
                    SummaryRow(
                        stringResource(R.string.lbl_location),
                        "Lat: %.5f, Lng: %.5f".format(state.latitude, state.longitude)
                    )
                }
            }

            SummarySection(title = stringResource(R.string.lbl_planting_harvest_dates)) {
                SummaryRow(
                    stringResource(R.string.lbl_planting_date),
                    DateHelper.formatToString(state.plantingDate)
                )
                SummaryRow(
                    stringResource(R.string.lbl_harvesting_date),
                    DateHelper.formatToString(state.harvestDate)
                )
            }

            SummarySection(title = stringResource(R.string.lbl_tillage_operations)) {
                state.tillageOperations.forEach { (type, method) ->
                    SummaryRow(type.label(context), method.label(context))
                }
                state.weedControlMethod?.let { method ->
                    SummaryRow(stringResource(R.string.lbl_weeding), method.label(context))
                }
            }

            SummarySection(title = stringResource(R.string.lbl_investment_pref)) {
                SummaryRow(
                    stringResource(R.string.lbl_investment_pref_prompt),
                    state.investmentPref.label(context)
                )
            }
        }
    }
}

@Composable
private fun SummarySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(AkilimoSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.xxs),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String?) {
    if (!value.isNullOrBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AkilimoSpacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
