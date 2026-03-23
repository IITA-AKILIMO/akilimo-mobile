package com.akilimo.mobile.ui.screens.recommendations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedbackBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, nps: Int) -> Unit,
) {
    var selectedRating by remember { mutableStateOf<Int?>(null) }
    var selectedNps by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "How satisfied are you?",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(12.dp))
            // 5 emoji rating buttons
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val emojis = listOf("\uD83D\uDE21", "\uD83D\uDE15", "\uD83D\uDE10", "\uD83D\uDE0A", "\uD83D\uDE0D")
                emojis.forEachIndexed { index, emoji ->
                    val rating = index + 1
                    TextButton(
                        onClick = { selectedRating = rating },
                        colors = if (selectedRating == rating) {
                            ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        } else {
                            ButtonDefaults.textButtonColors()
                        },
                    ) {
                        Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
            selectedRating?.let { rating ->
                val label = when (rating) {
                    1 -> stringResource(R.string.feedback_very_poor)
                    2 -> stringResource(R.string.feedback_poor)
                    3 -> stringResource(R.string.feedback_okay)
                    4 -> stringResource(R.string.feedback_good)
                    5 -> stringResource(R.string.feedback_excellent)
                    else -> ""
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "How likely are you to recommend us? (0-10)",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            // NPS: 0-10 in a wrap row
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                (0..10).forEach { score ->
                    TextButton(
                        onClick = { selectedNps = score },
                        colors = if (selectedNps == score) {
                            ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        } else {
                            ButtonDefaults.textButtonColors()
                        },
                    ) {
                        Text(score.toString())
                    }
                }
            }
            selectedNps?.let { nps ->
                val label = when {
                    nps <= 6 -> stringResource(R.string.feedback_nps_low)
                    nps <= 8 -> stringResource(R.string.feedback_nps_medium)
                    else -> stringResource(R.string.feedback_nps_high)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val r = selectedRating
                    val n = selectedNps
                    if (r != null && n != null) onSubmit(r, n)
                },
                enabled = selectedRating != null && selectedNps != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Submit Feedback")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
