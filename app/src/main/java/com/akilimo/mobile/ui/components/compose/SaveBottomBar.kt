package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * Bottom bar for primary actions like Save or Next.
 *
 * - While [isLoading] the button keeps its primary colours (so the spinner stays
 *   visible) but ignores clicks; TalkBack announces the loading state.
 * - The bar pads itself for the navigation bar **and** the soft keyboard, so it sits
 *   directly above the IME. When used as a Scaffold `bottomBar`, the Scaffold content
 *   padding then already includes the keyboard height; [ScrollableFormColumn] consumes
 *   that padding before applying its own IME padding, so nothing is padded twice.
 */
@Composable
fun SaveBottomBar(
    label: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loadingDescription = stringResource(R.string.cmp_loading)
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            // union = max(navBars, ime) per edge → one inset, never summed.
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
    ) {
        Button(
            onClick = { if (!isLoading) onClick() },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AkilimoSpacing.md)
                .heightIn(min = 56.dp)
                .semantics { if (isLoading) stateDescription = loadingDescription },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                // Opaque, readable disabled colours (default M3 38% alpha is too faint outdoors)
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = LocalContentColor.current,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(AkilimoSpacing.sm))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
