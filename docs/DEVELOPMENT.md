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

**When adding a new screen:**
1. Create Activity extending `BaseActivity<VB>` — implement `inflateBinding()` and `onBindingReady()`.
2. Create a typed Repo class in `repos/` backed by a DAO.
3. Launch via explicit `Intent` (no NavGraph currently).

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

`AppDatabase` currently uses `allowMainThreadQueries()` as a transitional behavior. **Do not add new main-thread DB calls.** All repository methods should be `suspend` functions called from `safeScope.launch { }` or a background dispatcher.

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

## 9. Known active bugs

| Ticket | Status | Description | Files |
|--------|--------|-------------|-------|
| MUN-16 | ✅ Fixed | Language selection did not persist — short codes, missing AppLocale sync, wrong restart method | `WelcomeFragment.kt`, `UserSettingsActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt` |
| — | ✅ Fixed | WorkManager `IllegalStateException` on app start — missing `Configuration.Provider` | `AkilimoApp.kt`, `AndroidManifest.xml` |
| — | ⬜ Open | Dark mode setting saved but never applied (`MODE_NIGHT_NO` forced in `BaseActivity.onCreate():89`) | `BaseActivity.kt` |
| — | ⬜ Open | `allowMainThreadQueries()` — ANR risk on slow devices | `AppDatabase.kt` |

See `docs/ARCHITECTURE.md §10` for the full technical debt register and `docs/ROADMAP.md` for prioritised fix order.
