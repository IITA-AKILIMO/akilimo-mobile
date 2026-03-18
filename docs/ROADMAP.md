# AKILIMO Mobile — Implementation Roadmap

---

## Short-Term (0–4 weeks)

These are fixes to active bugs and security issues. All are self-contained with no architectural prerequisites.

| # | Status | Task | Files | Effort | Impact |
|---|--------|------|-------|--------|--------|
| 1 | ✅ Done | **Fix MUN-16: Language persistence** — full BCP-47 tags, AppLocale sync, ProcessPhoenix in WelcomeFragment, save to UserPreferences | `WelcomeFragment.kt`, `UserSettingsActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt` | S | Critical |
| 1a | ✅ Done | **Fix WorkManager initialization** — implement `Configuration.Provider` in `AkilimoApp`; remove `WorkManagerInitializer` from startup | `AkilimoApp.kt`, `AndroidManifest.xml` | S | Critical |
| 2 | ✅ Done | **Fix dark mode** — removed `MODE_NIGHT_NO` override; `AkilimoApp` applies night mode from `SessionManager.darkMode`; fixed StepperLayout bottom bar, welcome card, terms WebView background, and three hardcoded-black icon tints | `BaseActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt`, `UserSettingsActivity.kt`, `activity_home_stepper.xml`, `fragment_welcome.xml`, `fragment_terms.xml`, `ic_*.xml` | S | High |
| 2a | ✅ Done | **Consistent image-tap selection UX** — animated card highlight, 2dp primary stroke border, selectionOverlay + selectionIndicator on tap; fixed `SelectedCassavaMarketRepo.select()` not persisting `yieldId`; fixed `DiffCallback.areItemsTheSame` in both adapters; aligned `MaizePerformanceAdapter` to same pattern | `CassavaYieldAdapter.kt`, `MaizePerformanceAdapter.kt`, `SelectedCassavaMarketRepo.kt`, `item_card_recommendation_image.xml` | S | UX |
| 2b | ✅ Done | **Rebrand theme to AKILIMO/IITA identity** — updated primary green to `#3D7600`, IITA lime-earth secondary `#586200`, AKILIMO logo red tertiary `#B5162A`; tonal warm-green surfaces; pill buttons; 12dp rounded cards; dedicated dark palette tokens; fixed 7 layout hardcoded color violations | `colors.xml`, `themes.xml`, `values-night/themes.xml`, 7 layout files | M | UX |
| 2c | ✅ Done | **Fix dropdown options empty after restart** — `BaseSpinnerAdapter.getFilter()` overridden with no-op filter; `ArrayFilter` was filtering by `toString()` against prefilled text causing empty popups after `ProcessPhoenix` rebirth | `BaseSpinnerAdapter.kt` | S | Critical |
| 2d | ✅ Done | **Network banner in all activities** — programmatic injection in `BaseActivity.injectNetworkBannerIfNeeded()` after `onBindingReady()`; status bar inset applied; theme-aware colors; string resources; safe `Runnable` cancellation | `BaseActivity.kt`, `NetworkNotificationView.kt`, `strings.xml` | S | UX |
| 3 | ✅ Done | **Remove `allowMainThreadQueries()`** — all DB calls confirmed in coroutines; removed from `buildDatabase()` | `AppDatabase.kt` | S | High |
| 4 | ⬜ | **Move API keys to BuildConfig** — Mapbox runtime key and LocationIQ token out of source | `SessionManager.kt`, `app/build.gradle.kts`, `local.properties` | S | Security |
| 5 | ⬜ | **Enable R8 minification** — `isMinifyEnabled = true` in release build type + baseline proguard rules | `app/build.gradle.kts` | S | Security |

---

## Medium-Term (1–3 months) — Compose Prerequisites

Architectural foundations that **must** be in place before any screen migrates to Compose.
See `docs/COMPOSE_MIGRATION.md §2` for why each one is required.

