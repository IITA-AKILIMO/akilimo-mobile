package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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
    readOnly: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
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
            readOnly = readOnly,
            shape = MaterialTheme.shapes.small,
        )

        val message = error ?: helperText
        if (message != null) {
            Spacer(Modifier.height(AkilimoSpacing.xxs))
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = if (error != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = AkilimoSpacing.md)
            )
        }
    }
}
