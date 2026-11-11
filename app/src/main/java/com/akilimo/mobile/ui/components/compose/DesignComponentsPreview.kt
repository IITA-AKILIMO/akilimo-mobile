package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.ui.theme.AkilimoTheme

@Preview(showBackground = true, heightDp = 900)
@Composable
@Suppress("UnusedPrivateMember", "MagicNumber") // invoked by the Android Studio preview renderer
private fun DesignComponentsPreview() {
    AkilimoTheme {
        var tab by remember { mutableIntStateOf(0) }
        Column(
            modifier = Modifier.padding(AkilimoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AkilimoSpacing.md),
        ) {
            SyncBanner(
                title = "Online",
                detail = "5 items to sync",
                actionLabel = "Sync now",
            )
            ProgressSummaryCard(title = "Steps completed", progressLabel = "3 of 8", progress = 3f / 8f)
            StatSummaryCard(
                title = "Fertilizer prices",
                stats = listOf(
                    StatItem("Available", "12"),
                    StatItem("Selected", "3", StatusTone.Success),
                    StatItem("Per bag", "NGN 18,500"),
                ),
            )
            SegmentedTabRow(
                options = listOf("Overview", "Costs", "Prices"),
                selectedIndex = tab,
                onSelect = { tab = it },
            )
            AccentCard(onClick = {}) {
                Text("Urea (50 kg bag)")
                StatusChip("Completed", tone = StatusTone.Success, icon = Icons.Default.Check)
            }
            AccentCard(accent = androidx.compose.material3.MaterialTheme.colorScheme.tertiary) {
                Text("Planting date")
                StatusChip("Pending", tone = StatusTone.Warning, icon = Icons.Default.Schedule)
            }
        }
    }
}
