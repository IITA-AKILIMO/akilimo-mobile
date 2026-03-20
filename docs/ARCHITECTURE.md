# AKILIMO Mobile — Architecture

This document describes the architecture implemented in the Android codebase.

---

## 1. High-Level Architecture

The app follows a layered MVVM-adjacent architecture. ViewModels exist for key screens (`WelcomeViewModel`, `UserSettingsViewModel`); remaining fragments/activities still interact directly with repositories through coroutine scopes.

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                        │
│  HomeStepperActivity  UserSettingsActivity  [20+ Activities]     │
│   └─ WizardAdapter (ViewPager2) → [WelcomeFragment … Summary]   │
│  BaseActivity<VB>  →  attachBaseContext  →  LocaleHelper.wrap()  │
│  BaseFragment<VB>  →  safeScope (lifecycleScope)                 │
│  BaseStepFragment<VB>  →  WizardStep interface → prefillFromEntity() │
└────────────────────────────┬────────────────────────────────────┘
                             │ suspend / safeScope.launch
┌────────────────────────────▼────────────────────────────────────┐
│                        REPOSITORY LAYER                          │
│  UserPreferencesRepo   AkilimoUserRepo   FertilizerRepo          │
│  CassavaPriceRepo      MaizeRepo         StarchFactoryRepo …    │
└──────────┬──────────────────────────┬───────────────────────────┘
           │ Room DAO                 │ Retrofit service
┌──────────▼──────────────┐  ┌───────▼───────────────────────────┐
│    LOCAL (Room)         │  │    REMOTE (Retrofit 3 / OkHttp 5) │
│  DB: AKILIMO_19_FEB_2026│  │  ApiClient → AkilimoApi            │
│  Version: 2, 17 entities│  │  https://api.akilimo.org           │
│  Key entities:          │  │  https://akilimo.fuelrod.com       │
│   UserPreferences       │  │  RetryInterceptor, TLS legacy      │
│   AkilimoUser           │  │  Moshi JSON (KSP codegen)          │
│   Fertilizer, etc.      │  └───────────────────────────────────┘
└─────────────────────────┘
           │
