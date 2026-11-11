package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * Vertically scrolling form body that keeps the focused field above the soft keyboard.
 *
 * IME handling: [padding] (usually the Scaffold content padding) is applied and then
 * marked as consumed, so the following `imePadding()` only adds the part of the keyboard
 * height that the Scaffold padding does not already cover:
 * - Scaffold with [SaveBottomBar]: the bar lifts itself above the keyboard, so the
 *   Scaffold bottom padding already includes the IME height → extra IME padding is 0.
 * - Scaffold with a bar that stays behind the keyboard, or no bar: the extra padding is
 *   `ime - padding.bottom`, so the viewport ends exactly at the keyboard top.
 * Shrinking the scroll viewport lets Compose bring the focused text field into view.
 */
@Composable
fun ScrollableFormColumn(
    padding: PaddingValues = PaddingValues(0.dp),
    horizontalPadding: Dp = AkilimoSpacing.md,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .consumeWindowInsets(padding)
            .imePadding()
            .padding(horizontal = horizontalPadding)
            .verticalScroll(rememberScrollState()),
        content = content
    )
}
