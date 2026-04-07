# AKILIMO Mobile — Architecture

This document describes the modern architecture implemented in the AKILIMO Android codebase.

---

## 1. High-Level Architecture

The app follows a modern Android architecture using **Jetpack Compose**, **Hilt**, and **MVVM** with reactive state management.

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  MainActivity (@AndroidEntryPoint) — Single-Activity Host       │
│   └─ AkilimoNavHost (Jetpack Navigation for Compose)            │
│       └─ Compose Screens (Stateless Content + Screen Wrapper)    │
└────────────────────────────┬────────────────────────────────────┘
                             │ StateFlow<UiState> / Flow<Effect>
┌────────────────────────────▼────────────────────────────────────┐
│                        VIEWMODEL LAYER (Hilt)                   │
│  @HiltViewModel (one per screen)                                │
│   ├─ MutableStateFlow<UiState> — Current UI state               │
│   └─ Channel<Effect> — One-shot events (nav, snackbars)         │
└────────────────────────────┬────────────────────────────────────┘
                             │ Repository methods (suspend / Flow)
┌────────────────────────────▼────────────────────────────────────┐
│                        REPOSITORY LAYER                          │
│  UserPreferencesRepo   AkilimoUserRepo   FertilizerRepo          │
│  CassavaPriceRepo      MaizeRepo         StarchFactoryRepo …    │
└──────────┬──────────────────────────┬───────────────────────────┘
           │ Room DAO                 │ Retrofit service
┌──────────▼──────────────┐  ┌───────▼───────────────────────────┐
│    LOCAL (Room)         │  │    REMOTE (Retrofit 3 / OkHttp 5) │
│  DB: AKILIMO_V5         │  │  ApiClient → AkilimoApi            │
│  Room Migrations v2→5   │  │  Moshi JSON (KSP codegen)          │
└─────────────────────────┘  └───────────────────────────────────┘
           │
