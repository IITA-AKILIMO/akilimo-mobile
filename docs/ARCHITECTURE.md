# AKILIMO Mobile Architecture Notes

This document describes the current practical architecture implemented in the Android codebase.

## 1. High-level architecture

The app follows a layered Android architecture centered around:

- **UI layer**: activities/fragments/dialogs/components
- **Domain/persistence layer**: repositories over Room DAOs/entities
- **Integration layer**: Retrofit APIs + request/response mapping
- **Sync layer**: WorkManager workers for background hydration/synchronization

## 2. Startup behavior

`AkilimoApp` performs early app initialization:

1. Starts network monitoring.
2. Initializes locale support.
3. Runs startup housekeeping.
4. Schedules one-time/chained workers for reference data sync.

This pattern supports offline-first workflows by hydrating local data soon after launch.

## 3. Data flow patterns

Typical read/write pattern:

1. UI captures user input.
2. Repository persists/retrieves local entities through DAO.
3. Network calls are made through API service wrappers when needed.
4. Workers refresh reference data in background and upsert into local DB.

## 4. Background work

Work orchestration is centralized in `WorkerScheduler`:

- One-time unique work
- Periodic unique work
- Chained two-step work

Default constraints require network connectivity.

## 5. Network stack

`ApiClient` configures:

- Moshi serialization (including `LocalDateAdapter`)
- OkHttp with retry/network interceptors
- Conditional debug logging interceptor
- Legacy TLS compatibility handling for older Android versions

## 6. Configuration resolution

Service base URLs are resolved via `AppConfig` using priority:

1. Session override (if present)
2. BuildConfig default from Gradle/environment

This allows controlled runtime endpoint switching while preserving safe defaults.

## 7. Known technical debt hotspots

- Room currently allows main-thread queries (`allowMainThreadQueries()`), which should be removed during DB threading hardening.
- Automated tests are currently limited relative to feature surface.
- Some legacy and modern patterns coexist; progressive standardization is recommended per touched feature.
