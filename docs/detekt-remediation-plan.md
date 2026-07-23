# Detekt Remediation Plan

> Source: `app/build/reports/detekt/detekt.xml`  
> Detekt version: 1.23.8  
> Generated: 2026-05-08

---

## Issue Inventory

| # | Rule | Count | Severity | Batch |
|---|------|-------|----------|-------|
| 1 | `FunctionNaming` | ~50 | warning | A (config) |
| 2 | `LongParameterList` | 13 | warning | A (config) |
| 3 | `LongMethod` | 25 | warning | A (config) + J (deferred) |
| 4 | `CyclomaticComplexMethod` | 12 | warning | A (config) + J (deferred) |
| 5 | `TooManyFunctions` | 1 | warning | A (config) |
| 6 | `MagicNumber` | ~60 | warning | A (config) + I (constants) |
| 7 | `NewLineAtEndOfFile` | 22 | warning | D |
| 8 | `MatchingDeclarationName` | 6 | warning | E |
| 9 | `UnusedPrivateProperty` | 3 | warning | B |
| 10 | `UnusedPrivateMember` | 3 | warning | B |
| 11 | `ForbiddenComment` | 1 | warning | C |
| 12 | `ForEachOnRange` | 1 | warning | F |
| 13 | `VariableNaming` | 1 | warning | F |
| 14 | `ImplicitDefaultLocale` | 3 | warning | F |
| 15 | `TooGenericExceptionCaught` | 10 | warning | A (config) + G |
| 16 | `SwallowedException` | 3 | warning | G |
| 17 | `ReturnCount` | 1 | warning | G |
| 18 | `MaxLineLength` | 10 | warning | H |

---

## Batch A — Detekt Configuration (Compose false-positive suppression)

### Root cause
Jetpack Compose mandates PascalCase `@Composable` function names, many parameters per
composable (with defaults), and composable functions that are inherently long as UI
declarative blocks. These all conflict with Detekt's default thresholds which target
non-UI Kotlin code. Additionally, `AppModule` is a Hilt DI object that legitimately
accumulates `@Provides` methods beyond the object function threshold.

### Files
- CREATE `app/detekt.yml`
- MODIFY `app/build.gradle.kts` — register config file

### Rules addressed
| Rule | Config change | Justification |
|------|--------------|---------------|
| `FunctionNaming` | `ignoreAnnotated: ['Composable']` | Compose convention is PascalCase; any other casing would break tooling |
| `LongParameterList` | `ignoreAnnotated: ['Composable']` | Composables use default-parameter patterns instead of builder/data-class patterns |
| `LongMethod` | `ignoreAnnotated: ['Composable']` | Compose UI is declarative markup — indentation-heavy but not complex; splitting every screen into tiny private composables adds noise |
| `CyclomaticComplexMethod` | `ignoreAnnotated: ['Composable']` | Same rationale; branch complexity in Compose is driven by conditional rendering not logic |
| `TooManyFunctions` in objects | `thresholdInObjects: 15` | Hilt `@Module` objects require one `@Provides` per binding; restructuring for count is counter-productive |
| `MagicNumber` | `excludes` for `AkilimoColors.kt` and `DatabaseMigrations.kt` | Color hex literals are self-documenting (they ARE the color definition); DB version pairs in `Migration(X, Y)` are meaningful inline |
| `TooGenericExceptionCaught` | `excludes` for specific service/ViewModel files | Network/flow services must catch all exceptions to emit error results safely; specific exclusions documented per file |

### Risks
- `ignoreAnnotated: ['Composable']` could hide genuine complexity in composables. The ViewModel
  long-method / cyclomatic findings are **not** annotated with `@Composable` and so remain visible.
- Increasing `thresholdInObjects` should not be raised beyond 15 to avoid hiding non-DI bloat.

### Verification
- [ ] `./gradlew detekt` — no false-positive FunctionNaming / LongMethod on composables
- [ ] ViewModel long-method findings still reported (OnboardingViewModel)

---

## Batch B — Remove Unused Code

### Rule: `UnusedPrivateProperty`, `UnusedPrivateMember`

