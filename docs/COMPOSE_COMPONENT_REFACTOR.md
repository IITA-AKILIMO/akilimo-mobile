# Compose Component Refactoring Plan

**Target:** Extract repeated UI patterns across 30 Compose screens into shared components under `ui/components/compose/`.

**Goal:** Reduce boilerplate, enforce consistency, and make future screen changes single-site edits.

---

## Existing shared components

| File | Purpose |
|---|---|
| `AkilimoTextField.kt` | Styled `OutlinedTextField` wrapper |
| `AkilimoDropdown.kt` | Styled dropdown selector |
| `DateInputField.kt` | Date picker input field |
| `ExitConfirmDialog.kt` | Exit confirmation dialog |
| `WizardBottomBar.kt` | Back/Next bar for onboarding wizard |
| `SelectionCard.kt` | Image + text card for grid and list layouts |

---

## Phase 1 — Structural boilerplate (highest impact)

These are pure layout patterns with no logic. Safe to extract in any order.

---

### 1.1 `BackTopAppBar`

**Affected screens:** 18
**File:** `ui/components/compose/BackTopAppBar.kt`

**Pattern being replaced:**
```kotlin
TopAppBar(
    title = { Text(stringResource(R.string.lbl_something)) },
    navigationIcon = {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.lbl_back)
            )
        }
    }
)
```

**Proposed API:**
```kotlin
@Composable
fun BackTopAppBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
)
```

**Screens to update:**
- `usecases/DatesScreen.kt`
- `usecases/FertilizerScreen.kt`
- `usecases/InvestmentAmountScreen.kt`
- `usecases/MaizeMarketScreen.kt`
- `usecases/MaizePerformanceScreen.kt`
- `usecases/CassavaYieldScreen.kt`
- `usecases/CassavaMarketScreen.kt`
- `usecases/SweetPotatoMarketScreen.kt`
- `usecases/TractorAccessScreen.kt`
- `usecases/WeedControlCostsScreen.kt`
- `usecases/ManualTillageCostScreen.kt`
- `recommendations/GetRecommendationScreen.kt`
- `recommendations/UseCaseScreen.kt`
- `settings/UserSettingsScreen.kt`
- `settings/LocationPickerScreen.kt`
- *(+ up to 3 additional screens)*

---

### 1.2 `SaveBottomBar`

**Affected screens:** 9
**File:** `ui/components/compose/SaveBottomBar.kt`

**Pattern being replaced:**
```kotlin
bottomBar = {
    Surface(shadowElevation = 4.dp) {
        Button(
            onClick = { /* action */ },
            enabled = someCondition,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(stringResource(R.string.lbl_save))
        }
    }
}
```

**Proposed API:**
```kotlin
@Composable
fun SaveBottomBar(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
)
```

**Screens to update:**
- `usecases/FertilizerScreen.kt`
- `usecases/MaizePerformanceScreen.kt`
- `usecases/CassavaYieldScreen.kt`
- `usecases/InvestmentAmountScreen.kt`
- `usecases/ManualTillageCostScreen.kt`
- `usecases/TractorAccessScreen.kt`
- `usecases/WeedControlCostsScreen.kt`
- `usecases/CassavaMarketScreen.kt`
- `recommendations/UseCaseScreen.kt`

---

### 1.3 `ScrollableFormColumn`

**Affected screens:** 14
**File:** `ui/components/compose/ScrollableFormColumn.kt`

**Pattern being replaced:**
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
) { ... }
```

**Proposed API:**
```kotlin
@Composable
fun ScrollableFormColumn(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(0.dp),
    horizontalPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
)
```

**Screens to update:**
- `usecases/DatesScreen.kt`
- `usecases/MaizeMarketScreen.kt`
- `usecases/SweetPotatoMarketScreen.kt`
- `usecases/TractorAccessScreen.kt`
- `usecases/WeedControlCostsScreen.kt`
- `usecases/ManualTillageCostScreen.kt`
- `usecases/CassavaMarketScreen.kt`
- `recommendations/GetRecommendationScreen.kt`
- `settings/UserSettingsScreen.kt`
- *(+ up to 5 additional screens)*

---

### 1.4 `completeTask` NavController extension

**Affected screens:** 12
**File:** `ui/components/compose/NavExtensions.kt` *(or a `navigation/` util file)*

**Pattern being replaced:**
```kotlin
LaunchedEffect(state.saved) {
    if (state.saved) {
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set(
                "completed_task",
                AdviceCompletionDto(EnumAdviceTask.SOME_TASK, EnumStepStatus.COMPLETED)
            )
        navController.popBackStack()
        viewModel.onSaveHandled()
    }
}
```

**Proposed API:**
```kotlin
fun NavHostController.completeTask(task: EnumAdviceTask) {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set("completed_task", AdviceCompletionDto(task, EnumStepStatus.COMPLETED))
    popBackStack()
}
```

Usage in screens:
```kotlin
LaunchedEffect(state.saved) {
    if (state.saved) {
        navController.completeTask(EnumAdviceTask.SOME_TASK)
        viewModel.onSaveHandled()
    }
}
```

**Screens to update:**
- `usecases/DatesScreen.kt`
- `usecases/FertilizerScreen.kt`
- `usecases/MaizeMarketScreen.kt`
- `usecases/MaizePerformanceScreen.kt`
- `usecases/CassavaYieldScreen.kt`
- `usecases/TractorAccessScreen.kt`
- `usecases/WeedControlCostsScreen.kt`
- `usecases/ManualTillageCostScreen.kt`
- *(+ up to 4 additional screens)*

---

## Phase 2 — Interaction patterns

---

### 2.1 `RadioButtonRow`

**Affected screens:** 5
**File:** `ui/components/compose/RadioButtonRow.kt`

**Pattern being replaced:**
```kotlin
Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
        .fillMaxWidth()
        .clickable { selectedId = item.id }
) {
    RadioButton(
        selected = selectedId == item.id,
        onClick = { selectedId = item.id }
    )
    Text(label, modifier = Modifier.weight(1f))
}
```

**Proposed API:**
```kotlin
@Composable
fun RadioButtonRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Screens to update:**
- `usecases/InvestmentAmountScreen.kt`
- `usecases/CassavaMarketScreen.kt`
- `usecases/FertilizerScreen.kt` (price selection sheet)

