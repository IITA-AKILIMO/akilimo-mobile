# AKILIMO Mobile — Developer Guide

This guide covers day-to-day engineering work: local setup, common commands, coding conventions, and safe change practices.

## 1. Local setup checklist

1. Install Android Studio (latest stable).
2. Install/configure JDK 17.
3. Ensure Android SDK 36 and build tools are installed.
4. Configure `MAPBOX_DOWNLOADS_TOKEN` in `~/.gradle/gradle.properties` (the map picker uses Mapbox).
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

- `AKILIMO_BASE_URL` and `AKILIMO_API_KEY`
- `FUELROD_BASE_URL`
- `MAPBOX_RUNTIME_TOKEN`, `LOCATION_IQ_TOKEN`, `WEATHER_API_KEY`

These become `BuildConfig` fields. The API base URLs are resolved through `AppConfig` (saved value first, then build default). Never commit a key or token; use environment variables, `local.properties` or `~/.gradle/gradle.properties`.

## 4. Code organization conventions

Main package: `com.akilimo.mobile`

| Package | Purpose |
|---------|---------|
| `ui/activities/` | `MainActivity`: the single Activity, hosting `AkilimoNavHost` |
| `ui/screens/` | Compose screens by feature: `hub/`, `profile/`, `farm/`, `onboarding/`, `recommendations/`, `usecases/`, `account/`, `settings/` |
| `ui/components/compose/` | Shared Compose components (see `AGENTS.md`, "Design conventions") |
| `ui/viewmodels/` | One `@HiltViewModel` per screen; `StateFlow<UiState>` (plus an `Effect` flow where a screen needs one-shot events) |
| `ui/theme/` | `AkilimoTheme`, colours, shapes, spacing, typography |
| `repos/` | Repositories wrapping Room DAOs and Retrofit; `repos/account/` holds the account and saved-advice repos |
| `dao/` + `entities/` | Room `@Dao` interfaces and `@Entity` classes |
| `workers/` | WorkManager workers, `WorkerScheduler`, `DataRefresher` (pull-to-refresh) |
| `network/` | Retrofit services (`AkilimoApi`, `AccountApi`, `LocationIqApi`, `WeatherApi`), `ApiClient`, interceptors, `NetworkMonitor` |
| `data/` | `AppSettingsDataStore` (settings), `AppLanguage`, and `data/account/` (session store, token encryption) |
| `enums/` | Domain enums: `EnumCountry`, `EnumAreaUnit`, `EnumAdviceTask`, `EnumStepStatus`, etc. |
| `navigation/` | `Route.kt` (serializable routes), `FeatureGraphs.kt`, `AkilimoNavHost.kt` |
| `di/` | Hilt modules (`AppModule`, `AccountModule`) |
| `utils/` | Small helpers (`AmountInput`, `DateHelper`, `PlantingSchedule`, `CoverageArea`, ...) |
Package guardrails:

- Do not add new files to `utils/` unless there is a strong cross-cutting reason and no better specific package exists.
- Prefer moving code into a feature package, `data/`, `navigation/`, `workers/`, or `ui/components/` instead of extending generic buckets.
- New remote code should converge toward one remote-data package path rather than growing parallel structures.

**When adding a new screen:**
1. Create a `@Composable` screen function under `ui/screens/<feature>/`.
2. Create a `@HiltViewModel` in `ui/viewmodels/` with a nested `UiState` data class and `StateFlow<UiState>`.
3. Use shared components from `ui/components/compose/` (`BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions.completeTask`).
4. Add the route to `navigation/Route.kt` and wire it in `AkilimoNavHost.kt`.
5. Do not add new `Activity` or `Fragment` classes — Compose + NavHost is the active path.

See `AGENTS.md` for the Compose and ViewModel conventions.


**Language/locale changes:**
- Always save language as a full BCP-47 tag (e.g., `"sw-TZ"`, not `"sw"`).
- Use `Locales.supportedLocales` as the single source of supported locales.
- Write to DataStore **before** applying the locale, so a recreated Activity does not read a stale value. The settings screen does this in the ViewModel (`setLanguageTagAndOverride(tag)`, then a `LanguageChanged` effect), and the screen then calls `AppLanguage.apply(tag)`, which wraps `AppCompatDelegate.setApplicationLocales()` and recreates the Activity on Android versions that need it.
- The `lock app language` setting keeps the chosen language regardless of the phone's language.- See `docs/ARCHITECTURE.md` §6 for the full locale system.

## 5. Database and threading

All repository methods are `suspend` functions called from `viewModelScope.launch { }` or a background dispatcher. Do not add main-thread DB calls — `allowMainThreadQueries()` has been removed.

Room DB version: **9** — the destructive fallback is used only on a downgrade. When changing entity schema, write a proper `Migration` object in `database/DatabaseMigrations.kt` to avoid wiping user data.

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

- Workflows: `.github/workflows/` (`unit-test.yml`, `build-apk.yml`, `sonar-analysis.yml`, `next-release-pr.yml`, `github-release.yml`, `publish.yml`)
- Changelog: `CHANGELOG.md`
- Release note assets: `release/distribution/whatsnew/`
- Script docs: `scripts/README.md`

## 9. Compose development conventions

These apply to all new screens. The Compose migration is **100% complete** — all screens are Jetpack Compose. No Fragments, no ViewBinding, no XML layouts remain.

### File layout for a Compose screen

```
ui/
├── screens/
│   └── usecases/
│       └── FertilizerScreen.kt          @Composable screen root (stateless content + screen wrapper)
├── viewmodels/
│   └── FertilizerViewModel.kt           @HiltViewModel with a nested UiState
├── components/compose/                  Shared components: BackTopAppBar, SaveBottomBar, ScrollableFormColumn,
│                                        NavExtensions (completeTask), FormCard, RadioButtonRow, ... (see AGENTS.md)
└── theme/                               AkilimoTheme, AkilimoColors, AkilimoShapes, AkilimoSpacing, AkilimoTypography
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
- The theme is chosen by the user in Settings (ThemeMode: system, light or dark); MainActivity resolves it and passes darkTheme to AkilimoTheme.

### Previews

Every composable must have a `@Preview` with both light and dark variants:

```kotlin
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyScreenPreview() {
    AkilimoTheme { MyScreenContent(state = MyViewModel.UiState(), onEvent = {}) }
}
```
### No-go list

- ❌ Do not use `LiveData` — use `StateFlow`.
- ❌ Do not use `findViewById` or XML layouts — every screen is Compose.
- ❌ Do not call `startActivity()` from a Compose screen — use `navController.navigate()`.
- ❌ Do not access `AppSettingsDataStore` directly from a composable — inject via `@HiltViewModel`.
