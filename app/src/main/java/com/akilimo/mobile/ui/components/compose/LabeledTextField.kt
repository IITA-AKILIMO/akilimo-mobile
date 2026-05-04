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
import androidx.compose.ui.unit.dp

@Composable
fun LabeledTextField(
    sectionLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = sectionLabel,
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = sectionLabel, style = MaterialTheme.typography.labelMedium)
        AkilimoTextField(
            value = value,
            onValueChange = onValueChange,
            label = hint,
            error = error,
            keyboardOptions = keyboardOptions
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