| File | Symbol | Disposition |
|------|--------|-------------|
| `repos/RecommendationBuilder.kt:41` | `investmentRepo` | DELETE — was injected for a use case that now reads from `selectedInvestmentRepo` |
| `repos/RecommendationBuilder.kt:42` | `cassavaYieldRepo` | DELETE — yield data is now read via `cassavaMarketInfo.cassavaYield` |
| `network/LocationHelper.kt:58` | `locationRequest` (local variable) | DELETE — `LocationRequest` object is created but never passed to any API call |
| `ui/screens/onboarding/OnboardingScreen.kt:257` | `stepTitle` | DELETE — private composable function, no call sites |
| `ui/screens/recommendations/RecommendationsScreen.kt:283` | `RecommendationsScreenPreview` | SUPPRESS with `@Suppress("UnusedPrivateMember")` — Android Studio `@Preview` renderer invokes this function at design time; it is intentionally private and not callable from production code |

### Risk
- Low. Removing injected repos does not affect `build()` logic — data flow was already routed through other repos.
- Removing `locationRequest` does not change runtime behaviour; the variable was dead code after an API migration.

### Verification
- [ ] `./gradlew :app:compileDebugKotlin` — no "unresolved reference" errors

---

## Batch C — ForbiddenComment

### Rule: `ForbiddenComment`

| File | Line | Comment | Action |
|------|------|---------|--------|
| `repos/RecommendationBuilder.kt:118` | 118 | `//TODO: evaluate according to country` | REMOVE — `interCroppedCrop = "maize"` is intentionally hardcoded for now (only maize intercropping is supported); the TODO adds noise without a tracking ticket |

### Verification
- [ ] No `TODO` markers remain in `RecommendationBuilder`

---

## Batch D — NewLineAtEndOfFile (22 files)

### Rule: `NewLineAtEndOfFile`

Add a single trailing newline to each file listed below. No logic changes.

| File |
|------|
| `network/GeocodingService.kt` |
| `network/LocationHelper.kt` |
| `network/dto/CassavaPrices.kt` |
| `network/dto/CassavaUnits.kt` |
| `network/dto/FertilizerPrices.kt` |
| `network/dto/Fertilizers.kt` |
| `network/dto/MaizePrices.kt` |
| `network/dto/StarchFactories.kt` |
| `database/Converters.kt` |
| `dto/AdviceCompletionDto.kt` |
| `entities/relations/UserWithDetails.kt` |
| `enums/EnumUnitOfSale.kt` |
| `network/dto/Currencies.kt` |
| `network/dto/InvestmentAmounts.kt` |
| `network/dto/UserFeedBackRequest.kt` |
| `network/LocationIqApi.kt` |
| `network/request/ComputeRequest.kt` |
| `network/request/RecommendationRequest.kt` |
| `network/WeatherApi.kt` |
| `repos/FieldOperationCostsRepo.kt` |
| `utils/NumberHelper.kt` |
| `utils/PermissionHelper.kt` |

### Risk
Nil. POSIX newline at end of file; no code change.

### Verification
- [ ] Detekt reports no more `NewLineAtEndOfFile`

---

## Batch E — MatchingDeclarationName (6 DTO files)

### Rule: `MatchingDeclarationName`

The six DTO files were named without the `Dto` suffix before the suffix convention was
adopted. The correct fix is a file rename; however, file renaming requires atomic
delete+create which cannot be done without a shell command (violating the no-build
constraint). File-level `@file:Suppress` is applied as a tracked workaround.

**Each file gets `@file:Suppress("MatchingDeclarationName")`.**

| File | Class inside | Correct filename |
|------|-------------|-----------------|
| `network/dto/CassavaPrices.kt` | `CassavaPriceDto` | `CassavaPriceDto.kt` |
| `network/dto/CassavaUnits.kt` | `CassavaUnitDto` | `CassavaUnitDto.kt` |
| `network/dto/FertilizerPrices.kt` | `FertilizerPriceDto` | `FertilizerPriceDto.kt` |
| `network/dto/Fertilizers.kt` | `FertilizerDto` | `FertilizerDto.kt` |
| `network/dto/MaizePrices.kt` | `MaizePriceDto` | `MaizePriceDto.kt` |
| `network/dto/StarchFactories.kt` | `StarchFactoryDto` | `StarchFactoryDto.kt` |

