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
| `ui/activities/` | `MainActivity` — single-Activity host with `AkilimoNavHost` |
| `ui/screens/` | Compose screens grouped by feature (`usecases/`, `settings/`, `recommendations/`, `onboarding/`) |
| `ui/components/compose/` | Shared Compose primitives: `BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions`, `AkilimoTextField`, `SelectionCard`, `BinaryToggleChips`, `RadioButtonRow`, `SwitchRow` |
| `ui/viewmodels/` | One `@HiltViewModel` per screen; `StateFlow<UiState>` + `Channel<Effect>` pattern |
| `repos/` | Repository classes wrapping Room DAOs — all DB access goes through here |
| `dao/` + `entities/` | Room `@Dao` interfaces (16) and `@Entity` data classes (17) |
| `workers/` | WorkManager `CoroutineWorker` subclasses + `WorkerScheduler` |
| `network/` + `rest/` | Retrofit service interfaces (`AkilimoApi`, `LocationIqApi`, `WeatherApi`), `ApiClient`, request/response models |
| `data/` | `AppSettingsDataStore.kt` — Preferences DataStore for all 17 settings keys |
| `enums/` | Domain enums: `EnumCountry`, `EnumAreaUnit`, `EnumAdvice`, `EnumAdviceTask`, `EnumStepStatus`, etc. |
| `navigation/` | `Route.kt` (@Serializable routes) + `AkilimoNavHost.kt` (modular feature graphs) |
| `di/` | `AppModule.kt` — 15 @Singleton repository providers |
| `helper/` | Cross-cutting helpers (`LocaleHelper`, `WorkStateMapper`); do not add new files here |

Package guardrails:

- Do not add new files to `rest/`, `helper/`, `utils/`, or `interfaces/` unless there is a strong cross-cutting reason and no better specific package exists.
- Prefer moving code into a feature package, `data/`, `navigation/`, `workers/`, or `ui/components/` instead of extending generic buckets.
- New remote code should converge toward one remote-data package path rather than growing parallel structures.

**When adding a new screen:**
1. Create a `@Composable` screen function under `ui/screens/<feature>/`.
2. Create a `@HiltViewModel` in `ui/viewmodels/` with a nested `UiState` data class and `StateFlow<UiState>`.
3. Use shared components from `ui/components/compose/` (`BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions.completeTask`).
4. Add the route to `navigation/Route.kt` and wire it in `AkilimoNavHost.kt`.
5. Do not add new `Activity` or `Fragment` classes — Compose + NavHost is the active path.

See `docs/COMPOSE_MIGRATION.md` for full conventions.


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

Room DB version: **5** — `fallbackToDestructiveMigration()` is disabled. When changing entity schema, write a proper `Migration` object in `database/DatabaseMigrations.kt` to avoid wiping user data.

## 6. Coroutines conventions

- In ViewModels, use `viewModelScope.launch { }`.
- Repositories use `suspend` functions — do not `runBlocking` anywhere in production code.
- `NetworkMonitor.isConnected` is a `StateFlow<Boolean>` — collect via `collectAsStateWithLifecycle()` in Compose screens.

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

These apply to all new screens. The Compose migration is **100% complete** — all screens are Jetpack Compose. No Fragments, no ViewBinding, no XML layouts remain.

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

- Every screen has exactly one `UiState` data class with default values.
- ViewModels expose `StateFlow<UiState>` via `_uiState.asStateFlow()` — never `LiveData`, never raw mutable state.
- One-shot effects (navigation, snackbars) use `Channel<Effect>` exposed as `Flow<Effect>` — prevents re-delivery on recomposition.
- Screens are **stateless** — they receive state and callbacks only.
- Use `_uiState.update { it.copy(...) }` for state mutations.
- `saved: Boolean` flag: set `true` after save, reset via `onSaveHandled()` after the screen observes it.

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

### No-go list

- ❌ Do not use `LiveData` — use `StateFlow`.
- ❌ Do not use `findViewById` in migrated screens.
- ❌ Do not mix ViewBinding and Compose in the same screen after migration.
- ❌ Do not call `startActivity()` from a Compose screen — use `navController.navigate()`.
- ❌ Do not access `AppSettingsDataStore` directly from a composable — inject via `@HiltViewModel`.

## 10. Resolved bugs (historical reference)

| Ticket | Description |
|--------|-------------|
| MUN-16 | Language selection did not persist — fixed: BCP-47 tags, DataStore write before `setApplicationLocales()`, race condition eliminated |
| MUN-22 | Hilt `@AndroidEntryPoint` missing on entry points — fixed during Compose migration |
| feat/#475 | DataStore migration — `SessionManager.kt` deleted; `AppSettingsDataStore` covers all 17 settings keys |
| — | WorkManager `IllegalStateException` on app start — fixed: `Configuration.Provider` in `AkilimoApp` |
| — | Dark mode setting saved but never applied — fixed: `AppSettingsDataStore.darkMode` drives night mode in `AkilimoApp` |
| — | `allowMainThreadQueries()` removed — all DB calls in coroutines |

See `docs/ROADMAP.md` for open items (R8 minification, unit test coverage, deep links, offline cache).
