package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.akilimo.mobile.R

/**
 * A read-only outlined text field that opens a date picker when tapped.
 * The field is non-editable and shows a calendar icon in the trailing slot.
 *
 * Accessibility: the visual text field is hidden from TalkBack; the tap overlay is the
 * single focusable node, announced as "<label>, <value>, Button, double-tap to choose date"
 * (plus the error, if any).
 */
@Composable
fun DateInputField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    error: String? = null,
) {
    val clickLabel = stringResource(R.string.cmp_choose_date)
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            trailingIcon = { Icon(Icons.Outlined.DateRange, contentDescription = null) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {},
        )
        // Overlay captures taps without fighting the TextField's touch handling and
        // carries the field's semantics. Ripple is kept as visual tap feedback.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(
                    enabled = enabled,
                    onClickLabel = clickLabel,
                    role = Role.Button,
                    onClick = onClick,
                )
                .semantics {
                    contentDescription = label
                    val shown = value.ifEmpty { placeholder }
                    if (shown.isNotEmpty()) stateDescription = shown
                    if (error != null) error(error)
                }
        )
    }
}
