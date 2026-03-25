# Package Structure Improvement Plan

Analysis date: 2026-03-25
Branch: `fix/cleanup`

This document captures every concrete structural issue found in the package layout and a
phased execution plan to resolve them. Issues are grouped by risk and execution order —
complete each phase before starting the next; each phase leaves the codebase in a
compilable, green-CI state.

---

## Problem Summary

| # | Package(s) affected | Issue | Risk |
|---|---------------------|-------|------|
| 1 | `dao/` | Two repos (`MaizePerformanceRepo`, `ProduceMarketRepo`) live in the DAO package | Low |
| 2 | `wizard/` | Entire package is one enum (`OnboardingSection`) — wrong location | Low |
| 3 | `ui/viewmodels/usecases/` | `FertilizerViewModel` is the only ViewModel in a sub-package; all others are flat | Low |
| 4 | `interfaces/` | `ILabelProvider` and `IProduceType` belong in `enums/`; `IDispatcherProvider` belongs in `base/workers/` | Low |
| 5 | `base/` | `BaseEntity.kt` is isolated at root of `base/`; belongs in `entities/` or `database/` | Low |
| 6 | `helper/` | Three unrelated files: `LocaleHelper` → `data/`, `WorkStateMapper` → `workers/`, `WorkerError` → `workers/` | Low–Med |
| 7 | `extensions/` | Single file (`WorkerExtensions`) tightly coupled to `helper/WorkStatus`; merge into `workers/` | Low |
| 8 | `utils/` | Mixed tiers: service-layer classes, DB infrastructure, and business logic mixed with true utilities | Medium |
| 9 | `rest/` + `network/` + `dto/` | Remote-data layer split across three packages; should converge toward `network/` | High |

---

## Target Package Structure

```
com.akilimo.mobile/
├── ui/
│   ├── activities/              MainActivity only
│   ├── screens/
│   │   ├── onboarding/          + OnboardingSection.kt (moved from wizard/)
│   │   ├── recommendations/
│   │   ├── settings/
│   │   └── usecases/
│   ├── viewmodels/              ALL ViewModels flat (FertilizerViewModel moved here)
│   ├── components/compose/
│   └── theme/
├── navigation/
├── repos/                       ALL 17 repos (MaizePerformanceRepo + ProduceMarketRepo moved here)
├── dao/                         Pure Room @Dao interfaces only
├── entities/                    Room @Entity classes + BaseEntity (moved from base/)
├── database/                    AppDatabase, DatabaseMigrations, Converters (moved from utils/)
├── data/                        AppSettingsDataStore, LocaleHelper (moved from helper/)
├── network/
│   ├── ApiClient, AkilimoApi, LocationIqApi, WeatherApi, NetworkMonitor, RetryInterceptor
│   ├── GeocodingService.kt      (moved from utils/)
│   └── WeatherService.kt        (moved from utils/)
├── rest/request/, rest/response/ → network/request/, network/response/  [Phase 4]
├── dto/                         UI/domain DTOs only after Phase 4 split
│                                (AdviceCompletionDto, Options, OptionEntries)
├── enums/                       + ILabelProvider.kt, IProduceType.kt (moved from interfaces/)
├── workers/
│   ├── 6 concrete workers + WorkerScheduler
│   ├── WorkStateMapper.kt       (moved from helper/)
│   ├── WorkerError.kt           (moved from helper/)
│   └── WorkerExtensions.kt      (moved from extensions/)
├── base/workers/                NetworkAwareWorker, BaseApiWorker, SafePagedWorker
│                                + DispatcherProvider (moved from interfaces/)
├── di/
├── config/
└── utils/                       True utilities only: DateHelper, MathHelper, NumberHelper,
                                 StringHelper, PermissionHelper, StartupManager
                                 AnimationHelper.kt — DELETE if no Compose callers
```

---

## Phase 1 — Misplaced files, zero logic change

**Goal**: Fix obvious wrong-package placements. Every change is a file move + package
declaration update + import fix. No logic changes.

### Task 1.1 — Move repos out of `dao/`
- Move `dao/MaizePerformanceRepo.kt` → `repos/MaizePerformanceRepo.kt`
- Move `dao/ProduceMarketRepo.kt` → `repos/ProduceMarketRepo.kt`
- Update `package` declaration in both files
- Update imports in: `di/AppModule.kt`, `utils/RecommendationBuilder.kt`

### Task 1.2 — Dissolve `wizard/` package
- Move `wizard/OnboardingSection.kt` → `ui/screens/onboarding/OnboardingSection.kt`
- Update `package` declaration
- Update imports in: `ui/viewmodels/OnboardingViewModel.kt`, `ui/screens/onboarding/OnboardingScreen.kt`
- Delete empty `wizard/` directory

### Task 1.3 — Flatten `FertilizerViewModel`
- Move `ui/viewmodels/usecases/FertilizerViewModel.kt` → `ui/viewmodels/FertilizerViewModel.kt`
- Update `package` declaration
- Update imports in: screen file(s) that call `hiltViewModel<FertilizerViewModel>()`
- Delete empty `ui/viewmodels/usecases/` directory

### Task 1.4 — Dissolve `interfaces/` package
- Move `interfaces/ILabelProvider.kt` → `enums/ILabelProvider.kt`
- Move `interfaces/IProduceType.kt` → `enums/IProduceType.kt`
- Move `interfaces/IDispatcherProvider.kt` (+ `DefaultDispatcherProvider`) → `base/workers/DispatcherProvider.kt`
- Update `package` declarations
- Update imports in: all enums implementing the interfaces (`EnumAdviceTask`, `EnumInvestmentPref`, `EnumUnitOfSale`, `EnumCassavaProduceType`, `EnumProduceType`), `base/workers/BaseApiWorker.kt`
- Delete empty `interfaces/` directory