---

### 2.2 `BinaryToggleChips`

**Affected screens:** 5
**File:** `ui/components/compose/BinaryToggleChips.kt`

**Pattern being replaced:**
```kotlin
Row {
    FilterChip(
        selected = value,
        onClick = { onSelect(true) },
        label = { Text(labelA) },
        modifier = Modifier.padding(end = 8.dp)
    )
    FilterChip(
        selected = !value,
        onClick = { onSelect(false) },
        label = { Text(labelB) }
    )
}
```

**Proposed API:**
```kotlin
@Composable
fun BinaryToggleChips(
    labelA: String,
    labelB: String,
    selectedA: Boolean,
    onSelectA: () -> Unit,
    onSelectB: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Screens to update:**
- `usecases/TractorAccessScreen.kt` (Yes/No tractor available)
- `usecases/ManualTillageCostScreen.kt` (Ploughing/Ridging)
- `usecases/MaizeMarketScreen.kt`
- *(+ related toggle chip usages)*

---

### 2.3 `SwitchRow` (promote to shared)

**Affected screens:** 3
**File:** `ui/components/compose/SwitchRow.kt`

`UserSettingsScreen.kt` already defines a private `SwitchRow` composable. Move it to shared components.

**Proposed API:**
```kotlin
@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)
```

**Screens to update:**
- `settings/UserSettingsScreen.kt` (remove private, use shared)
- `usecases/DatesScreen.kt`

---

## Phase 3 — Consolidation

---

### 3.1 `FertilizerCard` → use `SelectionCard`

`FertilizerScreen.kt` has its own `FertilizerCard` composable that duplicates the `SelectionCard` grid/list pattern already extracted. Replace it with direct `SelectionCard` usage.

**File to update:** `usecases/FertilizerScreen.kt`

---

### 3.2 `LabeledTextField`

**Affected screens:** 2 (but 6–8 repetitions per screen)
**File:** `ui/components/compose/LabeledTextField.kt`

**Pattern being replaced:**
```kotlin
Text(label, style = MaterialTheme.typography.labelMedium)
AkilimoTextField(value = ..., onValueChange = ..., label = ...)
Spacer(modifier = Modifier.height(8.dp))
```

**Proposed API:**
```kotlin
@Composable
fun LabeledTextField(
    sectionLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
)
```

**Screens to update:**
- `settings/UserSettingsScreen.kt`
- `onboarding/steps/BioDataStep.kt`

---

## Execution checklist

### Phase 1 ✅ Complete
- [x] Create `BackTopAppBar.kt` and update 18 screens
- [x] Create `SaveBottomBar.kt` and update 9 screens
- [x] Create `ScrollableFormColumn.kt` and update 14 screens
- [x] Create `NavExtensions.kt` with `completeTask` and update 12 screens

### Phase 2 ✅ Complete
- [x] Create `RadioButtonRow.kt` and update screens (CassavaMarketScreen, FertilizerScreen)
- [x] Create `BinaryToggleChips.kt` and update screens (TractorAccessScreen Yes/No, MaizeMarketScreen)
- [x] Promote `SwitchRow` to shared and update screens (UserSettingsScreen, DatesScreen)

### Phase 3 ✅ Complete
- [x] Replace `FertilizerCard` with `SelectionCard` in `FertilizerScreen.kt` (extended SelectionCard with optional `imageColorFilter`)
- [x] Create `LabeledTextField.kt` and update `UserSettingsScreen.kt` (5 text fields; BioDataStep uses AkilimoTextField directly without section labels — not updated)

---

## Notes

- All new components go in `app/src/main/java/com/akilimo/mobile/ui/components/compose/`
- Each phase should be a separate commit
- Compile-check (`./gradlew :app:compileDebugKotlin`) after each phase before committing
- No logic changes — these are purely structural extractions
