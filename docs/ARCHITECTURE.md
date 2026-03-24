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
│  DB: AKILIMO_V4         │  │  ApiClient → AkilimoApi            │
│  Room Migrations        │  │  Moshi JSON (KSP codegen)          │
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
├── database/                 
│   └── AppDatabase.kt        Room singleton (Migrations, TypeConverters)
├── Locales.kt                Canonical BCP-47 tags (en-US, sw-TZ, rw-RW)
├── data/
│   └── AppSettingsDataStore.kt  Preferences DataStore (Reactive settings)
├── navigation/
│   ├── Route.kt              @Serializable route definitions
│   └── AkilimoNavHost.kt     Navigation graph and transitions
├── ui/
│   ├── activities/           MainActivity (launcher / single-host)
│   ├── screens/              Compose screens grouped by feature
│   ├── viewmodels/           @HiltViewModel per screen
│   ├── components/compose/   Shared Compose primitives (Buttons, Cards, Forms)
│   └── theme/                Material 3 (Colors, Typography, Shapes)
├── repos/                    Typed repositories wrapping DAOs and APIs
├── dao/                      Room @Dao interfaces
├── entities/                 Room @Entity data classes
├── dto/                      Data Transfer Objects (API / UI models)
├── network/
│   ├── ApiClient.kt          Retrofit builder, TLS, interceptors
│   ├── AkilimoApi.kt         Main API interface
│   └── NetworkMonitor.kt     Connectivity tracking
└── workers/                  WorkManager workers + WorkerScheduler
```

---

## 3. Startup and Navigation

### Startup Sequence
`MainActivity.onCreate()` initializes the app:
1. `enableEdgeToEdge()` — Configures edge-to-edge display.
2. Evaluates `appSettings.isFirstRun` and `appSettings.disclaimerRead` to determine the `startRoute`.
3. Sets the content to `AkilimoTheme` wrapping `AkilimoNavHost(startDestination)`.

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

- **Retrofit 3.0** + **OkHttp 5.2** + **Moshi 1.15** (KSP).
- **Security**: Custom TLS handling for older Android versions (ISRG Root X1).
- **Interceptors**: Logging, retries, and network availability checks.
