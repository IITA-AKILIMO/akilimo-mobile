# AKILIMO Mobile — Developer Guide

This guide covers day-to-day engineering work: local setup, common commands, coding conventions, and safe change practices.

## 1. Local setup checklist

1. Install Android Studio (latest stable).
2. Install/configure JDK 17.
3. Ensure Android SDK 36 and build tools are installed.
4. Configure `MAPBOX_DOWNLOADS_TOKEN` in `~/.gradle/gradle.properties`.
5. Open project in Android Studio and run Gradle sync.

## 2. Common Gradle commands

### Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease bundleRelease
```

### Tests

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Quality checks

```bash
./gradlew lintDebug
./gradlew detekt
./gradlew sonar
```

## 3. Runtime configuration

Build-time defaults can be overridden through environment variables:

- `AKILIMO_BASE_URL`
- `FUELROD_BASE_URL`

These become `BuildConfig` fields and are then resolved through `AppConfig` (session value first, then build fallback).

## 4. Code organization conventions

Main package: `com.akilimo.mobile`

| Package | Purpose |
|---------|---------|
| `ui/activities/` | All Activity subclasses (extend `BaseActivity<VB>`) |
| `ui/fragments/` | All Fragment subclasses (extend `BaseFragment<VB>` or `BaseStepFragment<VB>`) |
| `ui/screens/` | Compose screens grouped by feature (`usecases/`, `settings/`, `recommendations/`) |
| `ui/components/compose/` | Shared Compose primitives: `BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions`, `AkilimoTextField`, `SelectionCard`, `WizardBottomBar` |
| `ui/viewmodels/` | One `@HiltViewModel` per Compose screen; all new screens use this pattern |
| `repos/` | Repository classes wrapping Room DAOs — all DB access goes through here |
| `dao/` + `entities/` | Room `@Dao` interfaces and `@Entity` data classes |
| `workers/` | WorkManager `CoroutineWorker` subclasses + `WorkerScheduler` |
| `network/` + `rest/` | Retrofit service interfaces, `ApiClient`, request/response models |
| `data/` | `AppSettingsDataStore.kt` — Preferences DataStore for all 17 settings keys (replaces deleted `SessionManager`) |
| `helper/` | `LocaleHelper` (locale context wrapper); `SessionManager` has been deleted |
| `enums/` | Domain enums: `EnumCountry`, `EnumAreaUnit`, `EnumAdvice`, etc. |

**When adding a new screen:**
1. Create a `@Composable` screen function under `ui/screens/<feature>/`.
2. Create a `@HiltViewModel` in `ui/viewmodels/` with a nested `UiState` data class and `StateFlow<UiState>`.
3. Use shared components from `ui/components/compose/` (`BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions.completeTask`).
4. Add the route to `navigation/Route.kt` and wire it in `AkilimoNavHost.kt`.
5. Do not add new `Activity` or `Fragment` classes — Compose + NavHost is the active path.

See `docs/COMPOSE_MIGRATION.md` for full conventions.

**When adding a new stepper step:**
1. Create Fragment extending `BaseStepFragment<VB>`.
2. Override `prefillFromEntity()` to reload saved state when the step is re-selected.
3. Override `verifyStep()` to return a `VerificationError` if inputs are incomplete.
4. Register in `StepperAdapter`.

**Language/locale changes:**
- Always save language as a full BCP-47 tag (e.g., `"sw-TZ"`, not `"sw"`).
- Use `Locales.supportedLocales` as the single source of supported locales.
- Write to DataStore **before** calling `setApplicationLocales()` to prevent a stale locale read during activity recreation (race condition fix):
  ```kotlin
  safeScope.launch {
      appSettings.setLanguageTag(tag)
      AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
      AppLocale.desiredLocale = …   // secondary step for Reword string-replacement library
  }
  ```
- `ProcessPhoenix` has been removed — `AppCompatDelegate.setApplicationLocales()` recreates activities in-process.
- See `docs/ARCHITECTURE.md §5` for the full locale system.

## 5. Database and threading

All repository methods are `suspend` functions called from `safeScope.launch { }` or a background dispatcher. Do not add main-thread DB calls — `allowMainThreadQueries()` has been removed.

Room DB version: **2** — `fallbackToDestructiveMigration()` is active. When changing entity schema, write a proper `Migration` object to avoid wiping user data.

## 6. Coroutines conventions

- In Activities/Fragments, use `safeScope.launch { }` (= `lifecycleScope`).
- Switch to `Dispatchers.Main` for UI updates inside a coroutine block.
- Repositories use `suspend` functions — do not `runBlocking` anywhere in production code.
- `NetworkMonitor.isConnected` is a `StateFlow<Boolean>` — collect via `repeatOnLifecycle(STARTED)` in `BaseActivity`.

## 7. Recommended pre-PR checklist

1. `./gradlew lintDebug detekt testDebugUnitTest`
2. Verify changed flows on emulator/device (especially locale-related changes).
3. Update `docs/` if architecture or config behavior changed.
4. Add `CHANGELOG.md` entry for user-visible changes.
5. Ensure no new hardcoded secrets introduced in source — use `BuildConfig` or `local.properties`.

## 8. Release-related references

- Main pipeline: `.github/workflows/android.yml`
- Changelog: `CHANGELOG.md`
- Release note assets: `release/distribution/whatsnew/`
- Script docs: `scripts/README.md`

## 9. Compose development conventions

These apply to all new screens. The Compose migration is active — `MainActivity` and `AkilimoNavHost` are live; Phase 3 (use-case screens) and Phase 4 (settings) are complete.

### File layout for a Compose screen

```
ui/
├── screens/
│   └── settings/
│       ├── UserSettingsScreen.kt        @Composable screen root
│       ├── UserSettingsViewModel.kt     @HiltViewModel
│       └── UserSettingsUiState.kt       data class + sealed NavEvent
├── components/
│   └── compose/
│       ├── BackTopAppBar.kt      TopAppBar with back arrow (carries its own @OptIn)
│       ├── SaveBottomBar.kt      Full-width save/confirm button bar
│       ├── ScrollableFormColumn.kt  fillMaxSize + horizontalPadding + verticalScroll
│       ├── NavExtensions.kt      completeTask(EnumAdviceTask) extension
│       ├── AkilimoButton.kt
│       ├── AkilimoCard.kt
│       ├── AkilimoTextField.kt
│       └── NetworkBanner.kt
└── theme/
    ├── AkilimoTheme.kt
    ├── AkilimoColors.kt
    ├── AkilimoTypography.kt
    └── AkilimoShapes.kt
