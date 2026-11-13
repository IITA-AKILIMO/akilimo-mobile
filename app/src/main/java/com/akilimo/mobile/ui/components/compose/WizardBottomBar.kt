package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * Back / "Step X of Y" / Next bar for multi-step flows.
 *
 * @param nextEnabled disables the Next/Finish button (e.g. until the step is valid).
 * @param isLoading shows a spinner in the Next/Finish button and ignores taps on it.
 * @param finishLabel replaces "Finish" on the last step (e.g. "Save" when editing).
 */
@Composable
fun WizardBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
    nextEnabled: Boolean = true,
    isLoading: Boolean = false,
    finishLabel: String? = null,
) {
    val loadingDescription = stringResource(R.string.cmp_loading)
    Surface(
        shadowElevation = 4.dp,
        modifier = modifier.navigationBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs),
        ) {
            // Equal-weight side slots keep the step counter centred whether or not
            // the Back button is shown, and let long labels ellipsize instead of
            // pushing into the counter.
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (!isFirstStep) {
                    OutlinedButton(onClick = onBack, shape = MaterialTheme.shapes.medium) {
                        Text(
                            text = stringResource(R.string.lbl_back),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            Text(
                text = stringResource(R.string.cmp_wizard_step_progress, currentStep + 1, totalSteps),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = AkilimoSpacing.xs),
            )

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick = { if (!isLoading) onNext() },
                    enabled = nextEnabled,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.semantics {
                        if (isLoading) stateDescription = loadingDescription
                    },
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = LocalContentColor.current,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(AkilimoSpacing.xs))
                    }
                    Text(
                        text = if (isLastStep) finishLabel ?: stringResource(R.string.lbl_finish)
                        else stringResource(R.string.lbl_next),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