┌──────────▼─────────────────────────────────────────────────────┐
│                      CROSS-CUTTING                              │
│  AppSettingsDataStore — Preferences DataStore, 17 keys,        │
│    migrated from SharedPrefs ("new-akilimo-config")             │
│  Hilt DI (2.57.1) — @HiltAndroidApp / @AndroidEntryPoint /    │
│    @HiltViewModel; kapt for Hilt compiler                       │
│  NetworkMonitor  — StateFlow<Boolean> connectivity              │
│  WorkManager     — FertilizerWorker, CassavaPriceWorker, etc.   │
│  Sentry 8.23 + Firebase Analytics — observability              │
│  AppLocale 3.1 / Reword 4.0 — i18n runtime string replacement  │
└────────────────────────────────────────────────────────────────┘
```

---

## 2. Package Structure

```
com.akilimo.mobile/
├── AkilimoApp.kt             Application: @HiltAndroidApp, locale init, WorkManager, NetworkMonitor
├── AppDatabase.kt            Room singleton (17 entities, v2, KSP)
├── Locales.kt                Supported locales: en-US, sw-TZ, rw-RW
├── base/
│   ├── BaseActivity.kt       attachBaseContext → AppSettingsDataStore.readLanguageTagSync(); network/permission setup
│   ├── BaseFragment.kt       safeScope, DB, appSettings (AppSettingsDataStore) access
│   └── BaseStepFragment.kt   WizardStep interface; onSelected() → prefillFromEntity()
├── data/
│   └── AppSettingsDataStore.kt  Preferences DataStore; 17 settings keys; SharedPreferencesMigration from "new-akilimo-config"
├── helper/
│   └── LocaleHelper.kt       createConfigurationContext() wrapper for locale application
├── ui/
│   ├── activities/           HomeStepperActivity (launcher), UserSettingsActivity, 18+ domain activities
│   ├── fragments/            WelcomeFragment … SummaryFragment (11 stepper steps)
│   └── viewmodels/           WelcomeViewModel, UserSettingsViewModel (key screens)
├── repos/                    Typed repository classes wrapping Room DAOs
├── dao/                      Room @Dao interfaces
├── entities/                 Room @Entity data classes
├── dto/                      Dropdown option DTOs (LanguageOption, CountryOption, etc.)
├── network/
│   ├── ApiClient.kt          Retrofit builder, TLS, interceptors
│   ├── AkilimoApi.kt         Main API interface
│   └── NetworkMonitor.kt     StateFlow<Boolean>
├── workers/                  WorkManager workers + WorkerScheduler
├── enums/                    EnumCountry, EnumAreaUnit, EnumAdvice, etc.
└── utils/                    DateHelper, PermissionHelper, StartupManager
```

---

## 3. Startup Sequence

`AkilimoApp.onCreate()` runs on process start in this order:

1. `networkMonitor.startMonitoring()` — begins StateFlow connectivity tracking
2. `initLocale()` — reads saved locale tag via `appSettings.getLanguageTagSync()`; sets `AppLocale.desiredLocale` and `AppCompatDelegate.setApplicationLocales()`
3. `initVectorSupport()` — enables vector drawables on pre-21 (compatibility)
4. `initTimeAndCountry()` — initializes `World` (country data library)
5. `runStartupTasks()` — `StartupManager.runHousekeeping()`
6. Schedules one-time WorkManager workers: `InvestmentAmountWorker`, `CassavaPriceWorker`, `CassavaUnitWorker`, `StarchFactoryWorker`
7. Schedules chained workers: `FertilizerWorker` → `FertilizerPriceWorker`

Each `Activity.onCreate()`:
1. `attachBaseContext()` called first — reads locale tag via `AppSettingsDataStore.readLanguageTagSync(newBase)` (companion static), wraps context via `LocaleHelper.wrap()`
2. `AppCompatDelegate.setDefaultNightMode()` — driven by `appSettings.darkMode`
3. View binding inflated, `setContentView()` called
4. `observeNetworkChanges()` — collects `NetworkMonitor.isConnected` via `repeatOnLifecycle(STARTED)`

---

## 4. Data Flow

### User Input → Persistence

```
Fragment/Activity
  └─ safeScope.launch { repo.save(entity) }
       └─ DAO.insertOrReplace(entity)       → Room DB (SQLite)
```

### Reference Data → Local DB

```
WorkManager Worker (on network constraint)
  └─ ApiClient.createService<AkilimoApi>(…)
       └─ api.getFertilizers() → Moshi JSON → Fertilizer list
            └─ FertilizerRepo.saveAll(list) → Room DB
```

### Recommendations

```
SummaryFragment / GetRecommendationActivity
  └─ AkilimoApi.computeRecommendations(request)
       └─ Remote: POST /v1/recommendations/compute
            └─ Response → RecommendationsActivity display
```

---

## 5. Locale / i18n System

`AppSettingsDataStore` is the single source of truth for the user's language selection. `SessionManager` has been deleted.

| Component | Role |
|-----------|------|
| `AppSettingsDataStore.languageTag` | Persists BCP-47 language tag in Preferences DataStore (migrated from `"new-akilimo-config"` SharedPrefs) |
| `AppCompatDelegate.setApplicationLocales(LocaleListCompat)` | Applies locale at runtime; recreates activities in-process — no process kill required |
| `AppLocale.desiredLocale` | Set as a secondary step for the AppLocale string-replacement library |
| `LocaleHelper.wrap(context, langTag)` | Applies locale to Activity context via `createConfigurationContext()` during `attachBaseContext()` |

**Application flow on startup:**

```
AkilimoApp.initLocale()
  → appSettings.getLanguageTagSync()       → e.g. "sw-TZ"
  → AppLocale.desiredLocale = …
  → AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("sw-TZ"))

