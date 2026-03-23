# AKILIMO Mobile — Implementation Roadmap

---

## Short-Term (0–4 weeks)

These are fixes to active bugs and security issues. All are self-contained with no architectural prerequisites.

| # | Status | Task | Files | Effort | Impact |
|---|--------|------|-------|--------|--------|
| 1 | ✅ Done | **Fix MUN-16: Language persistence** — full BCP-47 tags, DataStore write before `setApplicationLocales()` (race fix), `AppCompatDelegate` in-process locale change replaces ProcessPhoenix | `WelcomeFragment.kt`, `UserSettingsActivity.kt`, `AkilimoApp.kt`, `AppSettingsDataStore.kt` | S | Critical |
| 1a | ✅ Done | **Fix WorkManager initialization** — implement `Configuration.Provider` in `AkilimoApp`; remove `WorkManagerInitializer` from startup | `AkilimoApp.kt`, `AndroidManifest.xml` | S | Critical |
| 2 | ✅ Done | **Fix dark mode** — removed `MODE_NIGHT_NO` override; `AkilimoApp` applies night mode from `SessionManager.darkMode`; fixed StepperLayout bottom bar, welcome card, terms WebView background, and three hardcoded-black icon tints | `BaseActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt`, `UserSettingsActivity.kt`, `activity_home_stepper.xml`, `fragment_welcome.xml`, `fragment_terms.xml`, `ic_*.xml` | S | High |
| 2a | ✅ Done | **Consistent image-tap selection UX** — animated card highlight, 2dp primary stroke border, selectionOverlay + selectionIndicator on tap; fixed `SelectedCassavaMarketRepo.select()` not persisting `yieldId`; fixed `DiffCallback.areItemsTheSame` in both adapters; aligned `MaizePerformanceAdapter` to same pattern | `CassavaYieldAdapter.kt`, `MaizePerformanceAdapter.kt`, `SelectedCassavaMarketRepo.kt`, `item_card_recommendation_image.xml` | S | UX |
| 2b | ✅ Done | **Rebrand theme to AKILIMO/IITA identity** — updated primary green to `#3D7600`, IITA lime-earth secondary `#586200`, AKILIMO logo red tertiary `#B5162A`; tonal warm-green surfaces; pill buttons; 12dp rounded cards; dedicated dark palette tokens; fixed 7 layout hardcoded color violations | `colors.xml`, `themes.xml`, `values-night/themes.xml`, 7 layout files | M | UX |
| 2c | ✅ Done | **Fix dropdown options empty after restart** — `BaseSpinnerAdapter.getFilter()` overridden with no-op filter; `ArrayFilter` was filtering by `toString()` against prefilled text causing empty popups after `ProcessPhoenix` rebirth | `BaseSpinnerAdapter.kt` | S | Critical |
| 2d | ✅ Done | **Network banner in all activities** — programmatic injection in `BaseActivity.injectNetworkBannerIfNeeded()` after `onBindingReady()`; status bar inset applied; theme-aware colors; string resources; safe `Runnable` cancellation | `BaseActivity.kt`, `NetworkNotificationView.kt`, `strings.xml` | S | UX |
| 3 | ✅ Done | **Remove `allowMainThreadQueries()`** — all DB calls confirmed in coroutines; removed from `buildDatabase()` | `AppDatabase.kt` | S | High |
| 4 | ✅ Done | **Move API keys to BuildConfig** — Mapbox runtime key and LocationIQ token sourced from env vars via `BuildConfig`; defaults removed from source | `SessionManager.kt`, `app/build.gradle.kts`, `.env.template` | S | Security |
| 5 | ⬜ | **Enable R8 minification** — `isMinifyEnabled = true` in release build type + baseline proguard rules | `app/build.gradle.kts` | S | Security |

---

## Medium-Term — Compose Prerequisites

Architectural foundations required before screen migration to Compose.
See `docs/COMPOSE_MIGRATION.md §2` for why each one is required.

| # | Status | Task | Files | Effort | Impact | Compose dependency |
|---|--------|------|-------|--------|--------|-------------------|
| 7 | ✅ Done | **Introduce ViewModels** — one per screen; `StateFlow<UiState>` exposed; all DB/DataStore calls moved out of Activity/Fragment; all screens including onboarding wizard done | `ui/activities/`, `ui/fragments/`, `ui/viewmodels/` | M | Architecture | Required — `collectAsStateWithLifecycle()` needs a ViewModel |
| 8 | ✅ Done | **Proper Room migrations** — `fallbackToDestructiveMigration()` replaced with explicit `Migration` objects | `AppDatabase.kt` | M | Data integrity | Required — schema changes during migration must not wipe data |
| 9 | ✅ Done | **Introduce Hilt** — `@HiltAndroidApp` on `AkilimoApp`; `@AndroidEntryPoint` on all concrete Activities/Fragments; `@HiltViewModel` on ViewModels; inject repos via constructor | All | L | Maintainability | Required — `hiltViewModel()` in composables |
| 10 | ✅ Done | **Consolidate ALL settings to DataStore** — `AppSettingsDataStore` covers all 17 keys; `SessionManager.kt` deleted; `SharedPreferencesMigration` applied | `data/AppSettingsDataStore.kt`, `AkilimoApp.kt`, `BaseActivity.kt`, `BaseFragment.kt` | M | Reliability | Required — Compose observes `DataStore` via `collectAsState()` |
| 11 | ⬜ | **Unit test coverage for repos and locale logic** — `UserPreferencesRepo`, `LocaleHelper`, `AppSettingsDataStore`, all ViewModels | `test/` | M | Quality | Required — ViewModel unit tests validate `UiState` transitions |

