# AKILIMO Mobile — 3-Day UI Redesign Plan

**Version:** 2.0 (Merged)
**Last Updated:** 2026-03-23
**Status:** Ready for Implementation
**Author:** Senior Android Product Designer

---

## Executive Summary

This document outlines a focused **3-day UI redesign sprint** for the AKILIMO cassava recommendation mobile app. The redesign improves visual hierarchy, component consistency, and usability while preserving all existing functionality and business logic.

**Target Users:** Farmers and field agents in Tanzania and Nigeria
**Design Philosophy:** Calm, organized, trustworthy, outdoor-readable
**Constraint:** minSdkVersion 21, no business logic changes

---

## Purpose & Objectives

### Redesign Goals

1. **Make the app scannable in 2–3 seconds**
   Users should immediately understand where they are, what to do next, what's selected, and how to proceed.

2. **Improve confidence in selections**
   Every selected item must be visually unmistakable through color, border, icon/indicator, and clearer supporting text.

3. **Reduce visual clutter without reducing information**
   The app should feel calmer and more professional through grouped content, shortened text, clear actions, and consistent spacing.

4. **Preserve task completion flows**
   All redesign work must maintain existing recommendation workflows, navigation paths, and validation rules.

### Non-Negotiable Constraints

- **Do NOT change business logic, APIs, repositories, or ViewModels**
- **Do NOT remove or alter features**
- **Preserve all user flows and navigation paths**
- **Maintain compatibility with minSdkVersion 21**
- **Keep UI lightweight for low-end Android devices**

---

## Current UI Foundation

The app already has a strong Compose base to build on:

- `AkilimoTheme` wraps Material 3 with centralized color, typography, and shapes
- Nature-forward palette with strong green tones and high-contrast light/dark schemes
- Reusable building blocks: `SelectionCard`, `BackTopAppBar`, `SaveBottomBar`, `BinaryToggleChips`, `RadioButtonRow`
- Many workflows already use `LazyColumn`, `Scaffold`, and persistent bottom actions

**This redesign enhances rather than replaces existing patterns.**

---

## Design System Specifications

### Color System

Use the current AKILIMO palette systematically with refined contrast for outdoor readability.

#### Light Theme Palette

| Role | Current | Proposed | Rationale |
|------|---------|----------|-----------|
| Primary | `#3D7600` | `#2E5900` | Deepened for sunlight contrast |
| On Primary | `#FFFFFF` | `#FFFFFF` | Keep |
| Primary Container | `#BAEE82` | `#C8E68A` | Warmer, less neon |
| On Primary Container | `#0D2100` | `#0E2400` | Match deeper primary |
| Secondary | `#586200` | `#4A5200` | Muted olive-earth |
| Secondary Container | `#DCE87A` | `#D4E175` | Less saturated |
| Background | `#F5FCE9` | `#F8FBF4` | Softer green-white |
| On Background | `#181D12` | `#1C1F16` | Deeper charcoal-green |
| Surface | `#F5FCE9` | `#F8FBF4` | Match background |
| On Surface | `#181D12` | `#1C1F16` | Match |
| Surface Variant | `#DCE6C8` | `#E3E8D8` | Neutral gray-green |
| Outline | `#72796A` | `#6B7265` | Warmer gray |

**Dark Theme:** Adjust surface to `#141811`, primary to `#91C660`.

**Semantic Colors (Unchanged):** Success `#2D6A13`, Warning `#7B5800`, Info `#0062A1`, Error `#BA1A1A`.

### Typography System

Tighten the existing Material 3 scale for better readability and hierarchy:

```kotlin
val AkilimoTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,  // was Normal
        fontSize = 28.sp,                // was 32sp
        lineHeight = 36.sp,              // was 40sp
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,                // was 28sp
        lineHeight = 32.sp,              // was 36sp
    ),
    headlineSmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,                // was 24sp
        lineHeight = 28.sp,              // was 32sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,              // was 24sp
        letterSpacing = 0.3.sp,          // was 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,                // was 14sp
        lineHeight = 22.sp,              // was 20sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.SemiBold, // was Medium
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
```

### Spacing System

Introduce strict 8dp grid for consistency:

```kotlin
object AkilimoSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}
```

**Usage:** 8dp between closely related elements, 16dp section padding, 24dp between major sections.

### Shapes (Unchanged)

Current shape system is solid:
- `extraSmall = 4.dp` (chips)
- `small = 8.dp` (text fields)
- `medium = 12.dp` (cards)
- `large = 24.dp` (bottom sheets)
- `extraLarge = 28.dp` (dialogs)

---

## Component Library Specifications

### 1. SelectionCard — Enhanced Selection States

**Current Issues:** Only background color change, no clear visual indicator

**Proposed Implementation:**