BaseActivity.attachBaseContext(newBase)
  → AppSettingsDataStore.readLanguageTagSync(newBase)   → e.g. "sw-TZ"
  → LocaleHelper.wrap(newBase, "sw-TZ")
       → Locale.Builder().setLanguageTag("sw-TZ").build()
       → config.setLocales(LocaleList(locale))
       → newBase.createConfigurationContext(config)
       → resolves values-sw-rTZ/strings.xml ✓
```

**Runtime language change (WelcomeFragment / UserSettingsActivity):**

```
safeScope.launch {
    appSettings.setLanguageTag(tag)          // write to DataStore first (fixes race condition)
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    AppLocale.desiredLocale = …              // secondary step for Reword library
}
// setApplicationLocales() triggers in-process activity recreation — no ProcessPhoenix needed
```

**Supported locales** (defined in `Locales.kt`):

| Language | Tag | Resource dir |
|----------|-----|-------------|
| English | `en-US` | `values/` (default) |
| Swahili | `sw-TZ` | `values-sw-rTZ/` |
| Kinyarwanda | `rw-RW` | `values-rw-rRW/` |

---

## 6. Navigation

Navigation is purely intent-based — there is no Jetpack NavGraph.

```
HomeStepperActivity (launcher)
  └─ WizardAdapter (ViewPager2) → 11 step fragments (sequential, pendingOnSelected pattern)
       └─ SummaryFragment → startActivity(RecommendationsActivity)
  └─ FAB → startActivity(UserSettingsActivity)

Domain activities launched from recommendations:
  FertilizersActivity, DatesActivity, WeedManagementActivity,
  IcMaizeActivity, IcSweetPotatoActivity, SphActivity, BppActivity, …
