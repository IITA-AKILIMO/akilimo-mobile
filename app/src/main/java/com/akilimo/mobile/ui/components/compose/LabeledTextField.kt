package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * A section label (announced as a heading) followed by an [AkilimoTextField].
 *
 * [hint] is the field's own floating label. It defaults to empty so the section label is
 * not shown and read twice; pass a more specific hint (e.g. a unit) when useful.
 */
@Composable
fun LabeledTextField(
    sectionLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = sectionLabel,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.semantics { heading() },
        )
        AkilimoTextField(
            value = value,
            onValueChange = onValueChange,
            label = hint,
            error = error,
            keyboardOptions = keyboardOptions,
            accessibilityLabel = sectionLabel,
        )
        Spacer(modifier = Modifier.height(AkilimoSpacing.xs))
    }
}
