# AKILIMO Mobile — Architecture

This document describes the architecture of the AKILIMO Android app. Conventions for writing new code are in `AGENTS.md`; this file explains how the pieces fit together.

> Checked against the code on 2026-11-17 (`develop`).

---

## 1. High-Level Architecture

A single-Activity **Jetpack Compose** app using **Hilt** and **MVVM** with reactive state (`StateFlow`).

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  MainActivity (@AndroidEntryPoint) — the only Activity           │
│   └─ AkilimoNavHost (Navigation Compose, type-safe routes)       │
│       └─ Compose screens, grouped by feature                     │
└────────────────────────────┬────────────────────────────────────┘
                             │ StateFlow<UiState> (and Flow<Effect> where a screen needs one-shot events)
┌────────────────────────────▼────────────────────────────────────┐
│                        VIEWMODEL LAYER (Hilt)                   │
│  @HiltViewModel, one per screen                                 │
│   └─ MutableStateFlow<UiState>                                  │
└────────────────────────────┬────────────────────────────────────┘
                             │ repository methods (suspend / Flow)
┌────────────────────────────▼────────────────────────────────────┐
│                        REPOSITORY LAYER (repos/)                 │
│  The only layer ViewModels call. Wraps Room DAOs and Retrofit.   │
└──────────┬──────────────────────────┬───────────────────────────┘
           │ Room DAO                 │ Retrofit service
┌──────────▼──────────────┐  ┌───────▼───────────────────────────┐
│    LOCAL (Room)         │  │    REMOTE (Retrofit 3 / OkHttp 5) │
│  DB: AKILIMO_MOBILE     │  │  AkilimoApi, AccountApi,          │
│  version 9, explicit    │  │  LocationIqApi, WeatherApi        │
│  migrations             │  │  Moshi JSON                       │
└─────────────────────────┘  └───────────────────────────────────┘
           │
┌──────────▼─────────────────────────────────────────────────────┐
│                      CROSS-CUTTING                              │
│  AppSettingsDataStore — Preferences DataStore (settings)        │
│  Hilt DI                                                        │
│  NetworkMonitor      — connectivity as a StateFlow              │
│  WorkManager         — reference-data sync, DataRefresher       │
│  Sentry + Firebase Analytics — error reporting and analytics    │
│  Native locales      — AppCompatDelegate / AppLanguage          │
└────────────────────────────────────────────────────────────────┘
```

---

## 2. Package Structure

Under `com.akilimo.mobile`:

```
├── AkilimoApp.kt             Application: Hilt, WorkManager config, locale/theme init, start-up syncs
├── Locales.kt                Canonical BCP-47 tags (en-US, sw-TZ, rw-RW)
├── base/workers/             Paged-worker base classes and the retry policy
├── config/AppConfig.kt       Runtime API base URLs (saved value first, then build default)
├── data/
│   ├── AppSettingsDataStore.kt  Preferences DataStore: single source of truth for settings
│   ├── AppLanguage.kt, LocaleHelper.kt  Language handling
│   └── account/              Signed-in session store, token encryption, account prompt state
├── database/                 AppDatabase (Room), DatabaseMigrations, type converters
├── di/                       Hilt modules (AppModule, AccountModule)
├── dao/                      Room @Dao interfaces
├── entities/                 Room @Entity classes (+ relations/)
├── dto/                      Small UI and option models
├── enums/                    Domain enums (EnumCountry, EnumAreaUnit, EnumAdviceTask, …)
├── network/                  Retrofit services (AkilimoApi, AccountApi, LocationIqApi, WeatherApi),
│                             ApiClient, RetryInterceptor, NetworkMonitor, plus dto/, request/, response/
├── navigation/               Route.kt (serializable routes), FeatureGraphs.kt, AkilimoNavHost.kt
├── repos/                    Repositories; account/ holds the account and saved-advice repos
├── workers/                  WorkManager workers, WorkerScheduler, DataRefresher, sync status
├── utils/                    Small helpers (AmountInput, DateHelper, PlantingSchedule, CoverageArea, …)
└── ui/
    ├── activities/           MainActivity
    ├── screens/              Compose screens by feature:
    │                         hub/, profile/, farm/, onboarding/, recommendations/, usecases/,
    │                         account/, settings/
    ├── viewmodels/           One @HiltViewModel per screen
    ├── components/compose/   Shared components (see AGENTS.md "Design conventions")
    └── theme/                AkilimoTheme, colors, shapes, spacing, typography