---

## Long-Term — Compose Migration

Full migration to Jetpack Compose. Executed in six phases as detailed in
`docs/COMPOSE_MIGRATION.md §7`. Items 12–17 are the migration phases.

**Note:** No View-based NavGraph prerequisite — the app goes directly to pure Compose
`NavHost` with `@Serializable` routes from Phase 1. See `COMPOSE_MIGRATION.md §2` for rationale.

| # | Status | Task | Effort | Impact | Branch | Dependency |
|---|--------|------|--------|--------|--------|------------|
| 12 | ✅ Done | **Phase 0 — Enable Compose** — Compose plugin + catalog entries; `AkilimoTheme`, `AkilimoColors`, `AkilimoTypography`, `AkilimoShapes`; shared component primitives (`BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`) | S | Foundation | — | #9, #10 |
| 13 | ✅ Done | **Phase 1 — Navigation Shell** — `MainActivity` with `setContent { AkilimoTheme { AkilimoNavHost() } }`; all routes declared in `navigation/Route.kt`; `MainActivity` set as launcher | S | Architecture | — | #12 |
| 14 | ✅ Done | **Phase 2 — Onboarding Wizard** — `OnboardingViewModel` + `OnboardingScreen` with `AnimatedContent` step transitions; all 11 wizard step composables; `HomeStepperActivity`, `WizardAdapter`, all wizard Fragment classes and XML layouts deleted | L | Core UX | — | #13 |
| 15 | ✅ Done | **Phase 3 — Recommendations & Use-Case Screens** — all use-case and recommendations screens ported to Compose; shared component extractions complete (`RadioButtonRow`, `BinaryToggleChips`, `SwitchRow`, `LabeledTextField`, `SelectionCard`) | XL | Core UX | — | #13 |
| 16 | ✅ Done | **Phase 4 — Settings & Misc** — `UserSettingsScreen` in Compose with `LabeledTextField` inputs, `AkilimoDropdown`, `SwitchRow`; dead nav graphs deleted (`nav_graph.xml`, `nav_recommendations.xml`); `NavRouterFragment` removed | M | UX | — | #15 |
| 17 | ✅ Done | **Phase 5 — Final Cleanup** — `viewBinding = false`; all XML layouts deleted; `BaseFragment`, `BaseStepFragment` deleted; `BaseActivity` thinned; View-only libraries removed (`AppLocale`/`Reword`/`ViewPump`, `hbb20:ccp`, `StepperLayout`, `navigation-fragment`, `navigation-ui-ktx`) | M | Cleanup | — | #16 |
| 18 | ⬜ | **Deep link support** — `<intent-filter>` for recommendation sharing URLs | M | UX | — | #17 |
| 19 | ⬜ | **Offline recommendation cache** — store last recommendation per crop; display stale data when offline | L | Offline UX | — | #8 |
| 20 | ⬜ | **Push notification delivery** — FCM integration for email/SMS notification prefs | L | Product | — | None |
| 21 | ⬜ | **Accessibility audit** — TalkBack, font scaling, content descriptions, Compose semantics | M | Compliance | — | #16 |

---

## Milestone Summary

```
Completed:
  Short-term bug fixes + security hardening (items 1–4)          ✅
  Hilt (#9) + DataStore (#10)                                     ✅
  ViewModels for all screens (#7)                                 ✅
  Room migrations (#8)                                            ✅
  Phase 0: Compose foundation + theme system (#12)                ✅
  Phase 1: MainActivity + NavHost shell (#13)                     ✅
  Phase 2: Onboarding wizard in Compose (#14)                     ✅
  Phase 3: All use-case + recommendation screens (#15)            ✅
  Phase 4: Settings screens (#16)                                 ✅
  Phase 5: Final cleanup — ViewBinding off, XML layouts and
    legacy View libraries removed (#17)                           ✅

Open:
  R8 minification (#5)
  Unit test coverage (#11)
  Deep links + offline cache + push notifications + a11y (18–21)
```

> Full Compose migration specification: `docs/COMPOSE_MIGRATION.md`
