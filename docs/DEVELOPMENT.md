# AKILIMO Mobile Developer Guide

This guide is focused on day-to-day engineering work: local setup, common commands, coding expectations, and safe change practices.

## 1. Local setup checklist

1. Install Android Studio (latest stable).
2. Install/configure JDK 17.
3. Ensure Android SDK 36 and build tools are installed.
4. Configure `MAPBOX_DOWNLOADS_TOKEN` in `~/.gradle/gradle.properties`.
5. Open project in Android Studio and run Gradle sync.

## 2. Common Gradle commands

### Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease bundleRelease
```

### Tests

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

### Quality checks

```bash
./gradlew lintDebug
./gradlew detekt
./gradlew sonar
```

## 3. Runtime configuration

Build-time defaults can be overridden through environment variables:

- `AKILIMO_BASE_URL`
- `FUELROD_BASE_URL`

These become `BuildConfig` fields and are then resolved through `AppConfig` (session value first, then build fallback).

## 4. Code organization conventions

Main package root:

- `ui/` for screens and visual components
- `repos/` for persistence access wrappers
- `dao/` + `entities/` for Room persistence
- `workers/` for background sync orchestration
- `network/` + `rest/` for remote calls and transport models

When adding new features:

1. Prefer adding new flow logic in feature-specific UI/activity packages.
2. Keep networking concerns inside API/service + request/response models.
3. Persist user/reference data through repos + DAOs.
4. Reuse worker abstractions for paged sync work.

## 5. Database and threading note

`AppDatabase` currently uses `allowMainThreadQueries()` as a transitional behavior. Avoid introducing new main-thread DB operations; prefer background execution and coroutine-based data access patterns.

## 6. Recommended pre-PR checklist

Before opening a PR:

1. Run `./gradlew lintDebug detekt testDebugUnitTest`.
2. Verify impacted flow on emulator/device if UI behavior changed.
3. Update docs when behavior/config changes.
4. Add changelog entry when required by release process.

## 7. Release-related references

- Main pipeline: `.github/workflows/android.yml`
- Changelog: `CHANGELOG.md`
- Release note assets: `release/distribution/whatsnew/`
- Script docs: `scripts/README.md`
