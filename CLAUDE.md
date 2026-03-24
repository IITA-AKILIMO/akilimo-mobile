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

The app is a **modern Jetpack Compose-only codebase**. Legacy View-based screens have been entirely removed.

**Package layout** (under `com.akilimo.mobile`):
- `ui/activities/` — `MainActivity.kt` (single-Activity host with `AkilimoNavHost`)
- `ui/screens/` — Compose screens grouped by feature (`usecases/`, `settings/`, `recommendations/`, etc.)
- `ui/viewmodels/` — `@HiltViewModel` per screen, pattern: `StateFlow<UiState>` + `Flow<Effect>`
- `ui/components/compose/` — shared Compose primitives (`BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions.kt`)
- `repos/` — Typed repositories; the only layer ViewModels call
- `database/`, `entities/` — Room `AppDatabase`, `@Dao` interfaces, and `@Entity` data classes
- `data/AppSettingsDataStore.kt` — DataStore-backed settings; single source of truth for `akilimoUser`, language, country

**Startup:** `MainActivity` (Compose nav host) handles the entire user flow from onboarding to recommendations.

**Database:** Room v2+ with mandatory `Migration` objects for all schema changes. `fallbackToDestructiveMigration()` is discouraged.

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

`@OptIn(ExperimentalMaterial3Api::class)` is only needed on screens that use `ModalBottomSheet` or `DatePicker` directly. `BackTopAppBar` carries its own `@OptIn` internally.

When the confirm action depends on nullable state, use `?.let` instead of early-return:
```kotlin
onClick = { selectedItem?.let { viewModel.save(it); navController.completeTask(task) } }
```

## ViewModel / State Conventions

- Each screen has a nested `data class UiState(...)` with default values.
- Expose state as `StateFlow<UiState>` via `_uiState.asStateFlow()`.
- One-shot effects (navigation, snackbars) are handled via `Channel<Effect>` and exposed as `Flow<Effect>`.
- No `LiveData` or `ViewBinding` in the codebase.
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