**Tech debt:** Each file should be renamed in a follow-up PR (no code changes needed, just
a git `mv`).

### Verification
- [ ] Detekt reports no `MatchingDeclarationName` for these files
- [ ] Compilation succeeds (class names are unchanged)

---

## Batch F — ForEachOnRange / VariableNaming / ImplicitDefaultLocale ✓ APPLIED

### Rule: `ForEachOnRange` — `FeedbackBottomSheet.kt:96`
`(0..10).forEach { score -> ... }` → `for (score in 0..10) { ... }`  
**Why:** `forEach` on an `IntRange` allocates a boxed iterator; a `for` loop does not.

### Rule: `VariableNaming` — `LocationHelper.kt:109`
Local `val MAX_LOCATION_AGE` (SCREAMING_SNAKE_CASE) → `val maxLocationAge`  
**Why:** Local variables must be camelCase; SCREAMING_SNAKE_CASE is reserved for
`const val` at top-level or inside `companion object`.

### Rule: `ImplicitDefaultLocale`
| File | Line | Expression | Fix |
|------|------|-----------|-----|
| `network/GeocodingService.kt:91` | 91 | `String.format("%.6f", lat/lon)` | Add `Locale.ROOT` |
| `ui/screens/usecases/InvestmentAmountScreen.kt:99` | 99 | `String.format("%.0f", ...)` | Add `Locale.ROOT` |

**Why:** `String.format` without an explicit `Locale` uses the device locale, which can
cause digit grouping separators or decimal symbols to vary (e.g., `,` instead of `.` for
floats in many European locales). `Locale.ROOT` ensures consistent decimal formatting.

### Risk
- `ForEachOnRange`: performance fix only, no behavioral change.
- `VariableNaming`: cosmetic rename within private function scope.
- `ImplicitDefaultLocale`: lat/lon formatting fix avoids sending `1,234567` to an API.

### Verification
- [ ] No `ForEachOnRange`, `VariableNaming`, `ImplicitDefaultLocale` findings remain

---

## Batch G — Exception Handling ✓ APPLIED

### Rules: `ReturnCount`, `TooGenericExceptionCaught`, `SwallowedException`

#### G-1: `ReturnCount` — `LocationHelper.getCurrentLocation` (3 returns, limit 2)
Restructure guard clauses into a `when` expression — eliminates 2 early returns,
leaving only the single outer `return try { when { ... } }`.

#### G-2: `SwallowedException` — `LocationHelper` (lines 47, 81, 127)
`SecurityException` is caught and a hardcoded string is returned; the exception object
itself is never used. Fix: include `e.message` in the error string so the exception is
observably "consumed".

For `getLastKnownLocation` (line 127): the broad `catch (e: Exception) { null }` is an
intentional null-return pattern for a synchronous best-effort API; add a Sentry capture
so the exception is not silently discarded.

#### G-3: `TooGenericExceptionCaught` — `LocationHelper`
Add explicit `IOException` catch before the general `Exception` catch in both
`getCurrentLocation` and `getFreshLocation`.

#### G-4: `TooGenericExceptionCaught` — service/ViewModel files (handled via config)
The following files catch `Exception` as a final safety net inside `flow {}` blocks or
`viewModelScope.launch` blocks that emit error results. Splitting every possible exception
type would require enumerating unstable 3rd-party internals. Each file is **excluded from
`TooGenericExceptionCaught`** in `detekt.yml`:

| File | Justification |
|------|--------------|
| `network/GeocodingService.kt` | Flow emits `GeocodingResult.Error`; any exception must be caught to avoid crashing the collector |
| `network/WeatherService.kt` | Same pattern |
| `ui/viewmodels/GetRecommendationViewModel.kt` | ViewModel catches all exceptions and emits `UiState.Error` |
| `ui/viewmodels/LocationPickerViewModel.kt` | ViewModel; exception emitted to Sentry and state cleared |
| `ui/screens/settings/LocationPickerScreen.kt` | Map SDK initialisation; exceptions from style loading caught to prevent UI crash |

### Risk
- `ReturnCount` refactor: behaviorally equivalent; restructure only.
- `SwallowedException` fixes: includes exception message in error — no breaking change.
- Config exclusions: targeted, file-level; does not disable rule globally.