### Task 1.5 — Move `BaseEntity` to `entities/`
- Move `base/BaseEntity.kt` → `entities/BaseEntity.kt`
- Update `package` declaration
- Update imports in: all 17 entity classes that extend it
- If `base/` is now empty (only `base/workers/` remains), this is structurally fine — leave `base/workers/` as-is

---

## Phase 2 — Worker package cleanup

**Goal**: Consolidate all worker-domain code into `workers/`; eliminate `helper/` and
`extensions/`.

### Task 2.1 — Move worker support files into `workers/`
- Move `helper/WorkStateMapper.kt` (+ `WorkStatus` sealed class) → `workers/WorkStateMapper.kt`
- Move `helper/WorkerError.kt` → `workers/WorkerError.kt`
- Move `extensions/WorkerExtensions.kt` → `workers/WorkerExtensions.kt`
- Update `package` declarations in all three files
- Update imports in: any ViewModel or screen that uses `WorkStatus`, any file that calls `WorkerError`
- `WorkerExtensions.kt` imports `WorkStatus` from `helper/` — update to `workers/`

### Task 2.2 — Move `LocaleHelper` to `data/`
- Move `helper/LocaleHelper.kt` → `data/LocaleHelper.kt`
- Update `package` declaration
- Update imports in: `AkilimoApp.kt`, `MainActivity.kt`, or wherever locale context is wrapped

### Task 2.3 — Delete empty `helper/` and `extensions/`
- Verify both directories are empty after 2.1 and 2.2
- Delete both directories

---

## Phase 3 — `utils/` decomposition

**Goal**: `utils/` should contain only true stateless utilities. Extract everything else to
its correct home.

### Task 3.1 — Move `Converters.kt` to `database/`
- Move `utils/Converters.kt` → `database/Converters.kt`
- Update `package` declaration
- Update the `@TypeConverters(...)` annotation in `database/AppDatabase.kt` to import from `database/`

### Task 3.2 — Move service-layer classes to `network/`
- Move `utils/GeocodingService.kt` → `network/GeocodingService.kt`
- Move `utils/WeatherService.kt` → `network/WeatherService.kt`
- Update `package` declarations
- Update imports in: `LocationPickerViewModel.kt`, any ViewModel that injects these services

### Task 3.3 — Move `LocationHelper` out of `utils/`
- Move `utils/LocationHelper.kt` → `network/LocationHelper.kt` (or new `location/LocationHelper.kt`)
- Update `package` declaration
- Update imports in all callers

### Task 3.4 — Move `RecommendationBuilder` to `repos/`
- Move `utils/RecommendationBuilder.kt` → `repos/RecommendationBuilder.kt`
- Update `package` declaration
- Update imports in: `GetRecommendationViewModel.kt`

### Task 3.5 — Audit and delete `AnimationHelper`
- Grep for any import of `AnimationHelper` across the codebase
- If zero callers: delete `utils/AnimationHelper.kt`
- If callers exist: assess whether the View-system animation code is still needed in the 100% Compose codebase; likely dead

---

## Phase 4 — Remote-data layer consolidation *(do last)*

**Goal**: Merge `rest/` into `network/` and split `dto/` into API-layer vs UI-layer objects,
fulfilling the CLAUDE.md guardrail: *"New remote code should converge toward one remote-data
package path."*

### Task 4.1 — Migrate `rest/request/` → `network/request/`
- Move all files in `rest/request/` to `network/request/`
- Update `package` declarations
- Update imports in: `AkilimoApi.kt`, `RecommendationBuilder.kt`, all workers that build requests

### Task 4.2 — Migrate `rest/response/` → `network/response/`
- Move all files in `rest/response/` to `network/response/`
- Update `package` declarations
- Update imports in: `LocationIqApi.kt`, `WeatherApi.kt`, `GeocodingService.kt`, `WeatherService.kt`

### Task 4.3 — Split `dto/` into API-layer and UI-layer
- API-layer DTOs (network envelopes with `.toEntity()` methods):
  `Fertilizers.kt`, `CassavaPrices.kt`, `CassavaUnits.kt`, `FertilizerPrices.kt`,
  `InvestmentAmounts.kt`, `MaizePrices.kt`, `StarchFactories.kt`, `Pagination.kt`,
  `RecommendationResponse.kt`, `UserFeedBackRequest.kt`
  → Move to `network/dto/`
- UI/domain DTOs (no network dependency):
  `AdviceCompletionDto.kt`, `Options.kt`, `OptionEntries.kt`
  → Keep in `dto/` (now clean)
- Update `package` declarations and all import sites (workers, repos, ViewModels, API interfaces)

### Task 4.4 — Delete `rest/` directory
- Verify `rest/` is empty after 4.1 and 4.2
- Delete directory

---

## Execution notes

1. **Always run `./gradlew :app:compileDebugKotlin` after each task** before moving to the next.
2. **One PR per phase** — keeps diffs reviewable.
3. Phase 4 touches the most files; run `./gradlew testDebugUnitTest` before and after.
4. Android Studio's **Refactor → Move** handles package declaration + import rewrites
   automatically for most moves — use it to reduce manual edit risk.
