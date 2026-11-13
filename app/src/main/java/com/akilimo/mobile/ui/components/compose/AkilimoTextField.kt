package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import com.akilimo.mobile.ui.theme.AkilimoSpacing

/**
 * Enhanced OutlinedTextField with better Material 3 styling,
 * supporting helper text and error messages.
 */
@Composable
fun AkilimoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    /** Name read by TalkBack when [label] is empty (e.g. the field is named by a visible heading). */
    accessibilityLabel: String? = null,
) {
    val fieldName = label.ifEmpty { accessibilityLabel.orEmpty() }
    Column(modifier = modifier.fillMaxWidth()) {
        // The label sits above the field so it stays readable while typing; the field itself
        // carries the name for TalkBack, so the visible text is not read a second time.
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(bottom = AkilimoSpacing.xxs)
                    .clearAndSetSemantics {},
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder?.let { { Text(it) } },
            isError = error != null,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (fieldName.isNotEmpty()) Modifier.semantics { contentDescription = fieldName }
                    else Modifier
                ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.outline,
            ),
            keyboardOptions = keyboardOptions,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            readOnly = readOnly,
            shape = MaterialTheme.shapes.small,
            // Error/helper go in supportingText so TalkBack links them to the field
            // (error is announced via isError). Colour follows the field's error state.
            supportingText = (error ?: helperText)?.let { message ->
                { Text(text = message, style = MaterialTheme.typography.bodySmall) }
            },
        )
    }
}
