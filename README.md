# AKILIMO Mobile

[![Android CI](https://github.com/IITA-AKILIMO/akilimo-mobile/actions/workflows/android.yml/badge.svg)](https://github.com/IITA-AKILIMO/akilimo-mobile/actions/workflows/android.yml)

AKILIMO Mobile is an Android application for delivering site-specific agronomic recommendations to farmers and extension agents. The app combines location-aware data collection, crop/use-case workflows, and remote recommendation APIs with offline-friendly local storage.

---

## Table of Contents

- [What this app does](#what-this-app-does)
- [Current technical baseline](#current-technical-baseline)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
- [Configuration and secrets](#configuration-and-secrets)
- [Build and run](#build-and-run)
- [Testing and quality checks](#testing-and-quality-checks)
- [Release workflow](#release-workflow)
- [Troubleshooting](#troubleshooting)
- [Documentation index](#documentation-index)

---

## What this app does

AKILIMO Mobile supports agronomy decision workflows such as:

- Fertilizer recommendation flows (including intercropping variants)
- Produce market and yield-related flows (cassava, maize, sweet potato)
- Tillage and weed-management related cost flows
- Location-aware recommendations via on-device location + map-assisted picking
- Multi-language UX support (English, Swahili/sw-TZ, Kinyarwanda/rw-RW)
- Background synchronization of reference datasets (e.g., fertilizer catalog/prices, market data)

From current source configuration, the app includes country support for:

- Nigeria (`NG`)
- Tanzania (`TZ`)
- Ghana (`GH`)
- Rwanda (`RW`)
- Burundi (`BI`)

---

## Current technical baseline

- **Platform**: Android app (`:app` single-module project)
- **Language**: Kotlin
- **Build system**: Gradle (Kotlin DSL + Version Catalog)
- **JDK target**: 17
- **Android SDKs**:
  - `minSdk = 21`
  - `targetSdk = 36`
  - `compileSdk = 36`
- **UI**: XML views + ViewBinding (Compose currently disabled)
- **Local persistence**: Room
- **Background jobs**: WorkManager
- **Networking**: Retrofit + OkHttp + Moshi
- **Observability/analytics**: Sentry + Firebase Analytics
- **Mapping/geospatial integrations**: Mapbox + Location services
- **Quality tooling**: Android Lint, Detekt, SonarQube

---

## Project structure

```text
.
├── app/                         # Android application module
│   ├── src/main/java/com/akilimo/mobile/
│   │   ├── ui/                  # Activities, fragments, dialogs, reusable UI components
│   │   ├── workers/             # Domain sync/background workers and scheduling
│   │   ├── base/                # Base classes and worker abstractions
│   │   ├── repos/               # Repository layer over DAOs / local entities
│   │   ├── dao/, entities/      # Room DAOs and entity models
│   │   ├── network/, rest/      # API clients, DTOs, request/response models
│   │   └── config/              # Runtime configuration helpers
│   └── src/main/res/            # Android resources (layouts, strings, themes, drawables)
├── docs/                        # Project documentation
├── scripts/                     # Utility scripts (release notes generation)
├── release/distribution/        # WhatsNew assets for Play Store release notes
└── .github/workflows/           # CI/CD pipelines
```

---

## Getting started

### Prerequisites

- **Android Studio**: latest stable recommended
- **JDK**: 17
- **Android SDK**: API 36 platform + build tools
- **Git**

### Clone

```bash
git clone https://github.com/IITA-AKILIMO/akilimo-mobile.git
cd akilimo-mobile
```

### Open in Android Studio

1. Open the project root in Android Studio.
2. Let Gradle sync.
3. Ensure the IDE is using JDK 17.

---

## Configuration and secrets

This project relies on a combination of Gradle properties, environment variables, and service JSON files.

### 1) Gradle properties

The build expects `MAPBOX_DOWNLOADS_TOKEN` for Mapbox Maven dependency resolution.

Recommended approach:

- Put secrets in `~/.gradle/gradle.properties` (preferred for local development), not in committed files.

```properties
MAPBOX_DOWNLOADS_TOKEN=<your-mapbox-download-token>
```

### 2) BuildConfig base URLs (optional overrides)

`app/build.gradle.kts` reads optional environment variables:

- `AKILIMO_BASE_URL`
- `FUELROD_BASE_URL`

If unset, default values are used.

Example local override for a shell session:

```bash
export AKILIMO_BASE_URL="https://api.akilimo.org"
export FUELROD_BASE_URL="https://akilimo.fuelrod.com"
```

### 3) Firebase config

The app uses `app/google-services.json` for Firebase services.

If using a different Firebase project, replace with your environment-appropriate config.

### 4) Signing credentials (release builds)

CI handles release signing via GitHub secrets. For local release signing, configure a local keystore and matching signing settings/workflow inputs.

---

## Build and run

### Debug build

```bash
./gradlew assembleDebug
```

### Install to connected device/emulator

```bash
./gradlew installDebug
```

### Release artifacts (unsigned/signed depending on env)

```bash
./gradlew assembleRelease bundleRelease
```

---

## Testing and quality checks

### Unit tests

```bash
./gradlew testDebugUnitTest
```

### Instrumented tests (requires emulator/device)

```bash
./gradlew connectedDebugAndroidTest
```

### Lint + static analysis

```bash
./gradlew lintDebug detekt
```

### Sonar task (depends on lint + detekt per Gradle setup)

```bash
./gradlew sonar
```

---

## Release workflow

The GitHub Actions workflow in `.github/workflows/android.yml` covers:

1. Version/tag derivation
2. Build APK/AAB
3. Signing
4. Artifact packaging
5. Play Store publish (beta/production track logic)
6. Post-publish build number update

Release note source files are under:

- `release/distribution/whatsnew/`

Changelog history is maintained in:

- `CHANGELOG.md`

For script details, see `scripts/README.md`.

---

## Troubleshooting

### Gradle cannot resolve Mapbox artifacts

- Ensure `MAPBOX_DOWNLOADS_TOKEN` is set in a Gradle properties file available to Gradle.

### Build runs with wrong Java version

- Confirm Android Studio and/or `JAVA_HOME` points to JDK 17.

### API endpoint confusion in debug builds

- Verify environment variables for `AKILIMO_BASE_URL` and `FUELROD_BASE_URL`.
- Check runtime configuration behavior in `AppConfig` and session overrides.

### Slow UI operations involving DB

- The current Room setup still allows main-thread queries for compatibility during migration phases. Prefer refactoring data access paths toward background/coroutine execution where possible.

---

## Documentation index

- [Developer Guide](docs/DEVELOPMENT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Product Requirements](docs/PRD.md)
- [Implementation Roadmap](docs/ROADMAP.md)
- [Compose Migration Guide](docs/COMPOSE_MIGRATION.md)
- [User Guide](docs/USER_GUIDE.md)
- [Technical Evaluation](docs/TECHNICAL_EVALUATION.md)
- [Release Script Notes](scripts/README.md)
- [Changelog](CHANGELOG.md)

---

## Maintainers and organization

This project is maintained by/for AKILIMO under IITA initiatives.

- IITA: <https://www.iita.org/>
- Repository: <https://github.com/IITA-AKILIMO/akilimo-mobile>
