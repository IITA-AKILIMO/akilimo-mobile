# AKILIMO Mobile Technical Evaluation and Documentation

## 1) Executive Summary

AKILIMO Mobile is a single-module Android application (`:app`) built with Kotlin, Room, Retrofit/Moshi, and WorkManager. The codebase is organized by technical layers (UI, workers, repositories, DAOs, entities, DTOs), and the overall architecture follows a pragmatic layered approach with background synchronization for reference data. Build and release automation is implemented via GitHub Actions and Gradle.

High-level findings:
- The project has clear package segmentation and domain naming consistency.
- It relies on WorkManager one-time/chained jobs during app startup to hydrate local tables.
- The local Room database is currently configured with `allowMainThreadQueries()`, which is a production-risk hotspot.
- Automated test coverage appears minimal (few sample tests).
- README technical metadata is partially stale versus current Gradle configuration (SDK levels).

---

## 2) Repository and Module Overview

### 2.1 Modules
- Root includes a single Android application module: `:app`.
- Build uses Gradle Kotlin DSL and version catalog (`gradle/libs.versions.toml`).

### 2.2 Code Organization (main Kotlin source)
Under `app/src/main/java/com/akilimo/mobile`, the code is grouped into:
- `ui` (activities, fragments, components, dialogs, use-case screens)
- `workers` + `base/workers` (sync and resilient background orchestration)
- `repos`, `dao`, `entities` (local persistence access)
- `network`, `rest`, `dto` (remote API + transport models)
- `base`, `helper`, `utils`, `enums`, `interfaces`, `config`

This structure is readable for feature evolution and onboarding.

---

## 3) Build, Tooling, and Dependencies

### 3.1 Android/Gradle configuration
- `compileSdk = 36`, `targetSdk = 36`, `minSdk = 21`.
- Java/Kotlin target is 17.
- ViewBinding enabled; Compose explicitly disabled.
- Dynamic versioning writes release tags to `nextrelease.txt` via `computeBuildNumber()`.

### 3.2 Quality tooling
Configured in Gradle:
- Detekt (baseline in module)
- SonarQube (`sonar` task depends on lint + detekt)
- Android lint XML report output for CI ingestion

### 3.3 Core libraries
- UI: AndroidX + Material + Navigation
- Persistence: Room + KSP compiler
- Networking: Retrofit + OkHttp + Moshi
- Observability/services: Firebase Analytics, Sentry
- Background jobs: WorkManager
- Mapping: Mapbox SDK

---

## 4) Runtime Architecture

## 4.1 Application startup
`AkilimoApp` initializes:
1. Analytics and network monitoring.
2. Locale support from persisted preferences.
3. Startup housekeeping.
4. Multiple one-time/chained WorkManager jobs to sync lookup/reference data (investment amounts, cassava prices/units, starch factories, fertilizers and prices).

This startup model ensures local/offline data availability, but can front-load startup network tasks if not throttled by UX gates.

### 4.2 Data layer
- `AppDatabase` defines Room entities, DAOs, and type converters.
- Repository classes wrap DAO access and provide persistence + query helpers.
- Sync workers fetch paginated remote data and upsert locally.

### 4.3 Worker framework
- `SafePagedWorker` encapsulates page-by-page retrieval, cancellation-aware execution, progress reporting, and error packaging.
- `WorkerScheduler` centralizes scheduling patterns: one-time, periodic, and chained workers with network constraints.

This is a strong pattern for consistency across sync jobs.

### 4.4 Network layer
- `ApiClient` creates Retrofit services with Moshi, retry interceptor, and safe network interceptor.
- Legacy SSL compatibility handling exists for pre-Android 7.1 devices by loading embedded certificate material.
- `AppConfig` resolves service base URLs from session overrides first, then build-time defaults.

---

## 5) Functional Surface (from manifest and package topology)

The app declares a wide activity set for:
- Primary app flows and recommendation entry points
- Produce/use-case specific flows (fertilizer, cassava yield/market, weed control, maize performance, tractor/manual tillage, etc.)
- Location and onboarding-related screens

Localization scaffolding and country-specific enums suggest multi-country and multi-language support at the application logic layer.

---

## 6) CI/CD and Release Engineering

GitHub Actions workflow (`.github/workflows/android.yml`) includes:
- Tag/manual trigger build
- Java 21 setup, Android build, signing APK/AAB
- Build artifact caching keyed by source hash and version inputs
- Publish to Google Play (beta/production tracks)
- Upload artifacts/releases and update external build-number service

This indicates a mature release pipeline with distribution automation.

---

## 7) Technical Risks and Gaps

### 7.1 Immediate
1. **Main-thread DB access enabled**
   - `allowMainThreadQueries()` remains in production DB builder.
   - Risk: UI jank/ANRs and non-deterministic latency under load.

2. **Limited automated tests**
   - Very small unit/instrumentation footprint for a feature-rich app.
   - Risk: regressions across complex use-case flows.

3. **Documentation drift**
   - README SDK values differ from current Gradle configuration.
   - Risk: onboarding confusion and environment mismatch.

### 7.2 Medium-term
4. **Architecture labeling mismatch**
   - README states MVVM, but codebase appears primarily Activity/Fragment + repository/worker layering without visible ViewModel usage.

5. **Potential worker scheduling duplication**
   - Some workers are scheduled at startup and can also be triggered in UI flows; idempotency and policy settings should be reviewed per use case.

---

## 8) Recommendations and Prioritized Roadmap

### P0 (next sprint)
1. Remove `allowMainThreadQueries()` and migrate DB operations to coroutine/IO-safe paths end-to-end.
2. Align README technical metadata with actual Gradle config (SDK/toolchain/current architecture notes).
3. Add baseline unit tests for:
   - `RecommendationBuilder` calculations
   - Repository upsert behavior
   - `AppConfig` URL resolution precedence
   - Worker pagination boundary handling

### P1
4. Add instrumentation tests for critical onboarding + recommendation happy paths.
5. Add contract tests (or mocked integration tests) for API DTO parsing and pagination metadata assumptions.
6. Introduce architecture decision records (ADRs) for worker-first synchronization design.

### P2
7. Incremental migration to explicit presentation layer abstractions (e.g., ViewModel/StateFlow) for long-term UI maintainability.
8. Define SLOs/telemetry dashboards for sync success rate, startup latency, and offline-readiness completion.

---

## 9) Developer Onboarding Quick Reference

### 9.1 Build
```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

### 9.2 Test
```bash
./gradlew test
./gradlew connectedAndroidTest
```

### 9.3 Important code entry points
- App startup and global initialization: `AkilimoApp.kt`
- DB schema and DAO wiring: `AppDatabase.kt`
- API client/bootstrap: `network/ApiClient.kt`
- Background orchestration: `workers/WorkScheduler.kt`, `base/workers/SafePagedWorker.kt`
- Environment endpoint routing: `config/AppConfig.kt`

---

## 10) Appendix: Evaluation Notes

This documentation was generated from static code and configuration inspection of the repository, including Gradle configuration, Android manifest, package structure, workers, networking, database, and CI pipeline definitions.
