package com.akilimo.mobile.ui.screens.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumOperationMethod
import com.akilimo.mobile.enums.EnumOperationType
import com.akilimo.mobile.enums.EnumWeedControlMethod
import com.akilimo.mobile.ui.components.compose.AkilimoDropdown
import com.akilimo.mobile.ui.viewmodels.OnboardingViewModel

@Composable
fun TillageStep(
    tillageOperations: Map<EnumOperationType, EnumOperationMethod>,
    weedControlEnabled: Boolean,
    weedControlMethod: EnumWeedControlMethod?,
    errors: Map<String, String>,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tillageTypes = remember { listOf(EnumOperationType.PLOUGHING, EnumOperationType.RIDGING) }
    val methods = remember { EnumOperationMethod.entries }
    val weedMethods = remember { EnumWeedControlMethod.entries }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.lbl_tillage_operations), style = MaterialTheme.typography.headlineMedium)

        tillageTypes.forEach { type ->
            val checked = type in tillageOperations
            val selectedMethod = tillageOperations[type]
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onEvent(OnboardingViewModel.Event.TillageOperationToggled(type, it)) },
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(type.label(context))
                    }
                    if (checked) {
                        AkilimoDropdown(
                            label = stringResource(R.string.lbl_select_tillage_method),
                            options = methods,
                            selectedOption = selectedMethod,
                            onOptionSelected = { onEvent(OnboardingViewModel.Event.TillageMethodSelected(type, it)) },
                            displayText = { it.label(context) },
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = weedControlEnabled,
                        onCheckedChange = { onEvent(OnboardingViewModel.Event.WeedControlToggled(it)) },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(EnumOperationType.WEEDING.label(context))
                }
                if (weedControlEnabled) {
                    AkilimoDropdown(
                        label = stringResource(R.string.lbl_select_tillage_method),
                        options = weedMethods,
                        selectedOption = weedControlMethod,
                        onOptionSelected = { onEvent(OnboardingViewModel.Event.WeedControlMethodSelected(it)) },
                        displayText = { it.label(context) },
                        error = errors["weedControl"],
                    )
                }
            }
        }
    }
}