```

---

## 3. Startup and Navigation

### Startup sequence
`AkilimoApp.onCreate()` runs first:
1. Initialises Hilt, WorkManager (custom `Configuration.Provider`) and `NetworkMonitor`.
2. Applies the saved language and theme from `AppSettingsDataStore`.
3. Schedules the reference-data syncs (see section 7).

`MainActivity.onCreate()` then picks the start route from the settings and sets the content to `AkilimoTheme { AkilimoNavHost(startRoute) }`:

| State | Start route |
|-------|-------------|
| First run, disclaimer or terms not accepted | `LegalWizardRoute` |
| First run, legal steps done | `OnboardingRoute` (first-run wizard only) |
| Every later launch | `HubRoute` |

### Navigation
- Routes are `@Serializable` objects or data classes in `Route.kt`; `FeatureGraphs.kt` defines one graph per feature and `AkilimoNavHost` ties them together.
- The hub opens `RecommendationsRoute` (advice types, then the use-case screens), `ProfileRoute`, `FarmRoute`, `SettingsRoute` and the account screens (`AccountRoute`, `SavedAdviceRoute`).
- A use-case screen reports completion with `navController.completeTask(task)`, which sets a result on the previous back-stack entry's `savedStateHandle` and pops back; the caller observes it.

---

## 4. State Management

- Each screen has one nested `UiState` data class with defaults, exposed as `StateFlow<UiState>`.
- Screens that need one-shot events (the onboarding wizard, for example) also expose an `Effect` flow.
- Data flow: UI event → ViewModel → repository (suspend or `Flow`) → ViewModel updates `_uiState` → the screen recomposes.
- No `LiveData`. ViewModels load data in `init`, mutate with `_uiState.update { it.copy(...) }`, and use a `saved` flag reset with `onSaveHandled()` after a save.

---

## 5. Persistence and Settings

### Room
- `AppDatabase` (version 9, DB file `AKILIMO_MOBILE`) holds the farm profile and selections (users, selected fertilizers, markets, investments, yields), the reference data (fertilizers, fertilizer prices, cassava prices and units, maize units, starch factories, investment amounts) and the farmer's saved advice. Schemas are exported to `app/schemas/`.
- Migrations are explicit objects in `DatabaseMigrations.kt`. The destructive fallback is used only on a downgrade. Always write a `Migration`, bump the version and update the exported schema.
- Reference-data tables are caches. After a full sync, rows the API no longer serves are removed; a saved fertilizer or cassava pick keeps its stored price and only loses the link.

### AppSettingsDataStore
- Single source of truth for app-level settings: signed-in user, language, country, theme, text size, list layout and similar. Built on Preferences DataStore; it replaced the old `SharedPreferences`.

### Account
- Optional sign-in. The session is held in `data/account/` with the token encrypted (`TokenCipher`). `AccountApi` talks to the account endpoints (`v1/account/...`); `SavedAdviceRepo` keeps a local copy of the saved advice in step with the server, and only when a signed-in farmer has given consent.

---

## 6. Internationalization (i18n)

- **Storage**: `AppSettingsDataStore` stores the BCP-47 tag (`en-US`, `sw-TZ`, `rw-RW`); always write it before applying the locale.
- **Application**: `AppCompatDelegate.setApplicationLocales()` and `AppLanguage` apply it, and `MainActivity` localises its base context so the choice also works on older Android versions. A change applies without restarting the app.
- **Option**: "lock app language" keeps the chosen language regardless of the phone's language.
- **UI**: `stringResource(R.string.*)`; canonical tags live in `Locales.kt`.

---

## 7. Background Work

`WorkManager` (via `WorkerScheduler`) syncs the reference data so the app works offline after the first sync:

| Worker | Data |
|--------|------|
| `FertilizerWorker` → `FertilizerPriceWorker` (chained) | Fertilizers, then their price options |
| `InvestmentAmountWorker` | Investment amount options |
| `CassavaPriceWorker`, `CassavaUnitWorker`, `StarchFactoryWorker` | Cassava price bands, cassava units of sale, starch factories |
| `MaizeUnitWorker` | Units maize grain is sold in |

- All of them extend a paged worker that saves page by page, and calls a hook after the last page to remove stale rows. An empty response never wipes the cache.
- `DataRefresher` runs the same syncs on demand for pull-to-refresh (fertilizer, cassava market and investment screens), joins one that is already running, and gives up after 30 seconds without a network.
- Work is unique-named with the `KEEP` policy, so starting the app repeatedly does not stack syncs.

---

## 8. Network Stack

- **Retrofit 3 + OkHttp 5 + Moshi** (generated adapters).
- **Interceptors**: HTTP logging, a safe-network guard, and `RetryInterceptor` (exponential backoff). 60-second connect, read and write timeouts.
- **Base URLs** come from `AppConfig` (saved value first, then the build default).
- **API services**:
  - `AkilimoApi`: recommendations, fertilizers and prices, cassava, maize and investment options, starch factories, coverage area, feedback
  - `AccountApi`: register, login (email or social), consent, email verification, password reset, saved-advice history, account deletion
  - `LocationIqApi`: reverse geocoding for the location picker
  - `WeatherApi`: current weather for the location picker
