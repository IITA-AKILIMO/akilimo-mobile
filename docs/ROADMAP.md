# AKILIMO Mobile — Implementation Roadmap

---

## Short-Term (0–4 weeks)

These are fixes to active bugs and security issues. All are self-contained with no architectural prerequisites.

| # | Task | Files | Effort | Impact |
|---|------|-------|--------|--------|
| 1 | **Fix MUN-16: Language persistence** — use full BCP-47 tags, sync AppLocale, use ProcessPhoenix in WelcomeFragment, save to UserPreferences | `WelcomeFragment.kt`, `UserSettingsActivity.kt`, `AkilimoApp.kt`, `SessionManager.kt` | S | Critical |
| 2 | **Fix dark mode** — remove `MODE_NIGHT_NO` override in `BaseActivity.onCreate()`, read `UserPreferences.darkMode` on startup | `BaseActivity.kt` | S | High |
| 3 | **Remove `allowMainThreadQueries()`** — all DB calls are already in coroutines | `AppDatabase.kt` | S | High |
| 4 | **Move API keys to BuildConfig** — Mapbox runtime key and LocationIQ token out of source | `SessionManager.kt`, `app/build.gradle.kts`, `local.properties` | S | Security |
| 5 | **Enable R8 minification** — `isMinifyEnabled = true` in release build type + baseline proguard rules | `app/build.gradle.kts` | S | Security |
| 6 | **Wire NetworkNotificationView** in activities that need it but currently have a null reference | All domain activities | S | UX |

---

## Medium-Term (1–3 months)

Architectural improvements. No single dependency blocks them all — can be parallelized across engineers.

| # | Task | Files | Effort | Impact | Dependency |
|---|------|-------|--------|--------|------------|
| 7 | **Introduce ViewModels** — start with `UserSettingsViewModel` and `WelcomeViewModel` to survive configuration changes | `ui/activities/`, `ui/fragments/` | M | Architecture | None |
| 8 | **Proper Room migrations** — replace `fallbackToDestructiveMigration()` with `Migration` objects | `AppDatabase.kt` | M | Data integrity | None |
| 9 | **Introduce Hilt** — eliminate manual repository instantiation; scope repos and DB properly | All | L | Maintainability | — |
| 10 | **Consolidate language persistence to DataStore** — single source of truth replacing dual SharedPrefs | `SessionManager.kt`, `AkilimoApp.kt`, `BaseActivity.kt` | M | Reliability | #8 |
| 11 | **Unit test coverage for repos and locale logic** — `UserPreferencesRepo`, `LocaleHelper`, `SessionManager` | `test/` | M | Quality | #9 |
| 12 | **Per-screen offline indicators** — collect `NetworkMonitor.isConnected` in each relevant activity | All domain activities | M | UX | None |

---

## Long-Term (3–6 months)

Strategic modernization. High effort; should be planned as dedicated milestones.

| # | Task | Effort | Impact | Dependency |
|---|------|--------|--------|------------|
| 13 | **Jetpack NavGraph** — replace all intent-based navigation with a type-safe navigation graph | L | Architecture | #9 (Hilt) |
| 14 | **ViewModel-driven stepper** — replace StepperAdapter + fragment state with ViewModel + SavedStateHandle | L | Robustness | #7, #13 |
| 15 | **Jetpack Compose** for new screens — settings, recommendations display | L | UX modernization | #9, #13 |
| 16 | **Deep link support** — `<intent-filter>` for recommendation sharing URLs | M | UX | #13 |
| 17 | **Offline recommendation cache** — store last recommendation per crop; display when offline | L | Offline UX | #8 |
| 18 | **Push notification delivery** — implement FCM integration for email/SMS notification prefs | L | Product | None |
| 19 | **Accessibility audit** — TalkBack, font scaling, content descriptions | M | Compliance | None |

---

## Milestone Summary

```
Week 1-4:   MUN-16 fix + dark mode + security hardening (items 1–6)
Month 2:    ViewModels + Room migrations + Hilt setup (items 7–9)
Month 3:    DataStore migration + unit tests + offline UX (items 10–12)
Month 4-6:  NavGraph + Compose + deep links + offline cache (items 13–17)
```