```

### Required shared components

Every screen with a back-navigation top bar uses `BackTopAppBar`:
```kotlin
BackTopAppBar(title = stringResource(R.string.lbl_foo), onBack = { navController.popBackStack() })
```

Every screen with a primary action uses `SaveBottomBar` in the Scaffold `bottomBar`:
```kotlin
SaveBottomBar(label = stringResource(R.string.lbl_save), enabled = canSave, onClick = { ... })
```

Scrollable form body:
```kotlin
ScrollableFormColumn(padding = paddingValues) { /* form fields */ }
```

Task completion (writes result to `savedStateHandle` and pops back):
```kotlin
navController.completeTask(EnumAdviceTask.SOME_TASK)
```

`@OptIn(ExperimentalMaterial3Api::class)` is only needed on screens that directly use `ModalBottomSheet` or `DatePicker`. `BackTopAppBar` carries its own `@OptIn` internally.

When the confirm action depends on nullable state, use `?.let` — early return is not valid in a `() -> Unit` lambda:
```kotlin
onClick = {
    selectedItem?.let { item ->
        viewModel.save(item)
        navController.completeTask(EnumAdviceTask.SOME_TASK)
    }
}
```

### State conventions

- Every screen has exactly one `UiState` data class.
- ViewModels expose `StateFlow<UiState>` — never `LiveData`, never raw mutable state.
- One-shot navigation events use `SharedFlow<NavEvent>` collected via `LaunchedEffect(Unit)`.
- Screens are **stateless** — they receive state and callbacks only.

### Theming

- Always wrap screen content in `AkilimoTheme { }`.
- Use `MaterialTheme.colorScheme.*`, `MaterialTheme.typography.*`, `MaterialTheme.shapes.*` — never hardcode colours or sizes.
- Dark mode is driven by `isSystemInDarkTheme()` passed from the host activity.

### Previews

Every composable must have a `@Preview` with both light and dark variants:

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UserSettingsScreenPreview() {
    AkilimoTheme { UserSettingsScreen(uiState = UserSettingsUiState(), onEvent = {}) }
}
```

### No-go list during migration

- ❌ Do not use `LiveData` — use `StateFlow`.
- ❌ Do not use `findViewById` in migrated screens.
- ❌ Do not mix ViewBinding and Compose in the same screen after migration.
- ❌ Do not call `startActivity()` from a Compose screen — use `navController.navigate()`.
- ❌ Do not access `AppSettingsDataStore` directly from a composable — inject via `@HiltViewModel`.

## 10. Known active bugs

| Ticket | Status | Description | Files |
|--------|--------|-------------|-------|
| MUN-16 | ✅ Fixed | Language selection did not persist — short codes, missing AppLocale sync, race condition on activity recreation | `WelcomeFragment.kt`, `UserSettingsActivity.kt`, `AkilimoApp.kt`, `AppSettingsDataStore.kt` |
| MUN-22 | ✅ Fixed | Hilt `@AndroidEntryPoint` missing on concrete Activities/Fragments — covered all 25+ entry points | All activity/fragment files |
| feat/#475 | ✅ Fixed | DataStore migration — `SessionManager.kt` deleted; `AppSettingsDataStore` covers all 17 settings keys with `SharedPreferencesMigration` | `data/AppSettingsDataStore.kt`, `BaseActivity.kt`, `BaseFragment.kt` |
| — | ✅ Fixed | Language race condition — DataStore write now happens inside `safeScope.launch` before `setApplicationLocales()` call | `WelcomeFragment.kt` |
| — | ✅ Fixed | WorkManager `IllegalStateException` on app start — missing `Configuration.Provider` | `AkilimoApp.kt`, `AndroidManifest.xml` |
| — | ✅ Fixed | Dark mode setting saved but never applied — `MODE_NIGHT_NO` override removed; now driven by `AppSettingsDataStore.darkMode` in `AkilimoApp` | `BaseActivity.kt`, `AkilimoApp.kt` |
| — | ✅ Fixed | `allowMainThreadQueries()` removed — all DB calls confirmed in coroutines | `AppDatabase.kt` |

See `docs/ARCHITECTURE.md §10` for the full technical debt register and `docs/ROADMAP.md` for prioritised fix order.
