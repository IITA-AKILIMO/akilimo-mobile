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
| `repos/` | Repository classes wrapping Room DAOs — all DB access goes through here |
| `dao/` + `entities/` | Room `@Dao` interfaces and `@Entity` data classes |
| `workers/` | WorkManager `CoroutineWorker` subclasses + `WorkerScheduler` |
| `network/` + `rest/` | Retrofit service interfaces, `ApiClient`, request/response models |
| `helper/` | `SessionManager` (SharedPrefs singleton), `LocaleHelper` (locale context wrapper) |
| `enums/` | Domain enums: `EnumCountry`, `EnumAreaUnit`, `EnumAdvice`, etc. |

**When adding a new screen (current View system):**
1. Create Activity extending `BaseActivity<VB>` — implement `inflateBinding()` and `onBindingReady()`.
2. Create a typed Repo class in `repos/` backed by a DAO.
3. Launch via explicit `Intent` (NavGraph not yet active).

> **⚠️ Compose migration in progress.** Once Hilt and NavGraph are active (ROADMAP #9, #12),
> all new screens must be written as `@Composable` functions with a `@HiltViewModel`.
> Do not add new activities after Phase 1 of the migration begins.
> See `docs/COMPOSE_MIGRATION.md` for conventions.

**When adding a new stepper step:**
1. Create Fragment extending `BaseStepFragment<VB>`.
2. Override `prefillFromEntity()` to reload saved state when the step is re-selected.
3. Override `verifyStep()` to return a `VerificationError` if inputs are incomplete.
4. Register in `StepperAdapter`.

**Language/locale changes:**
- Always save language as a full BCP-47 tag (e.g., `"sw-TZ"`, not `"sw"`).
- Use `Locales.supportedLocales` as the single source of supported locales.
- After saving, set `AppLocale.desiredLocale` and call `ProcessPhoenix.triggerRebirth()`.
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

These apply once the Compose migration begins (ROADMAP items 13–17).

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
- ❌ Do not access `SessionManager` directly from a composable — inject via ViewModel.

## 10. Known active bugs

| Ticket | Status | Description | Files |
|--------|--------|-------------|-------|
| MUN-16 | ✅ Fixed | Language selection did not persist — short codes, missing AppLocale sync, wrong restart method | `WelcomeFragment.kt`, `UserSettingsActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt` |
| — | ✅ Fixed | WorkManager `IllegalStateException` on app start — missing `Configuration.Provider` | `AkilimoApp.kt`, `AndroidManifest.xml` |
| — | ✅ Fixed | Dark mode setting saved but never applied — `MODE_NIGHT_NO` override removed; now driven by `SessionManager.darkMode` in `AkilimoApp` | `BaseActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt` |
| — | ✅ Fixed | `allowMainThreadQueries()` removed — all DB calls confirmed in coroutines | `AppDatabase.kt` |

See `docs/ARCHITECTURE.md §10` for the full technical debt register and `docs/ROADMAP.md` for prioritised fix order.
