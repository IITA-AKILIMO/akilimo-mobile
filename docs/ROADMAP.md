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

## Medium-Term (1–3 months) — Compose Prerequisites

Architectural foundations that **must** be in place before any screen migrates to Compose.
See `docs/COMPOSE_MIGRATION.md §2` for why each one is required.

| # | Task | Files | Effort | Impact | Compose dependency |
|---|------|-------|--------|--------|-------------------|
| 7 | 🔄 Partial | **Introduce ViewModels** — one per screen; expose `StateFlow<UiState>`; move all DB/DataStore calls out of Activity/Fragment. `WelcomeViewModel` and `UserSettingsViewModel` done; remaining screens still load directly from repos | `ui/activities/`, `ui/fragments/`, `ui/viewmodels/` | M | Architecture | Required — `collectAsStateWithLifecycle()` needs a ViewModel |
| 8 | ⬜ | **Proper Room migrations** — replace `fallbackToDestructiveMigration()` with `Migration` objects | `AppDatabase.kt` | M | Data integrity | Required — schema changes during migration must not wipe data |
| 9 | ✅ Done | **Introduce Hilt** — `@HiltAndroidApp` on `AkilimoApp`; `@AndroidEntryPoint` on all 25+ concrete Activities/Fragments; `@HiltViewModel` on ViewModels; inject repos via constructor. Version: `hilt = "2.57.1"`, uses `kapt` | All | L | Maintainability | Required — `hiltViewModel()` in composables |
| 10 | ✅ Done | **Consolidate ALL settings to DataStore** — `AppSettingsDataStore` covers all 17 keys (language, darkMode, akilimoUser, termsAccepted, disclaimerRead, rememberAreaUnit, isFertilizerGrid, deviceToken, apiToken, apiRefreshToken, mapBoxApiKey, locationIqToken, isFirstRun, notificationCount, termsLink, akilimoEndpoint, fuelrodEndpoint); `SessionManager.kt` deleted; `SharedPreferencesMigration` from `"new-akilimo-config"` | `data/AppSettingsDataStore.kt`, `AkilimoApp.kt`, `BaseActivity.kt`, `BaseFragment.kt` | M | Reliability | Required — Compose observes `DataStore` via `collectAsState()` |
| 11 | ⬜ | **Unit test coverage for repos and locale logic** — `UserPreferencesRepo`, `LocaleHelper`, `AppSettingsDataStore`, all ViewModels | `test/` | M | Quality | Required — ViewModel unit tests validate `UiState` transitions |

---

## Long-Term (3–12 months) — Compose Migration

Full migration to Jetpack Compose. Executed in six phases as detailed in
`docs/COMPOSE_MIGRATION.md §7`. Items 12–17 are the migration phases.

**Note:** No View-based NavGraph prerequisite — the app goes directly to pure Compose
`NavHost` with `@Serializable` routes from Phase 1. See `COMPOSE_MIGRATION.md §2` for rationale.

| # | Task | Effort | Impact | Branch | Dependency |
|---|------|--------|--------|--------|------------|
| 12 | **Phase 0 — Enable Compose (1 day)** — uncomment `kotlin-compose` plugin; add missing catalog entries (`navigation-compose`, `hilt-navigation-compose`, lifecycle-compose); set `compose = true` in `build.gradle.kts`; create `AkilimoTheme`, `AkilimoColors`, `AkilimoTypography`, `AkilimoShapes`; verify build passes | S | Foundation | `feature/compose-foundation` | #9 (Hilt), #10 (DataStore) |
| 13 | **Phase 1 — Navigation Shell (1–2 days)** — create `MainActivity` with `setContent { AkilimoTheme { AkilimoNavHost() } }`; declare all routes in `navigation/Route.kt`; create `AkilimoNavHost` with `TODO()` placeholders; set `MainActivity` as launcher in `AndroidManifest.xml`; keep all legacy Activities | S | Architecture | `feature/compose-foundation` | #12 |
| 14 | **Phase 2 — Onboarding Wizard (3–5 days)** — **pattern-setting phase**; create reusable atoms (`AkilimoTextField`, `AkilimoDropdown`, `WizardBottomBar`, `ExitConfirmDialog`); implement `OnboardingViewModel` with full UiState/Event/Effect; implement `OnboardingScreen` with `AnimatedContent` step transitions; migrate all 11 wizard step fragments to composables; wire into NavHost; delete `HomeStepperActivity`, `WizardAdapter`, all wizard Fragment classes, `BaseStepFragment`, and wizard XML layouts | L | Core UX | `feature/compose-onboarding` | #13 |
| 15 | **Phase 3 — Recommendations & Use-Case Screens (1 week)** — work outermost-shell-first: `RecommendationsScreen` → advice sub-screens → use-case forms → data screens → fertilizer screens → `GetRecommendationScreen`; for each: port ViewModel to Event/Effect → create `*Screen.kt` + `*Content` composable → wire into NavHost → delete Fragment/Activity + XML layout | XL | Core UX | `feature/compose-recommendations` | #14 |
| 16 | **Phase 4 — Settings & Misc (2–3 days)** — `UserSettingsScreen`; `LocationPickerScreen` (`AndroidView` wrapper for Mapbox if no Compose SDK available); any remaining dialogs or utility screens | M | UX | `feature/compose-settings` | #15 |
| 17 | **Phase 5 — Final Cleanup (1–2 days)** — `viewBinding = false`; delete all `res/layout/*.xml`; delete `BaseFragment`, `BaseStepFragment`; thin `BaseActivity` to lifecycle/permission helpers only; remove View-only libraries (`AppLocale`/`Reword`/`ViewPump`, `hbb20:ccp`, `StepperLayout`); remove `navigation-fragment` and `navigation-ui-ktx`; delete `nav_graph.xml` and `nav_recommendations.xml`; final build verification | M | Cleanup | `feature/compose-cleanup` | #16 |
| 18 | **Deep link support** — `<intent-filter>` for recommendation sharing URLs | M | UX | — | #17 |
| 19 | **Offline recommendation cache** — store last recommendation per crop; display stale data when offline | L | Offline UX | — | #8 |
| 20 | **Push notification delivery** — FCM integration for email/SMS notification prefs | L | Product | — | None |
| 21 | **Accessibility audit** — TalkBack, font scaling, content descriptions, Compose semantics | M | Compliance | — | #16 |

---

## Milestone Summary

```
Week 1-4:    Bug fixes + security hardening (items 1–5)         ← done ✅
Month 2–3:   Hilt (#9) ✅ + DataStore (#10) ✅ — completed ahead of schedule
             ViewModels (#7) 🔄 Partial — key screens done; remaining screens open
             Room migrations (#8) + unit tests (#11) — still open
Month 4:     Phase 0 + 1: enable Compose, theme system, MainActivity + NavHost shell (#12, #13)
Month 4–5:   Phase 2: onboarding wizard in Compose — pattern-setting phase (#14)
             Note: HomeStepperActivity already migrated StepperLayout → ViewPager2 + WizardAdapter (pre-existing)
Month 5–6:   Phase 3: recommendations + all use-case screens (#15)
Month 7:     Phase 4: settings + misc screens (#16)
Month 7–8:   Phase 5: delete all XML layouts, ViewBinding, legacy libraries (#17)
Month 9–12:  Deep links + offline cache + push notifications + accessibility (items 18–21)
```

> Full Compose migration specification: `docs/COMPOSE_MIGRATION.md`