### Verification
- [ ] No `ReturnCount`, `SwallowedException` findings remain
- [ ] `TooGenericExceptionCaught` gone from LocationHelper (specific catches added)
- [ ] `TooGenericExceptionCaught` gone from config-excluded files

---

## Batch H — MaxLineLength ✓ APPLIED

Wrap long lines by breaking at natural call-site boundaries.

| File | Lines | Content |
|------|-------|---------|
| `ui/viewmodels/OnboardingViewModel.kt` | 518 | Long `if` condition → split across lines |
| `ui/screens/usecases/CassavaYieldScreen.kt` | 186 | Long `YieldDef(...)` constructor call |
| `ui/screens/usecases/MaizeMarketScreen.kt` | 47 | Long Hilt ViewModel factory lambda |
| `ui/screens/usecases/SweetPotatoMarketScreen.kt` | 46 | Same Hilt factory pattern |
| `dao/FertilizerDao.kt` | 24, 30, 33 | Long `@Query` SQL strings |
| `dao/ProduceMarketDao.kt` | 26 | Long `@Query` SQL string |
| `ui/viewmodels/WeedControlCostsViewModel.kt` | 72, 73 | Long `copy()` expressions |
| `enums/EnumAdvice.kt` | 12 | Long enum constructor call |

### Risk
Formatting-only. No logic change.

### Verification
- [ ] No `MaxLineLength` findings remain

---

## Batch I — MagicNumber (named constants) ✓ APPLIED

Magic numbers remaining after config handles colors and migrations.

### I-1: `FeedbackBottomSheet.kt`
| Value | Extracted constant | Context |
|-------|-------------------|---------|
| `3` (rating when branch) | `RATING_OKAY = 3` | Satisfaction rating label |
| `4` (rating when branch) | `RATING_GOOD = 4` | |
| `5` (rating when branch) | `RATING_EXCELLENT = 5` | |
| `10` (NPS range upper) | `NPS_MAX_SCORE = 10` | |
| `6` (NPS threshold) | `NPS_PASSIVE_MAX = 6` | Detractor/passive boundary |
| `8` (NPS threshold) | `NPS_PROMOTER_MIN_MINUS_ONE = 8` | Passive/promoter boundary |

### I-2: `PlantingDateStep.kt` + `DatesScreen.kt` (same domain constants)
| Value | Extracted constant | Context |
|-------|-------------------|---------|
| `4` | `PLANTING_MONTHS_BEFORE` | Selectable planting window past months |
| `12` | `PLANTING_MONTHS_AHEAD` | Selectable planting window future months |
| `8` | `HARVEST_MONTHS_MIN` | Min months planting→harvest |
| `16` | `HARVEST_MONTHS_MAX` | Max months planting→harvest |

Constants defined in each file's companion (screens are not shared classes).

### I-3: `LocationPickerScreen.kt`
| Value | Extracted constant | Context |
|-------|-------------------|---------|
| `1500L` | `MAP_FLY_DURATION_MS` | Mapbox flyTo animation duration |
| `12.0` | `DEFAULT_ZOOM` | Default map zoom level |
| `1.5` | `MARKER_ICON_SIZE` | Mapbox icon size |
| `600L` | `BOUNCE_DURATION_MS` | Marker bounce animation duration |
| `0.5f` | `BOUNCE_SCALE_OFFSET` | Bounce animation scale floor |
| `36.8219` / `-1.2921` | `NAIROBI_LON` / `NAIROBI_LAT` | Fallback map center (Nairobi) |

### I-4: `CassavaYieldScreen.kt`
Yield amount values (`3.75`, `11.25`, `18.75`, `26.25`, `33.75`) and sort orders (`3`, `4`, `5`) are used only inside `buildCassavaYieldSeeds`. Extract as local private constants at file scope.

### I-5: `LocationHelper.kt`
| Value | Extracted constant |
|-------|-------------------|
| `10L` (seconds) | `LOCATION_INTERVAL_SECONDS` |
| `5L` (seconds) | `LOCATION_FASTEST_INTERVAL_SECONDS` |
| `15L` (seconds) | `LOCATION_MAX_WAIT_SECONDS` |
| `5L` (minutes) | `LOCATION_MAX_AGE_MINUTES` |

