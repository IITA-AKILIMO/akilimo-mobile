package com.akilimo.mobile.ui.components.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.akilimo.mobile.R
import com.akilimo.mobile.ui.theme.AkilimoSpacing
import com.akilimo.mobile.utils.PhoneCountry
import com.blongho.country_data.World
import java.util.Locale

/**
 * Phone number field with a country code button (flag + "+255") in front of the number. Tapping
 * the button opens a searchable list of every country. [number] is typed without the code;
 * [region] is the ISO code of the selected country.
 */
@Composable
fun PhoneNumberField(
    number: String,
    onNumberChange: (String) -> Unit,
    region: String,
    dialCode: Int,
    onRegionChange: (String) -> Unit,
    countries: List<PhoneCountry>,
    suggestedRegions: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    imeAction: ImeAction = ImeAction.Done,
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val countryName = regionName(region)
    val buttonDescription = stringResource(R.string.cd_phone_country_code, countryName, dialCode)

    AkilimoTextField(
        value = number,
        onValueChange = onNumberChange,
        label = label,
        error = error,
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = imeAction),
        leadingIcon = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(start = AkilimoSpacing.xxs)
                    .clip(MaterialTheme.shapes.small)
                    .clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(R.string.lbl_choose_country_code),
                    ) { showPicker = true }
                    .heightIn(min = 48.dp)
                    .padding(start = AkilimoSpacing.xs)
                    .clearAndSetSemantics { contentDescription = buttonDescription },
            ) {
                CountryFlag(region)
                Text(
                    text = "+$dialCode",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = AkilimoSpacing.xs),
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
        },
    )

    if (showPicker) {
        CountryCodePickerSheet(
            countries = countries,
            suggestedRegions = suggestedRegions,
            selectedRegion = region,
            onSelect = {
                onRegionChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

/** Bottom sheet listing every country, searchable by name, "+code" or ISO code. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryCodePickerSheet(
    countries: List<PhoneCountry>,
    suggestedRegions: List<String>,
    selectedRegion: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    // Names follow the app language, so they are sorted and searched as the user reads them
    val locale = Locale.getDefault()
    val named = remember(countries, locale) {
        countries
            .map { it to Locale("", it.region).getDisplayCountry(locale) }
            .sortedBy { it.second.lowercase(locale) }
    }
    val byRegion = remember(named) { named.associateBy { it.first.region } }
    val suggested = remember(byRegion, suggestedRegions) { suggestedRegions.mapNotNull { byRegion[it] } }
    val matches = remember(named, query) {
        val q = query.trim().removePrefix("+").lowercase(locale)
        if (q.isEmpty()) {
            named
        } else {
            named.filter { (country, name) ->
                name.lowercase(locale).contains(q) ||
                    country.dialCode.toString().startsWith(q) ||
                    country.region.lowercase(Locale.ROOT) == q
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(modifier = Modifier.imePadding()) {
            Text(
                text = stringResource(R.string.lbl_choose_country_code),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(horizontal = AkilimoSpacing.md)
                    .semantics { heading() },
            )
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.lbl_search_country_code)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AkilimoSpacing.md),
            )
            LazyColumn {
                if (query.isBlank() && suggested.isNotEmpty()) {
                    item(key = "suggested-header") { ListHeader(stringResource(R.string.lbl_suggested_countries)) }
                    items(suggested, key = { "s-" + it.first.region }) { (country, name) ->
                        CountryRow(country, name, country.region == selectedRegion) { onSelect(country.region) }
                    }
                    item(key = "divider") { HorizontalDivider(Modifier.padding(vertical = AkilimoSpacing.xs)) }
                    item(key = "all-header") { ListHeader(stringResource(R.string.lbl_all_countries)) }
                }
                if (matches.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(R.string.lbl_no_country_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(AkilimoSpacing.md),
                        )
                    }
                }
                items(matches, key = { it.first.region }) { (country, name) ->
                    CountryRow(country, name, country.region == selectedRegion) { onSelect(country.region) }
                }
            }
        }
    }
}

@Composable
private fun ListHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(horizontal = AkilimoSpacing.md, vertical = AkilimoSpacing.xs)
            .semantics { heading() },
    )
}

@Composable
private fun CountryRow(country: PhoneCountry, name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AkilimoSpacing.sm),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = AkilimoSpacing.md),
    ) {
        CountryFlag(country.region)
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "+${country.dialCode}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** Flag image for an ISO region (World falls back to a globe for unknown codes); decorative. */
@Composable
private fun CountryFlag(region: String) {
    Image(
        painter = painterResource(World.getFlagOf(region)),
        contentDescription = null,
        modifier = Modifier
            .size(width = 28.dp, height = 20.dp)
            .clip(MaterialTheme.shapes.extraSmall),
    )
}

/** Country name in the app language, e.g. "TZ" → "Tanzania" */
private fun regionName(region: String): String = Locale("", region).displayCountry