┌──────────▼─────────────────────────────────────────────────────┐
│                      CROSS-CUTTING                              │
│  AppSettingsDataStore — Preferences DataStore (Single Source)   │
│  Hilt DI (2.57.1) — @HiltAndroidApp / @AndroidEntryPoint /      │
│    @HiltViewModel                                              │
│  NetworkMonitor  — StateFlow<Boolean> connectivity              │
│  WorkManager     — FertilizerWorker, CassavaPriceWorker, etc.   │
│  Sentry 8.23 + Firebase Analytics — observability              │
│  Native Locales  — AppCompatDelegate.setApplicationLocales()   │
└────────────────────────────────────────────────────────────────┘
```

---

## 2. Package Structure

```
com.akilimo.mobile/
├── AkilimoApp.kt             Application: Hilt, WorkManager config, NetworkMonitor
├── Locales.kt                Canonical BCP-47 tags (en, sw-TZ, rw-RW)
├── data/
│   └── AppSettingsDataStore.kt  Preferences DataStore — 17 keys, single source of truth
├── database/
│   ├── AppDatabase.kt        Room singleton (version 5, 17 entities, TypeConverters)
│   └── DatabaseMigrations.kt Manual SQL migrations (v2→3, v3→4, v4→5)
├── navigation/
│   ├── Route.kt              @Serializable route definitions (20+ routes)
│   └── AkilimoNavHost.kt     Navigation graph with modular feature graphs
├── ui/
│   ├── activities/           MainActivity (single-Activity host)
│   ├── screens/              Compose screens grouped by feature
│   │   ├── onboarding/       Welcome, legal wizard, onboarding steps
│   │   ├── usecases/         Fertilizer, Cassava, Maize, Investment screens
│   │   ├── recommendations/  Recommendation screens
│   │   └── settings/         Settings, location picker, WebView
│   ├── viewmodels/           @HiltViewModel per screen (17 total)
│   │   └── usecases/         FertilizerViewModel (assisted injection)
│   ├── components/compose/   Shared Compose primitives (18 components)
│   └── theme/                Material 3 (Colors, Typography, Shapes)
├── repos/                    Typed repositories (15 total) — only layer ViewModels call
├── dao/                      Room @Dao interfaces (16 total)
├── entities/                 Room @Entity data classes (17 total)
│   └── relations/            Room relation classes
├── dto/                      Data Transfer Objects (API / UI models)
├── enums/                    Domain enums (32+): EnumCountry, EnumAreaUnit, EnumAdvice…
├── network/
│   ├── ApiClient.kt          Retrofit + OkHttp builder, TLS, interceptors
│   ├── AkilimoApi.kt         Main API service interface
│   ├── LocationIqApi.kt      Reverse geocoding service
│   ├── WeatherApi.kt         Weather data service
│   └── NetworkMonitor.kt     StateFlow<Boolean> connectivity tracking
├── rest/                     Request/response models (do not add new files here)
├── workers/                  WorkManager CoroutineWorkers + WorkerScheduler
│   ├── FertilizerWorker      → FertilizerPriceWorker (chained)
│   ├── InvestmentAmountWorker, CassavaPriceWorker, CassavaUnitWorker, StarchFactoryWorker
│   └── WorkerScheduler.kt
├── di/
│   └── AppModule.kt          15 @Singleton @Provides repository bindings
├── config/
│   └── AppConfig.kt          Runtime configuration (API endpoints)
├── helper/                   Cross-cutting helpers (LocaleHelper, WorkStateMapper)
├── utils/                    Utility functions (do not add new files here)
├── extensions/               Extension functions (WorkerExtensions)
└── base/workers/             Base CoroutineWorker subclass
```

---

## 3. Startup and Navigation

### Startup Sequence
`MainActivity.onCreate()` initializes the app:
1. `enableEdgeToEdge()` — Configures edge-to-edge display.
2. Evaluates `appSettings.isFirstRun`, `appSettings.disclaimerRead`, and `appSettings.termsAccepted` to determine the `startRoute`: `LegalWizardRoute` → `OnboardingRoute` → `RecommendationsRoute`.
3. Sets the content to `AkilimoTheme` wrapping `AkilimoNavHost(startDestination)`.

`AkilimoApp.onCreate()` runs before `MainActivity`:
1. Initializes Hilt, WorkManager (custom `Configuration.Provider`), and `NetworkMonitor`.
2. Applies locale from `AppSettingsDataStore` via `AppCompatDelegate.setApplicationLocales()`.
3. Applies dark mode preference.
4. Schedules 6 one-time WorkManager workers for reference data sync.

### Navigation Architecture
Navigation is handled entirely within Compose using `navigation-compose` with type-safe routes:
- Routes are defined as `@Serializable` objects or data classes in `Route.kt`.
- `AkilimoNavHost` maps these routes to screen composables.
- ViewModels are scoped to the navigation backstack entries via `hiltViewModel()`.

---

## 4. State Management Contract

Every screen follows a strict State/Event/Effect contract:

| Component | Responsibility |
|-----------|----------------|
| `UiState` | Data class representing what the UI should render. Immutable. |
| `Event` | Sealed interface for user actions (button clicks, text entry). |
| `Effect` | Sealed interface for one-shot side effects (navigation, errors). |

### Data Flow
1. **UI → ViewModel**: User action triggers an `onEvent(event)`.
2. **ViewModel → Repository**: ViewModel calls a repository method (suspend).
3. **Repository → ViewModel**: Repository returns data or a `Flow`.
4. **ViewModel → UI**: ViewModel updates the `_uiState` (StateFlow) or emits to `_effect` (Channel).
5. **UI Recomposition**: The screen composable observes the state and re-renders.

---

## 5. Persistence and Settings

### Room Database
- `AppDatabase` manages persistent domain entities.
- **Migrations**: Destructive migration is disabled. Manual SQL migrations must be written for all schema changes in the `database/` package.

### AppSettingsDataStore
- Single source of truth for app-level settings (language, user profile, dark mode).
- Built on `Preferences DataStore`.
- Replaced all legacy `SharedPreferences`.

---

## 6. Internationalization (i18n)

The app uses native Android locale APIs integrated with Compose:
- **Storage**: `AppSettingsDataStore` stores the BCP-47 tag.
- **Application**: `AppCompatDelegate.setApplicationLocales()` updates the locale at the system level.
- **UI**: Standard Compose `stringResource(R.string.*)` is used, which automatically reacts to locale changes.
- **Canonical Tags**: Defined in `Locales.kt` (e.g., `sw-TZ` for Swahili).

---

## 7. Background Work

Background synchronization is managed by `WorkManager` via the `WorkerScheduler` helper. Workers are responsible for fetching reference data (fertilizers, prices) and updating the local Room database, ensuring the app remains offline-capable.

---

## 8. Network Stack

- **Retrofit 3.0** + **OkHttp 5.2** + **Moshi 1.15** (KSP codegen via KSP).
- **Security**: Custom TLS handling for older Android versions (ISRG Root X1).
- **Interceptors**: Logging, `RetryInterceptor` (exponential backoff), network availability checks.
- **API Services**:
  - `AkilimoApi` — main service: recommendations, options, feedback
  - `LocationIqApi` — reverse geocoding for location picker
  - `WeatherApi` — weather data for recommendations