```kotlin
@Composable
fun SelectionCard(
    imageRes: Int,
    title: String,
    subtitle: String? = null,
    description: String? = null,
    isSelected: Boolean,
    isGridLayout: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageColorFilter: ColorFilter? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            CardDefaults.outlinedCardBorder()
                .copy(width = 2.dp, color = MaterialTheme.colorScheme.primary)
        else
            CardDefaults.cardBorder().copy(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box {
            // Existing grid/list content...
            if (isSelected) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
```

**Visual States:**
- Unselected: light surface, subtle outline
- Selected: primary container background, 2dp primary border, checkmark badge
- Disabled: grayed out, no interaction

---

### 2. BinaryToggleChips — Modern SegmentedButton

**Current Issues:** Uses deprecated FilterChip, no visual connection between options

**Proposed Implementation:**

```kotlin
@Composable
fun BinaryToggleChips(
    labelA: String,
    labelB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(2.dp)
    ) {
        SegmentedButton(
            selected = selectedA,
            onClick = onSelectA,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.weight(1f),
            colors = SegmentedButtonDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedContainerColor = Color.Transparent,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        ) { Text(labelA) }

        SegmentedButton(
            selected = !selectedA,
            onClick = onSelectB,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.weight(1f),
            colors = SegmentedButtonDefaults.colors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedContainerColor = Color.Transparent,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        ) { Text(labelB) }
    }
}
```

---

### 3. RadioButtonRow — Enhanced Touch Target

**Current Issues:** Small touch area, no hover/pressed feedback

**Proposed Implementation:**

```kotlin
@Composable
fun RadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .minHeight(56.dp),  // Minimum 48dp touch target
        shape = MaterialTheme.shapes.small,
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        border = if (selected)
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = if (enabled) onClick else null,
                modifier = Modifier.size(24.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
```

---

### 4. AkilimoTextField — Better Forms

**Current Issues:** Default OutlinedBox, no helper text support

**Proposed Implementation:**

```kotlin
@Composable
fun AkilimoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.outline,
            ),
            keyboardOptions = keyboardOptions,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            shape = MaterialTheme.shapes.small,
        )

        Spacer(Modifier.height(4.dp))
        Text(
            text = errorMessage ?: helperText ?: "",
            style = MaterialTheme.typography.labelMedium,
            color = if (errorMessage != null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
```

---

### 5. BackTopAppBar — Cleaner Navigation

**Enhanced with subtitle support:**

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopAppBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.lbl_back),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        actions = actions,
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        )
    )
}
```

---

### 6. SaveBottomBar / WizardBottomBar — Primary Action Clarity

**Enhanced with loading state:**

```kotlin
@Composable
fun SaveBottomBar(
    label: String,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (enabled)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 16.sp
            )
        }
    }
}
```

---

### 7. New: SectionHeader Composable

**Purpose:** Consistent section titles throughout the app

```kotlin
@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        subtitle?.let {
            Spacer(Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

---

### 8. New: InfoCard Composable

**Purpose:** Display guidance, tips, and important notices

```kotlin
@Composable
fun InfoCard(
    title: String,
    message: String,
    icon: ImageVector = Icons.Outlined.Info,
    modifier: Modifier = Modifier,
    type: InfoCardType = InfoCardType.Info
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (type) {
                InfoCardType.Info -> MaterialTheme.colorScheme.infoContainer
                InfoCardType.Warning -> MaterialTheme.colorScheme.warningContainer
                InfoCardType.Error -> MaterialTheme.colorScheme.errorContainer
                InfoCardType.Success -> MaterialTheme.colorScheme.successContainer
            }
        ),
        border = BorderStroke(
            1.dp,
            when (type) {
                InfoCardType.Info -> MaterialTheme.colorScheme.info
                InfoCardType.Warning -> MaterialTheme.colorScheme.warning
                InfoCardType.Error -> MaterialTheme.colorScheme.error
                InfoCardType.Success -> MaterialTheme.colorScheme.success
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = when (type) {
                    InfoCardType.Info -> MaterialTheme.colorScheme.info
                    InfoCardType.Warning -> MaterialTheme.colorScheme.warning
                    InfoCardType.Error -> MaterialTheme.colorScheme.error
                    InfoCardType.Success -> MaterialTheme.colorScheme.success
                }
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

enum class InfoCardType { Info, Warning, Error, Success }
```

---

### 9. New: ResultCard Composable

**Purpose:** Display recommendation results with clear hierarchy

```kotlin
@Composable
fun ResultCard(
    title: String,
    value: String,
    unit: String? = null,
    subtitle: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (highlight) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (highlight)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (highlight)
                            MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    unit?.let {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                subtitle?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

---

## Screen Redesign Specifications

### Screen 1: WelcomeStep (Onboarding Entry)

**Proposed Implementation:**
```kotlin
@Composable
fun WelcomeStep(
    languageCode: String,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locales = Locales.supportedLocales
    val selectedLocale = locales.find { it.toLanguageTag() == languageCode } ?: Loc