| # | Task | Files | Effort | Impact | Compose dependency |
|---|------|-------|--------|--------|-------------------|
| 7 | **Introduce ViewModels** — one per screen; expose `StateFlow<UiState>`; move all DB/SharedPrefs calls out of Activity/Fragment | `ui/activities/`, `ui/fragments/` | M | Architecture | Required — `collectAsStateWithLifecycle()` needs a ViewModel |
| 8 | **Proper Room migrations** — replace `fallbackToDestructiveMigration()` with `Migration` objects | `AppDatabase.kt` | M | Data integrity | Required — schema changes during migration must not wipe data |
| 9 | **Introduce Hilt** — `@HiltAndroidApp` on `AkilimoApp`; `@AndroidEntryPoint` on all activities; `@HiltViewModel` on all ViewModels; inject repos via constructor | All | L | Maintainability | Required — `hiltViewModel()` in composables |
| 10 | **Consolidate language + dark mode to DataStore** — single reactive source replaces dual SharedPrefs; expose as `Flow<String>` and `Flow<Boolean>` | `SessionManager.kt`, `AkilimoApp.kt`, `BaseActivity.kt` | M | Reliability | Required — Compose observes `DataStore` via `collectAsState()` |
| 11 | **Unit test coverage for repos and locale logic** — `UserPreferencesRepo`, `LocaleHelper`, `SessionManager`, all ViewModels | `test/` | M | Quality | Required — ViewModel unit tests validate `UiState` transitions |
| 12 | **Jetpack NavGraph (View-based first)** — replace all `startActivity()` / `Intent` navigation with typed `NavGraph` destinations; enables `navigation-compose` in Phase 5 | All activities | L | Architecture | Required — `NavHost` is the Compose navigation backbone |

---

## Long-Term (3–12 months) — Compose Migration

Full migration to Jetpack Compose. Executed in five phases as detailed in
`docs/COMPOSE_MIGRATION.md`. Items 13–17 are internal sub-phases of the migration.

| # | Task | Effort | Impact | Dependency |
|---|------|--------|--------|------------|
| 13 | **Phase 1 — Compose infrastructure** — enable Compose in build; create `AkilimoTheme`, `AkilimoColors`, `AkilimoTypography`, `AkilimoShapes`; create reusable atom composables (`AkilimoButton`, `AkilimoCard`, `AkilimoTextField`, `NetworkBanner`) | M | Foundation | #9 (Hilt), #12 (NavGraph) |
| 14 | **Phase 2 — Leaf screens** — migrate `UserSettingsActivity`, `RecommendationsActivity`, `GetRecommendationActivity`, `MainActivity` to Compose via `ComposeView` bridge host; each gets a `*ViewModel` + `*Screen.kt` | L | UX modernisation | #13 |
| 15 | **Phase 3 — Domain screens** — migrate all 15 domain activities to Compose; replace all RecyclerView adapters with `LazyColumn` / `LazyVerticalGrid`; replace `AutoCompleteTextView` with `ExposedDropdownMenuBox` | XL | Core UX | #14 |
| 16 | **Phase 4 — Stepper replacement** — replace `StepperLayout` library with `HorizontalPager`-based `OnboardingFlow`; migrate all 11 step fragments to composables; introduce `OnboardingViewModel` with step validation | XL | Architecture | #15 |
| 17 | **Phase 5 — NavGraph migration and final cleanup** — consolidate all Compose destinations into `AppNavGraph`; remove ViewBinding, all XML layouts, all legacy View libraries (`StepperLayout`, `ViewPump`, `Reword`, `AppLocale`, `ProcessPhoenix`) | L | Architecture | #16 |
| 18 | **Deep link support** — `<intent-filter>` for recommendation sharing URLs | M | UX | #17 |
| 19 | **Offline recommendation cache** — store last recommendation per crop; display stale data when offline | L | Offline UX | #8 |
| 20 | **Push notification delivery** — FCM integration for email/SMS notification prefs | L | Product | None |
| 21 | **Accessibility audit** — TalkBack, font scaling, content descriptions, Compose semantics | M | Compliance | #16 |

---

## Milestone Summary

```
Week 1-4:    Bug fixes + security hardening (items 1–5)         ← mostly done ✅
Month 2:     ViewModels + Room migrations + Hilt (items 7–9)
Month 3:     DataStore + NavGraph + unit tests (items 10–12)
Month 4:     Compose infrastructure + leaf screens (items 13–14)
Month 5–6:   Domain screens migration (item 15)
Month 7–8:   Stepper replacement + onboarding flow (item 16)
Month 9–10:  NavGraph consolidation + library cleanup (item 17)
Month 11–12: Deep links + offline cache + accessibility (items 18–21)
```

> Full Compose migration specification: `docs/COMPOSE_MIGRATION.md`
