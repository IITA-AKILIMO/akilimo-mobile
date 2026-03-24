package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

@Composable
fun WizardBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shadowElevation = 4.dp,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
        ) {
            if (!isFirstStep) {
                TextButton(onClick = onBack) {
                    Text(stringResource(R.string.lbl_back))
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }

            Text(
                text = "${currentStep + 1} / $totalSteps",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(onClick = onNext) {
                Text(
                    if (isLastStep) stringResource(R.string.lbl_finish)
                    else stringResource(R.string.lbl_next),
                )
            }
        }
    }
}