### I-6: `AppSettingsDataStore.kt:205` + `SettingsViewModel.kt:126`
Default notification count `3` extracted as `DEFAULT_NOTIFICATION_COUNT = 3` in
`AppSettingsDataStore.companion` and referenced from both locations.

### I-7: `RecommendationsScreen.kt`
`1.5f` (parallax multiplier) and `0.35f` (parallax speed) → `PARALLAX_FADE_FACTOR` / `PARALLAX_SPEED` private constants.

### Risk
Constants extraction: no behavioural change. Naming makes intent explicit.

### Verification
- [ ] No `MagicNumber` findings in the above files (except `AkilimoColors.kt` and `DatabaseMigrations.kt` handled by config)

---

## Batch J — Deferred: Complex ViewModel Refactoring

The following findings require large, test-intensive refactoring that would exceed the
scope of a single incremental fix and carry regression risk without a test suite to back
them.

| Rule | File | Finding | Why Deferred |
|------|------|---------|-------------|
| `LongMethod` (135 lines) | `OnboardingViewModel.onEvent` | Complex `when` over all Event subtypes | Splitting requires new intermediate state or sub-handlers; unit tests exist and must be preserved |
| `CyclomaticComplexMethod` (cc=35) | `OnboardingViewModel.onEvent` | Same | Same |
| `LongMethod` (60 lines) | `OnboardingViewModel.loadInitialState` | Loads and maps all onboarding state | Safe to split into load phases; requires careful ordering |
| `CyclomaticComplexMethod` (cc=24) | `OnboardingViewModel.loadInitialState` | Same | Same |
| `CyclomaticComplexMethod` (cc=15) | `OnboardingViewModel.validateAll` | Validates all onboarding fields | Refactor as rule-based validator; medium complexity |
| `LongMethod` (67 lines) | `OnboardingViewModel.persistAll` | Writes all state to DB | Can be split by domain; lower risk |
| `LongMethod` (148 lines) | `RecommendationBuilder.build` | Assembles full recommendation payload | Large data assembly function; splitting requires careful domain grouping |
| `CyclomaticComplexMethod` (cc=33) | `RecommendationBuilder.build` | Same | Same |

**Recommended follow-up approach:**
- Extract `OnboardingViewModel.onEvent` into domain-scoped handlers (`handleBioEvent`, `handleLocationEvent`, etc.)
- Split `RecommendationBuilder.build` into sub-builders (`buildFarmInfo`, `buildMarketInfo`, `buildTillage`)

---

## Suppressions Summary

| Location | Rule | Justification |
|----------|------|--------------|
| `detekt.yml` — `FunctionNaming.ignoreAnnotated` | `FunctionNaming` | Compose PascalCase convention is industry standard |
| `detekt.yml` — `LongParameterList.ignoreAnnotated` | `LongParameterList` | Compose default-parameter pattern is not equivalent to long argument lists in imperative code |
| `detekt.yml` — `LongMethod.ignoreAnnotated` | `LongMethod` | Compose markup is verbose by design |
| `detekt.yml` — `CyclomaticComplexMethod.ignoreAnnotated` | `CyclomaticComplexMethod` | Compose conditional rendering inflates CC score without real logic complexity |
| `detekt.yml` — `MagicNumber.excludes AkilimoColors.kt` | `MagicNumber` | Hex color literals are the definition, not a hidden constant |
| `detekt.yml` — `MagicNumber.excludes DatabaseMigrations.kt` | `MagicNumber` | Migration version pairs are self-evident in `Migration(X, Y)` context |
| `detekt.yml` — `TooGenericExceptionCaught.excludes` (5 files) | `TooGenericExceptionCaught` | Flow/ViewModel safety-net catches must handle unknown 3rd-party exceptions |
| `RecommendationsScreen.kt` — `@Suppress("UnusedPrivateMember")` | `UnusedPrivateMember` | `@Preview` functions are invoked by Android Studio design-time tooling, not production code |
| `CassavaPrices.kt` et al. — `@file:Suppress("MatchingDeclarationName")` | `MatchingDeclarationName` | File predates Dto suffix convention; rename tracked as follow-up tech debt |
