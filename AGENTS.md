# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build
./gradlew assembleDebug
./gradlew :app:compileDebugKotlin   # fast compile check without full APK

# Tests
./gradlew testDebugUnitTest                                          # all unit tests
./gradlew testDebugUnitTest --tests "com.akilimo.mobile.ui.viewmodels.FertilizerViewModelTest"  # single class
./gradlew testDebugUnitTest --tests "*.FertilizerViewModelTest.getStepStatus*"                  # single test

# Lint / static analysis
./gradlew lintDebug
./gradlew detekt

# Pre-PR check (run before committing)
./gradlew :app:compileDebugKotlin && ./gradlew testDebugUnitTest
```

## Architecture Overview

The app is a **single-Activity Compose app**: `MainActivity` hosts one Compose `NavHost` (`navigation/`). There are no fragments or XML layouts. The only View interop is `AndroidView` for the WebView and the map; keep it that way and write every new screen in Compose.

**Package layout** (under `com.akilimo.mobile`):
- `navigation/` — `Route.kt` (serializable routes), `FeatureGraphs.kt` (graph per feature), `AkilimoNavHost.kt`
- `ui/screens/` — Compose screens grouped by feature (`hub/`, `profile/`, `onboarding/`, `recommendations/`, `usecases/`, `settings/`)
- `ui/viewmodels/` — one ViewModel per screen, pattern: `StateFlow<UiState>` + Hilt `@HiltViewModel`
- `ui/components/compose/` — shared Compose components (see "Design conventions")
- `ui/theme/` — colours, shapes, spacing, typography
- `repos/` — Room DAO wrappers + Retrofit API wrappers; the only layer ViewModels call
- `entities/` — Room `@Entity` data classes
- `data/AppSettingsDataStore.kt` — DataStore-backed settings; single source of truth for `akilimoUser`, language, country, theme

**Navigation flow:** first run: `LegalWizardRoute` → `OnboardingRoute` (first-run wizard only) → `HubRoute`. Every later launch starts at `HubRoute`, the app home, whose cards open `RecommendationsRoute` (advice types, then the use-case screens), `ProfileRoute` (edit details, same sections and view model as onboarding) and `SettingsRoute`.

**Theme:** the app starts in the light theme; `ThemeMode` (System / Light / Dark) is chosen in Settings and stored in `AppSettingsDataStore`.

**Database:** Room v8 with explicit `Migration` objects (`DatabaseMigrations.kt`); the destructive fallback is only used on downgrade. Always write a proper `Migration` for schema changes, bump the version and update the exported schema.

## Compose Screen Conventions

Every Compose screen uses the shared components from `ui/components/compose/`:

```kotlin
// Top bar with back arrow
BackTopAppBar(title = stringResource(R.string.lbl_foo), onBack = { navController.popBackStack() })

// Full-width save/confirm button (bottom bar)
SaveBottomBar(label = stringResource(R.string.lbl_save), enabled = canSave, onClick = { ... })

// Scrollable form body
ScrollableFormColumn(padding = paddingValues) { /* content */ }

// Navigate back and mark task complete
navController.completeTask(EnumAdviceTask.SOME_TASK)
```

`@OptIn(ExperimentalMaterial3Api::class)` is needed on any screen that calls `BackTopAppBar` (its scroll-behaviour parameter is experimental), `ModalBottomSheet` or `DatePicker`.

## Design conventions

Use the theme tokens (`MaterialTheme.colorScheme`, `MaterialTheme.shapes`, `AkilimoSpacing`); never hard-code colours or sizes in screens. The palette follows the logo (deep green `#006F45`, leaf lime `#9ACA3C` used only as a fill with dark text). Cards and buttons use `MaterialTheme.shapes.medium` (12dp).

Reuse the shared components in `ui/components/compose/` before adding a new one:
- `FormCard` / `Modifier.cardSurface()` — raised card that groups fields or sections
- `AkilimoTextField`, `AkilimoDropdown`, `DateInputField` — the label sits above the field
- `RadioButtonRow` — raised, selectable option card (optional `supportingText`)
- `SegmentedTabRow` (and `BinaryToggleChips`) — two to four mutually exclusive choices
- `StatusChip` / `StatusTone` — status in words plus semantic colour
- `StatSummaryCard`, `ProgressSummaryCard` — key figures and progress at the top of a screen
- `IconTile` — icon on a soft tile for list rows and cards
- `InfoCard`, `ResultCard`, `SelectionCard`, `SectionHeader`, `WizardBottomBar`

When the confirm action depends on nullable state, use `?.let` instead of early-return:
```kotlin
onClick = { selectedItem?.let { viewModel.save(it); navController.completeTask(task) } }
```

## ViewModel / State Conventions

- Each screen has a nested `data class UiState(...)` with default values.
- Expose state as `StateFlow<UiState>` via `_uiState.asStateFlow()`.
- No `LiveData` in new code.
- Load data in `init { viewModelScope.launch { ... } }`.
- Use `_uiState.update { it.copy(...) }` for mutations.
- `saved: Boolean` flag pattern: set `true` after save, reset via `onSaveHandled()` after the screen observes it.

## Unit Test Conventions

Use `UnconfinedTestDispatcher` (via `TestDispatcherRule`). Because this dispatcher runs coroutines eagerly, the ViewModel `init` block executes **synchronously during `@Before setUp()`**, before any test-body `coEvery` setup.

**Required pattern for ViewModels with `init` coroutines:**
```kotlin
@Before
fun setUp() {
    clearAllMocks()                              // clear stale mock state between tests
    every { appSettings.akilimoUser } returns "user1"
    // DO NOT create the ViewModel here
}

private fun buildViewModel() = MyViewModel(repo, appSettings)  // create per-test, after coEvery setup

@Test
fun `my test`() = runTest {
    coEvery { repo.getUser("user1") } returns testUser   // configure BEFORE buildViewModel()
    val viewModel = buildViewModel()
    advanceUntilIdle()
    // assert
}
```

Always include `every { appSettings.akilimoUser } returns "<username>"` before building a ViewModel that uses it, even with `relaxed = true` mocks (relaxed returns `""` for String, causing user lookups to fail silently).

## Language / Locale

Language tags must be BCP-47 (`"en"`, `"sw"`, `"ha"`, not display names). Always write to `AppSettingsDataStore` before calling `AppCompatDelegate.setApplicationLocales()`.

## Navigation

Current screens use `NavHostController`. Task completion passes a result back via `savedStateHandle`:
```kotlin
// NavExtensions.completeTask() does this:
previousBackStackEntry?.savedStateHandle?.set("completed_task", AdviceCompletionDto(task, EnumStepStatus.COMPLETED))
popBackStack()
```

Callers observe `currentBackStackEntry?.savedStateHandle` in a `LaunchedEffect` to detect task completion.
