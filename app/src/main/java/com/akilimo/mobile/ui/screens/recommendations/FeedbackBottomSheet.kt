package com.akilimo.mobile.ui.screens.recommendations

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

private const val RATING_OKAY = 3
private const val RATING_GOOD = 4
private const val RATING_EXCELLENT = 5
private const val NPS_MAX_SCORE = 10
private const val NPS_DETRACTOR_MAX = 6
private const val NPS_PASSIVE_MAX = 8
private val SELECTED_BORDER = 2.dp

@StringRes
private fun ratingLabelRes(rating: Int): Int = when (rating) {
    1 -> R.string.feedback_very_poor
    2 -> R.string.feedback_poor
    RATING_OKAY -> R.string.feedback_okay
    RATING_GOOD -> R.string.feedback_good
    else -> R.string.feedback_excellent
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, nps: Int) -> Unit,
) {
    var selectedRating by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedNps by rememberSaveable { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(AkilimoSpacing.md)) {
            Text(
                text = stringResource(R.string.lbl_how_satisfied),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(AkilimoSpacing.sm))
            // 5 emoji rating buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                val emojis = listOf("😡", "😕", "😐", "😊", "😍")
                emojis.forEachIndexed { index, emoji ->
                    val rating = index + 1
                    ChoiceButton(
                        selected = selectedRating == rating,
                        selectedColor = MaterialTheme.colorScheme.primaryContainer,
                        accessibilityLabel = stringResource(ratingLabelRes(rating)),
                        onClick = { selectedRating = rating },
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium,
                            // The emoji glyph name is noise for TalkBack; the label above replaces it
                            modifier = Modifier.clearAndSetSemantics { },
                        )
                    }
                }
            }
            selectedRating?.let { rating ->
                Text(
                    text = stringResource(ratingLabelRes(rating)),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(AkilimoSpacing.md))
            Text(
                text = stringResource(R.string.lbl_nps_prompt),
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(AkilimoSpacing.xs))
            // NPS: 0-10 in a wrap row
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                for (score in 0..NPS_MAX_SCORE) {
                    ChoiceButton(
                        selected = selectedNps == score,
                        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                        accessibilityLabel = null,
                        onClick = { selectedNps = score },
                    ) {
                        Text(score.toString(), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            selectedNps?.let { nps ->
                val label = when {
                    nps <= NPS_DETRACTOR_MAX -> stringResource(R.string.feedback_nps_low)
                    nps <= NPS_PASSIVE_MAX -> stringResource(R.string.feedback_nps_medium)
                    else -> stringResource(R.string.feedback_nps_high)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(AkilimoSpacing.md))
            Button(
                onClick = {
                    val r = selectedRating
                    val n = selectedNps
                    if (r != null && n != null) onSubmit(r, n)
                },
                enabled = selectedRating != null && selectedNps != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.lbl_submit_feedback))
            }
            Spacer(Modifier.height(AkilimoSpacing.md))
        }
    }
}

/**
 * A single-choice option exposed to TalkBack as a radio button with selected state, and
 * marked by a border as well as a fill colour so the choice is visible without colour.
 */
@Composable
private fun ChoiceButton(
    selected: Boolean,
    selectedColor: Color,
    accessibilityLabel: String?,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val labelModifier = if (accessibilityLabel != null) {
        Modifier.semantics { contentDescription = accessibilityLabel }
    } else {
        Modifier
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (selected) selectedColor else Color.Transparent,
        border = if (selected) BorderStroke(SELECTED_BORDER, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .sizeIn(minWidth = AkilimoSpacing.xxxl, minHeight = AkilimoSpacing.xxxl)
            .clip(MaterialTheme.shapes.small)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .then(labelModifier),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(AkilimoSpacing.xs)) {
            content()
        }
    }
}
