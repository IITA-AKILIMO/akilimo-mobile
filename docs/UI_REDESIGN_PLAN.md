# Akilimo Mobile UI Redesign Plan

## Overview

A 3-day phased redesign of the Akilimo cassava recommendation app to achieve a modern, clean, professional UI while preserving all existing functionality.

**Target Users:** Farmers and field agents in Tanzania and Nigeria
**Design Philosophy:** Calm, organized, trustworthy, outdoor-readable
**Constraint:** minSdkVersion 21, no business logic changes

---

## Design System Foundations

### Color Palette (Enhanced)

Current palette is solid but needs refinement for better contrast and outdoor visibility.

#### Light Theme

| Role | Current | Proposed | Rationale |
|------|---------|----------|-----------|
| Primary | `#3D7600` | `#2E5900` | Deepened for better contrast in sunlight |
| Primary Container | `#BAEE82` | `#C8E68A` | Slightly warmer, less neon |
| Secondary | `#586200` | `#4A5200` | Muted olive-earth tone |
| Surface | `#F5FCE9` | `#F8FBF4` | Softer green-white, less saturated |
| Surface Variant | `#DCE6C8` | `#E3E8D8` | Neutral warmer gray-green |
| On Surface | `#181D12` | `#1C1F16` | Slightly deeper for contrast |
| Outline | `#72796A` | `#6B7265` | Warmer gray |

#### Dark Theme

| Role | Current | Proposed | Rationale |
|------|---------|----------|-----------|
| Surface | `#10140C` | `#141811` | Slightly lifted, less muddy |
| Primary | `#9DD768` | `#91C660` | More muted, less vibrant |
| On Surface | `#E1E8D4` | `#E6ECD8` | Warmer off-white |

#### Semantic Colors (Unchanged)

- Success: `#2D6A13` (keep)
- Warning: `#7B5800` (keep)
- Info: `#0062A1` (keep)
- Error: `#BA1A1A` (keep)

### Typography (Refined)

Current Google Sans setup is excellent. Minor adjustments:

```kotlin
val AkilimoTypography = Typography(
    // Headlines - slightly tighter tracking
    headlineLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,  // Normal → Medium
        fontSize = 28.sp,  // 32 → 28
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,  // 28 → 24
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,  // 24 → 20
        lineHeight = 28.sp,
    ),
    // Body - increased line height for readability
    bodyLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,  // 24 → 26
        letterSpacing = 0.3.sp,  // 0.5 → 0.3
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,  // 14 → 15
        lineHeight = 22.sp,  // 20 → 22
    ),
    // Labels - bolder for action clarity
    labelLarge = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSans,
        fontWeight = FontWeight.SemiBold,  // Medium → SemiBold
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
```

### Shapes (Unchanged)

Current shape system is solid:
- `extraSmall = 4.dp` (chips)
- `small = 8.dp` (text fields)
- `medium = 12.dp` (cards)
- `large = 24.dp` (bottom sheets)
- `extraLarge = 28.dp` (dialogs)

### Spacing System (New)

Introduce consistent 8dp grid:

```kotlin
object AkilimoSpacing {
    val xxxs = 2.dp
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

---

## Component Redesign Specifications

### 1. SelectionCard (Enhanced Selection States)

**Current Issues:**
- Selected state only changes background color
- No clear visual indicator (checkmark, border)
- Grid/list layouts have inconsistent padding

**Proposed Changes:**

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
        // Content with overlay checkmark badge
        Box {
            // existing content...

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

### 2. BinaryToggleChips (Modern Segmented Button)

**Current Issues:**
- Uses FilterChip (deprecated in newer M3)
- No clear visual connection between options
- Limited styling

**Proposed Changes:**

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
        ) {
            Text(labelA)
        }
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
        ) {
            Text(labelB)
        }
    }
}
```

---

### 3. RadioButtonRow (Enhanced Touch Target)

**Current Issues:**
- Small touch area
- No hover/pressed state feedback

**Proposed Changes:**

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
            .padding(vertical = 4.dp),
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
                .padding(16.dp)
                .minHeight(56.dp),  // Minimum touch target
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

