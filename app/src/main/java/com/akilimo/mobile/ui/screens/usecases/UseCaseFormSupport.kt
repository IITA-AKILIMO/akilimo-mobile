package com.akilimo.mobile.ui.screens.usecases

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.os.ConfigurationCompat
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.components.compose.SaveBottomBar
import com.akilimo.mobile.ui.components.compose.SkeletonList
import com.akilimo.mobile.ui.components.compose.StateMessage
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import java.text.NumberFormat
import java.util.Locale

/** The locale the UI is currently rendered in (follows the in-app language picker). */
@Composable
internal fun currentLocale(): Locale =
    ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: Locale.getDefault()

/** A grouping, locale-aware number formatter (e.g. "1,500" in en, "1.500" in some locales). */
@Composable
internal fun rememberNumberFormat(maxFractionDigits: Int = 2): NumberFormat {
    val locale = currentLocale()
    return remember(locale, maxFractionDigits) {
        NumberFormat.getInstance(locale).apply { maximumFractionDigits = maxFractionDigits }
    }
}

/**
 * Fills the legacy `{farm_size}` / `{size_unit}` placeholders used by the translated
 * tillage/tractor strings, formatting the size for the current locale instead of
 * `Double.toString()` (which always prints "2.0").
 */
internal fun fillFarmSizeTemplate(
    template: String,
    farmSize: Double,
    areaUnitLabel: String,
    numberFormat: NumberFormat,
): String = template
    .replace("{farm_size}", numberFormat.format(farmSize))
    .replace("{size_unit}", areaUnitLabel)

/** Decimal keypad plus Next/Done so multi-field forms can be filled without closing the keyboard. */
internal fun amountKeyboard(isLast: Boolean = false) = KeyboardOptions(
    keyboardType = KeyboardType.Decimal,
    imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
)

/**
 * Label for a price field: "Price per 100 kg bag (NGN)". Falls back to the generic
 * "Unit price" while no unit has been chosen yet.
 */
@Composable
internal fun pricePerUnitLabel(unitLabel: String?, currencyCode: String): String = when {
    unitLabel != null && currencyCode.isNotBlank() ->
        stringResource(R.string.lbl_price_per_unit_currency, unitLabel, currencyCode)
    unitLabel != null -> stringResource(R.string.lbl_price_per_unit, unitLabel)
    currencyCode.isNotBlank() ->
        stringResource(R.string.lbl_with_currency, stringResource(R.string.lbl_unit_price), currencyCode)
    else -> stringResource(R.string.lbl_unit_price)
}

/** Currency code shown at the end of a cost field, e.g. "NGN". */
internal fun currencySuffix(currencyCode: String): (@Composable () -> Unit)? =
    currencyCode.takeIf { it.isNotBlank() }?.let { code ->
        {
            Text(
                text = code,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = AkilimoSpacing.sm),
            )
        }
    }

/**
 * The shared Save bottom bar for use-case forms. When Save is disabled, [disabledReason]
 * is shown just above the button so the farmer knows what is still missing.
 */
@Composable
internal fun FormSaveBar(
    enabled: Boolean,
    isSaving: Boolean,
    disabledReason: String?,
    onSave: () -> Unit,
    label: String = stringResource(R.string.lbl_save),
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!enabled && !isSaving && disabledReason != null) {
            Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = disabledReason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = AkilimoSpacing.md)
                        .padding(top = AkilimoSpacing.xs)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
        }
        SaveBottomBar(
            label = label,
            enabled = enabled,
            isLoading = isSaving,
            onClick = { if (enabled && !isSaving) onSave() },
        )
    }
}

/** Shows the generic "could not save" snackbar once per failure. */
@Composable
internal fun SaveErrorEffect(
    saveFailed: Boolean,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    val message = stringResource(R.string.error_save_failed)
    LaunchedEffect(saveFailed) {
        if (saveFailed) {
            // Clear first so a rotation while the snackbar is visible does not show it twice
            onShown()
            snackbarHostState.showSnackbar(message)
        }
    }
}

/** Top-bar action switching a picker between grid and list; describes the action for TalkBack. */
@Composable
internal fun LayoutToggleButton(isGridLayout: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            painter = painterResource(if (isGridLayout) R.drawable.ic_list else R.drawable.ic_grid),
            contentDescription = stringResource(
                if (isGridLayout) R.string.lbl_show_as_list else R.string.lbl_show_as_grid
            ),
        )
    }
}

/** Placeholder rows for a list that is still loading. */
@Composable
internal fun ListLoading(modifier: Modifier = Modifier) {
    SkeletonList(modifier = modifier)
}

/**
 * Shown in place of a list that loaded but has nothing to choose from. [showRefreshHint] says to
 * pull down to refresh: turn it off where the empty state is not inside a pull-to-refresh list.
 */
@Composable
internal fun ListEmpty(message: String, modifier: Modifier = Modifier, showRefreshHint: Boolean = true) {
    StateMessage(
        icon = Icons.Outlined.Inbox,
        title = stringResource(R.string.lbl_nothing_to_show),
        message = message,
        hint = stringResource(R.string.msg_pull_to_refresh).takeIf { showRefreshHint },
        modifier = modifier.padding(top = AkilimoSpacing.xl),
    )
}
/** Pull-to-refresh around a list. [content] must scroll; wrap a loading or empty state in [PullableState]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RefreshableList(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = onRefresh, modifier = modifier) { content() }
}

/** Makes a loading or empty state scrollable, so it can still be pulled down to refresh. */
@Composable
internal fun PullableState(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) { content() }
}