```

---

## 7. Background Work

`WorkerScheduler` provides three scheduling modes:

- `scheduleOneTimeWorker<T>()` — unique one-time work with network constraint
- `schedulePeriodicWorker<T>()` — periodic unique work
- `scheduleChainedWorkers<T>()` — two-step chain (first must succeed for second to run)

Workers read from the network API and upsert data into Room using the repository layer.

---

## 8. Network Stack

`ApiClient.createService<T>(context, baseUrl, timeoutSeconds)`:

- **Retrofit 3.0** + **OkHttp 5.2** + **Moshi 1.15** (KSP codegen)
- Interceptors: `SafeNetworkInterceptor`, `RetryInterceptor`, `HttpLoggingInterceptor` (debug only)
- Legacy TLS: custom `SSLContext` using ISRG Root X1 certificate for API <24
- Timeout: 60 seconds (configurable per call site)

Base URLs resolved via `AppConfig`:
1. DataStore override (`AppSettingsDataStore.readEndpointSync()` — `akilimoEndpoint` / `fuelrodEndpoint` keys) if set
2. `BuildConfig.AKILIMO_BASE_URL` / `FUELROD_BASE_URL` as fallback

---

## 9. State Management

| Mechanism | Scope | Usage |
|-----------|-------|-------|
| `AppSettingsDataStore` (Preferences DataStore) | Process lifetime | Language tag, dark mode, tokens, flags, device ID, endpoints — 17 keys total |
| Room entities | Persistent | User profile, preferences, reference data |
| `NetworkMonitor.isConnected: StateFlow<Boolean>` | Process lifetime | Connectivity banner in BaseActivity |
| `safeScope` (`lifecycleScope`) | Activity/Fragment lifecycle | All coroutine launches in UI layer |

`AppSettingsDataStore` is injected via Hilt (`@Inject lateinit var appSettings: AppSettingsDataStore`) in `BaseActivity` and `BaseFragment`. A `protected val sessionManager get() = appSettings` alias exists for backward compatibility. ViewModels exist for key screens (`WelcomeViewModel`, `UserSettingsViewModel`); remaining screens still load directly from repositories.

---

## 10. Known Technical Debt

| Item | Location | Risk | Status |
|------|----------|------|--------|
| `allowMainThreadQueries()` | `AppDatabase.kt` | ANR risk | ✅ Fixed |
| `fallbackToDestructiveMigration()` | `AppDatabase.kt` | Data loss on schema change | ⬜ Open |
| No ViewModel classes (partial) | Most fragments/activities | State lost on configuration change | 🔄 Partial — key screens done |
| API keys hardcoded in source | `app/build.gradle.kts`, `BuildConfig` | Exposed in APK without obfuscation | ✅ Fixed |
| `isMinifyEnabled = false` in release | `app/build.gradle.kts` | No code shrinking or obfuscation | ⬜ Open |
| No Jetpack NavGraph | All | Deep links impossible; navigation untestable | ⬜ Open |
| No DI framework | All | Manual repo instantiation; untestable | ✅ Fixed — Hilt 2.57.1 |
| Dual SharedPrefs locale sources | `SessionManager` + `AppLocale` | Potential sync drift on cold start | ✅ Fixed — DataStore is sole source |

---

## 11. Target Architecture (Post-Compose Migration)

The long-term target replaces the View layer entirely with Jetpack Compose.
The repository, DAO, and network layers are **unchanged** — only the presentation layer
and its wiring change.

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PRESENTATION (Compose + NavGraph)                 │
│  MainActivity (@AndroidEntryPoint, single-Activity host)            │
│    └─ NavHost (AppNavGraph.kt)                                       │
│         ├─ OnboardingGraph  → WelcomeScreen … SummaryScreen         │
│         ├─ RecommendationGraph → RecommendationUseCaseScreen, …     │
│         └─ SettingsGraph   → UserSettingsScreen                      │
│  Each screen: @Composable fun receiving state from @HiltViewModel   │
└──────────────────────────┬──────────────────────────────────────────┘
                            │ StateFlow<UiState> / SharedFlow<NavEvent>
┌──────────────────────────▼──────────────────────────────────────────┐
│                       VIEWMODEL LAYER (Hilt)                         │
│  @HiltViewModel — one per screen                                     │
│  State: UiState data class exposed as StateFlow                      │
│  Navigation: NavEvent sealed interface exposed as SharedFlow         │
│  Injected: repos, AppSettingsDataStore (via Hilt)                   │
└──────────────────────────┬──────────────────────────────────────────┘
                            │ suspend / Flow (unchanged)
┌──────────────────────────▼──────────────────────────────────────────┐
│                  REPOSITORY LAYER (unchanged)                        │
│  All 15 repos remain; constructor-injected via Hilt                  │
└──────────────────────────┬──────────────────────────────────────────┘
                            │
┌──────────────────────────▼──────────────────────────────────────────┐
│  LOCAL (Room)  │  REMOTE (Retrofit/OkHttp/Moshi)  │  DATASTORE      │
│  DB v2+        │  AkilimoApi, FuelrodApi           │  language, prefs│
└─────────────────────────────────────────────────────────────────────┘
```

**Design system in Compose:**

| View system | Compose equivalent |
|------------|-------------------|
| `values/colors.xml` | `ui/theme/AkilimoColors.kt` — `LightColorScheme` / `DarkColorScheme` |
| `values/themes.xml` | `ui/theme/AkilimoTheme.kt` — `MaterialTheme` wrapper |
| `values/type.xml` | `ui/theme/AkilimoTypography.kt` — `AkilimoTypography` |
| `ShapeAppearance.Akilimo.*` | `ui/theme/AkilimoShapes.kt` — `AkilimoShapes` |
| `Widget.Akilimo.Button` | `AkilimoButton.kt` composable (extraLarge = pill shape) |
| `Widget.Akilimo.CardView` | `AkilimoCard.kt` composable (medium = 12dp rounded) |
| `Widget.Akilimo.TextInputLayout` | `AkilimoTextField.kt` composable (small = 8dp rounded) |

See `docs/COMPOSE_MIGRATION.md` for the full migration plan, phase schedule,
screen-by-screen conversion map, and library removal checklist.