### 4. AkilimoTextField (Better Forms)

**Current Issues:**
- Default OutlinedBox styling
- No helper text support
- Error state could be clearer

**Proposed Changes:**

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
                focusedLabelColor = if (errorMessage != null)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
            ),
            keyboardOptions = keyboardOptions,
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            shape = MaterialTheme.shapes.small,
        )

        // Helper / error text
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

### 5. BackTopAppBar (Cleaner Navigation)

**Current Issues:**
- Basic TopAppBar with default styling
- No elevation control

**Proposed Changes:**

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
                modifier = Modifier.size(48.dp)  // Consistent touch target
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
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        )
    )
}
```

---

### 6. SaveBottomBar / WizardBottomBar (Primary Action Clarity)

**Current Issues:**
- Simple button with shadow elevation
- No loading state support
- Not distinctive enough

**Proposed Changes:**

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
                .height(56.dp),  // Prominent primary action
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
                fontSize = 16.sp  // Slightly larger for prominence
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

### 9. New: FormField Composable

**Purpose:** Unified form input with label, input, and helper text

```kotlin
@Composable
fun FormField(
    label: String,
    helperText: String? = null,
    errorMessage: String? = null,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (errorMessage != null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        content()
        Spacer(Modifier.height(4.dp))
        Text(
            text = errorMessage ?: helperText ?: "",
            style = MaterialTheme.typography.labelMedium,
            color = if (errorMessage != null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

---

### 10. New: ResultCard Composable

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
                            text = unit,
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

**Current:** Centered logo, welcome text, two buttons

**Proposed:**
```kotlin
@Composable
fun WelcomeStep(
    languageCode: String,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locales = Locales.supportedLocales
    val selectedLocale = locales.find { it.toLanguageTag() == languageCode } ?: Locales.english

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        // Logo
        Image(
            painter = painterResource(R.drawable.logo_akilimo_white_on_green),
            contentDescription = "Akilimo Logo",
            modifier = Modifier.size(100.dp)
        )

        // Title
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Description
        Text(
            text = stringResource(R.string.welcome_instructions),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        // Language selection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.lbl_select_language),
                    style = MaterialTheme.typography.titleSmall
                )
                AkilimoDropdown(
                    label = stringResource(R.string.lbl_language),
                    options = locales,
                    selectedOption = selectedLocale,
                    onOptionSelected = { locale ->
                        val tag = locale.toLanguageTag()
                        onEvent(OnboardingViewModel.Event.LanguageSelected(tag))
                    },
                    displayText = { locale -> locale.getDisplayLanguage(locale) }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Primary action
        Button(
            onClick = { onEvent(OnboardingViewModel.Event.NextClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = stringResource(R.string.lbl_continue_),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
```

**Improvements:**
- Better vertical rhythm with `spacedBy`
- Language selection in card (clearer grouping)
- Primary button at bottom (natural thumb position)
- Cleaner visual hierarchy

---

### Screen 2: RecommendationsScreen (Home Menu)

**Current:** Simple list of cards with arrows

**Proposed:**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<RecommendationsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                subtitle = stringResource(R.string.lbl_select_advice_type)
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
        ) {
            item {
                SectionHeader(
                    title = stringResource(R.string.lbl_cassava_advice),
                    subtitle = stringResource(R.string.lbl_choose_recommendation_type)
                )
            }

            items(state.adviceOptions) { option ->
                AdviceOptionCard(
                    option = option,
                    onClick = {
                        viewModel.trackActiveAdvice(option.valueOption)
                        val route: Any = when (option.valueOption) {
                            EnumAdvice.FERTILIZER_RECOMMENDATIONS -> FrRoute
                            EnumAdvice.BEST_PLANTING_PRACTICES -> BppRoute
                            EnumAdvice.SCHEDULED_PLANTING_HIGH_STARCH -> SphRoute
                            EnumAdvice.INTERCROPPING_MAIZE -> IcMaizeRoute
                            EnumAdvice.INTERCROPPING_SWEET_POTATO -> IcSweetPotatoRoute
                        }
                        navController.navigate(route)
                    }
                )
            }
        }
    }
}

@Composable
private fun AdviceOptionCard(option: AdviceOption, onClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .animateItemPlacement(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = option.valueOption.label(context),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**Improvements:**
- Icon container for visual anchor
- Consistent spacing with `spacedBy`
- AnimateItemPlacement for smooth transitions
- Clearer touch targets

---

### Screen 3: FertilizerScreen (Selection Flow)

**Current:** Grid/list toggle with SelectionCard

**Proposed:** Keep structure, enhance SelectionCard (see component spec above)

**Additional Improvements:**
```kotlin
// Add layout toggle as SegmentedButton in top app bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FertilizerScreen(
    fertilizerFlow: EnumFertilizerFlow,
    adviceTask: EnumAdviceTask,
    navController: NavHostController,
) {
    val viewModel = hiltViewModel<FertilizerViewModel, FertilizerViewModel.Factory> { factory ->
        factory.create(fertilizerFlow)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // ... existing modal sheet logic ...

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_available_fertilizers),
                subtitle = stringResource(R.string.lbl_select_available_fertilizers_subtitle),
                onBack = { navController.popBackStack() },
                actions = {
                    // Layout toggle as segmented button
                    Row(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.shapes.extraSmall
                            )
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.toggleLayout() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (state.isGridLayout)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        Color.Transparent,
                                    MaterialTheme.shapes.extraSmall
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_grid),
                                contentDescription = "Grid layout",
                                tint = if (state.isGridLayout)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { viewModel.toggleLayout() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    if (!state.isGridLayout)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        Color.Transparent,
                                    MaterialTheme.shapes.extraSmall
                                )
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_list),
                                contentDescription = "List layout",
                                tint = if (!state.isGridLayout)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = stringResource(R.string.lbl_finish),
                enabled = state.selectedIds.isNotEmpty(),
                onClick = { navController.completeTask(adviceTask) }
            )
        }
    ) { padding ->
        // Grid/List content with new SelectionCard
        // ...
    }
}
```

---

### Screen 4: GetRecommendationScreen (Results Display)

**Current:** Basic text display

**Proposed:** Card-based result cards with clear hierarchy

```kotlin
@Composable
fun GetRecommendationScreen(useCase: EnumUseCase, navController: NavHostController) {
    val viewModel = hiltViewModel<GetRecommendationViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // ... existing state handling ...

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_recommendations),
                onBack = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            if (state is GetRecommendationViewModel.UiState.Success) {
                FloatingActionButton(
                    onClick = { showFeedback = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.StarRate, contentDescription = "Feedback")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val s = state) {
                is GetRecommendationViewModel.UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is GetRecommendationViewModel.UiState.Success -> {
                    LazyColumn(
                        contentPadding = Modifier.padding(16.dp).padding(padding),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            // Title card (highlighted)
                            ResultCard(
                                title = s.title,
                                value = when (s.title) {
                                    "FR" -> stringResource(R.string.lbl_fertilizer_rec)
                                    "IC" -> stringResource(R.string.lbl_intercrop_rec)
                                    "PP" -> stringResource(R.string.lbl_planting_practices_rec)
                                    "SP" -> stringResource(R.string.lbl_scheduled_planting_rec)
                                    else -> s.title
                                },
                                highlight = true
                            )
                        }
                        item {
                            // Description card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.lbl_recommendation_details),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = s.description,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                is GetRecommendationViewModel.UiState.Error -> {
                    // Error card
                    InfoCard(
                        title = stringResource(R.string.lbl_error),
                        message = s.message,
                        type = InfoCardType.Error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchRecommendation(useCase, noRecsLabel, errorLabel) },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(stringResource(R.string.lbl_retry))
                    }
                }
                else -> Unit
            }
        }
    }
}
```

---

### Screen 5: TillageStep (Complex Form)

**Current:** Cards with checkboxes and dropdowns

**Proposed:** Better section grouping, clearer selection states

```kotlin
@Composable
fun TillageStep(
    tillageOperations: Map<EnumOperationType, EnumOperationMethod>,
    weedControlEnabled: Boolean,
    weedControlMethod: EnumWeedControlMethod?,
    errors: Map<String, String>,
    onEvent: (OnboardingViewModel.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val tillageTypes = remember { listOf(EnumOperationType.PLOUGHING, EnumOperationType.RIDGING) }
    val methods = remember { EnumOperationMethod.entries }
    val weedMethods = remember { EnumWeedControlMethod.entries }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        SectionHeader(
            title = stringResource(R.string.lbl_tillage_operations),
            subtitle = stringResource(R.string.lbl_select_tillage_operations_subtitle)
        )

        // Tillage operations
        tillageTypes.forEach { type ->
            val checked = type in tillageOperations
            val selectedMethod = tillageOperations[type]

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (checked)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                ),
                border = if (checked)
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                else
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onEvent(OnboardingViewModel.Event.TillageOperationToggled(type, it)) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = type.label(context),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    AnimatedVisibility(
                        visible = checked,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Divider()
                            AkilimoDropdown(
                                label = stringResource(R.string.lbl_select_tillage_method),
                                options = methods,
                                selectedOption = selectedMethod,
                                onOptionSelected = { onEvent(OnboardingViewModel.Event.TillageMethodSelected(type, it)) },
                                displayText = { it.label(context) }
                            )
                        }
                    }
                }
            }
        }

        // Weed control section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (weedControlEnabled)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = weedControlEnabled,
                        onCheckedChange = { onEvent(OnboardingViewModel.Event.WeedControlToggled(it)) }
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = EnumOperationType.WEEDING.label(context),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                AnimatedVisibility(
                    visible = weedControlEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Divider()
                        AkilimoDropdown(
                            label = stringResource(R.string.lbl_select_weed_control_method),
                            options = weedMethods,
                            selectedOption = weedControlMethod,
                            onOptionSelected = { onEvent(OnboardingViewModel.Event.WeedControlMethodSelected(it)) },
                            displayText = { it.label(context) },
                            error = errors["weedControl"]
                        )
                    }
                }
            }
        }
    }
}
```

---

### Screen 6: UserSettingsScreen

**Current:** Basic settings list

**Proposed:** Grouped settings with clear sections

```kotlin
@Composable
fun UserSettingsScreen(navController: NavHostController) {
    val viewModel = hiltViewModel<UserSettingsViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_settings),
                onBack = { navController.popBackStack() }
            )
        },
        bottomBar = {
            SaveBottomBar(
                label = stringResource(R.string.lbl_save),
                onClick = { /* save logic */ }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Profile section
                SectionHeader(
                    title = stringResource(R.string.lbl_profile),
                    subtitle = stringResource(R.string.lbl_profile_subtitle)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile fields
                        FormField(
                            label = stringResource(R.string.lbl_first_name)
                        ) {
                            AkilimoTextField(
                                value = state.firstName,
                                onValueChange = { viewModel.updateFirstName(it) },
                                label = stringResource(R.string.lbl_first_name)
                            )
                        }
                        // ... more fields
                    }
                }
            }

            item {
                // Preferences section
                SectionHeader(
                    title = stringResource(R.string.lbl_preferences),
                    subtitle = stringResource(R.string.lbl_preferences_subtitle)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Language
                        FormField(
                            label = stringResource(R.string.lbl_language)
                        ) {
                            AkilimoDropdown(
                                label = stringResource(R.string.lbl_select_language),
                                options = Locales.supportedLocales,
                                selectedOption = state.languageLocale,
                                onOptionSelected = { viewModel.updateLanguage(it) }
                            )
                        }
                        // Area unit
                        BinaryToggleChips(
                            labelA = stringResource(R.string.area_unit_acre),
                            labelB = stringResource(R.string.area_unit_ha),
                            selectedA = state.areaUnit == AreaUnit.ACRE,
                            onSelectA = { viewModel.updateAreaUnit(AreaUnit.ACRE) },
                            onSelectB = { viewModel.updateAreaUnit(AreaUnit.HA) }
                        )
                    }
                }
            }

            item {
                // Account section
                SectionHeader(
                    title = stringResource(R.string.lbl_account),
                    subtitle = stringResource(R.string.lbl_account_subtitle)
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Account settings
                }
            }

            item {
                Spacer(Modifier.height(80.dp)) // Bottom bar clearance
            }
        }
    }
}
```

---

## Implementation Phases

### Day 1: Foundation & Core Components

**Morning (3 hours): Design System Setup**
1. Update `AkilimoColors.kt` with proposed palette refinements
2. Update `AkilimoTypography.kt` with adjusted type scale
3. Create `AkilimoSpacing.kt` object for 8dp grid system
4. Verify dark theme colors

**Afternoon (4 hours): Core Component Refactors**
1. `SelectionCard` - enhanced border + checkmark badge
2. `BinaryToggleChips` - SegmentedButton implementation
3. `RadioButtonRow` - enhanced touch target + surface wrapper
4. `AkilimoTextField` - helper text + error state support
5. Run static analysis to verify no regressions

**Deliverable:** All core components updated, backward compatible

---

### Day 2: New Components & Screen Refactors

**Morning (3 hours): New Component Library**
1. `SectionHeader` composable
2. `InfoCard` composable with InfoCardType enum
3. `FormField` composable
4. `ResultCard` composable
5. Update `BackTopAppBar` with subtitle support

**Afternoon (4 hours): Screen Refactors**
1. `WelcomeStep` - cleaner layout
2. `RecommendationsScreen` - enhanced cards
3. `SaveBottomBar` - loading state support
4. `WizardBottomBar` - consistent styling

**Deliverable:** New component library, 4 screens refactored

---

### Day 3: Complex Screens & Polish

**Morning (4 hours): Complex Screen Refactors**
1. `FertilizerScreen` - enhanced SelectionCard integration
2. `TillageStep` - animated expansion, clearer states
3. `GetRecommendationScreen` - ResultCard implementation
4. `UserSettingsScreen` - grouped sections

**Afternoon (3 hours): Polish & Testing**
1. Run `./gradlew :app:compileDebugKotlin` - verify build
2. Run `./gradlew testDebugUnitTest` - verify tests
3. Manual testing checklist:
   - Selection states clearly visible
   - All touch targets ≥48dp
   - Text contrast passes WCAG AA
   - Dark theme renders correctly
   - Animations smooth (no jank)
4. Create before/after screenshots for documentation

**Deliverable:** All screens refactored, tested, documented

---

## Accessibility Checklist

### Touch Targets
- [ ] All buttons minimum 48dp height
- [ ] All icon buttons minimum 48dp size
- [ ] Checkbox/radio rows minimum 56dp height
- [ ] List items minimum 48dp touch area

### Color Contrast
- [ ] Primary text on background: ≥ 4.5:1
- [ ] Secondary text on background: ≥ 3:1
- [ ] Icon on container: ≥ 3:1
- [ ] Error states clearly distinguishable

### Content Scale
- [ ] Test on 5-inch screen (minimum target)
- [ ] Test on 7-inch screen (tablet)
- [ ] Font sizes scale with system settings
- [ ] No hardcoded dp values for text

---

## Performance Guidelines

### Do:
- Use `derivedStateOf` for expensive computations
- Use `rememberScrollState` over `LazyColumn` when list is short (<10 items)
- Use `animateItemPlacement()` for LazyColumn items
- Use `LaunchedEffect` for side effects
- Profile with Android Studio Compose Launcher

### Don't:
- Overuse elevation (shadowElevation > 8.dp)
- Nest LazyColumn inside LazyColumn
- Use `AnimatedVisibility` in hot paths
- Create new modifier instances per recomposition
- Use `Spacer(Modifier.height(x.dp))` - prefer `Arrangement.spacedBy`

---

## Testing Strategy

### Unit Tests (Unchanged)
- ViewModels remain unchanged per constraints
- Existing tests should pass

### UI Tests (New)
```kotlin
@RunWith(AndroidJUnit4::class)
class SelectionCardTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun selectionCard_selectedState_showsCheckmarkBadge() {
        composeTestRule.setContent {
            SelectionCard(
                imageRes = R.drawable.ic_fertilizer_bag,
                title = "Test",
                isSelected = true,
                isGridLayout = true,
                onClick = {}
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Selected")
            .assertExists()
    }

    @Test
    fun selectionCard_selectedState_hasPrimaryBorder() {
        // Assert border color via semantics
    }
}
```

---

## Success Metrics

### Qualitative
- [ ] Visual consistency across all screens
- [ ] Clear hierarchy (titles, sections, actions)
- [ ] Obvious selection states
- [ ] Reduced visual clutter

### Quantitative (Post-launch)
- Target: <2 seconds to identify next action
- Target: >90% task completion rate
- Target: <3% mis-tap rate on selection cards
- Target: User satisfaction score >4.2/5

---

## Files to Modify

### Core Theme
- `ui/theme/AkilimoColors.kt`
- `ui/theme/AkilimoTypography.kt`
- `ui/theme/AkilimoShapes.kt` (unchanged)
- `ui/theme/AkilimoTheme.kt` (unchanged)

### Components (Modified)
- `ui/components/compose/SelectionCard.kt`
- `ui/components/compose/BinaryToggleChips.kt`
- `ui/components/compose/RadioButtonRow.kt`
- `ui/components/compose/AkilimoTextField.kt`
- `ui/components/compose/BackTopAppBar.kt`
- `ui/components/compose/SaveBottomBar.kt`
- `ui/components/compose/WizardBottomBar.kt`

### Components (New)
- `ui/components/compose/SectionHeader.kt`
- `ui/components/compose/InfoCard.kt`
- `ui/components/compose/FormField.kt`
- `ui/components/compose/ResultCard.kt`
- `ui/components/compose/AkilimoSpacing.kt`

### Screens (Modified)
- `ui/screens/onboarding/steps/WelcomeStep.kt`
- `ui/screens/onboarding/steps/TillageStep.kt`
- `ui/screens/recommendations/RecommendationsScreen.kt`
- `ui/screens/recommendations/GetRecommendationScreen.kt`
- `ui/screens/usecases/FertilizerScreen.kt`
- `ui/screens/settings/UserSettingsScreen.kt`

---

## Risk Mitigation

### Technical Risks
| Risk | Mitigation |
|------|------------|
| minSdk 21 compatibility | Avoid Material 3 Expressive features |
| Performance regression | Profile with Compose compiler metrics |
| Test breakage | Run full test suite after each phase |
| Dark theme issues | Test both themes in parallel |

### Product Risks
| Risk | Mitigation |
|------|------------|
| User confusion | Maintain consistent interaction patterns |
| Accessibility loss | Run accessibility scanner after Day 2 |
| Feature removal | Audit all interactions before refactoring |

---

## Post-Implementation Review

### Questions to Answer
1. Did selection states improve clarity?
2. Is outdoor readability better?
3. Are farmers completing flows faster?
4. Did we reduce mis-taps?
5. Is code more maintainable?

### Follow-up Tasks
- [ ] Create visual regression test suite
- [ ] Document component usage patterns
- [ ] Add Compose previews for all components
- [ ] Write migration guide for future developers
- [ ] Schedule user testing session

---

## Appendix: Component Preview Checklist

Each component should have `@Preview` annotations:

```kotlin
@Preview(showBackground = true, backgroundColor = 0xFFF5FCE9)
@Composable
fun SelectionCardPreview() {
    SelectionCard(
        imageRes = R.drawable.ic_fertilizer_bag,
        title = "NPK 17-17-17",
        subtitle = "50 kg bag",
        isSelected = true,
        isGridLayout = true,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF10140C, darkTheme = true)
@Composable
fun SelectionCardDarkPreview() {
    SelectionCard(
        imageRes = R.drawable.ic_fertilizer_bag,
        title = "NPK 17-17-17",
        subtitle = "50 kg bag",
        isSelected = false,
        isGridLayout = true,
        onClick = {}
    )
}
```

---

**Document Version:** 1.0
**Last Updated:** 2026-03-23
**Author:** Senior Android Product Designer
**Status:** Ready for Implementation