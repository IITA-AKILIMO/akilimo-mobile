# Architecture Cleanup Plan

This document captures a practical cleanup plan for the current AKILIMO Android app structure.

It is not a rewrite proposal. The app already has a workable structure, but it is starting to accumulate overlap, generic catch-all packages, and migration residue. The goal is to improve clarity and maintainability without destabilizing the product.

## Tracking

- Overall status: `Not started`
- Last updated: `2026-03-25`
- Primary owner: `[ ] Unassigned`
- Review cadence: `[ ] Weekly` `[ ] Bi-weekly` `[ ] Monthly`

## Completed Changes Log

- [x] Added the initial architecture cleanup plan document.
- [x] Converted major action areas into checkbox-based checklists.
- [x] Added package guardrails to contributor guidance to stop new additions to `rest`, `helper`, `utils`, and `interfaces` by default.
- [x] Split the root navigation host into feature graph registration functions.

## Current Assessment

The current project structure is broadly sound:

- Single `app` module
- Clear `ui`, `repos`, `data`, `database`, `di`, `workers`, and `navigation` areas
- Compose screens grouped by feature
- ViewModels separated from screens
- Repositories acting as the data access boundary for UI code

The main issues are:

- Package sprawl under `com.akilimo.mobile`
- Conceptual overlap between `network` and `rest`
- Generic buckets such as `helper`, `utils`, `interfaces`, and `extensions`
- Navigation graph concentration in a single large host
- Legacy migration residue such as `wizard`
- Low test coverage relative to app size

## Goals

1. Reduce ambiguity in package ownership.
2. Remove overlapping layers and generic catch-all buckets.
3. Make feature boundaries easier to understand.
4. Keep Compose migration moving toward a consistent target shape.
5. Improve test coverage before major architectural changes.
6. Avoid a risky big-bang refactor.

## Non-Goals

- Splitting into multiple Gradle modules immediately
- Rewriting working features for stylistic reasons
- Replacing MVVM, Hilt, Room, WorkManager, or Compose

## Target Direction

Keep the app as a single module for now, but move toward clearer feature-oriented packaging inside that module.

Recommended medium-term package direction:

```text
com.akilimo.mobile/
  data/
    local/
    remote/
    settings/
    repository/
  domain/
    enums/
    models/
  navigation/
    routes/
    graphs/
  ui/
    activities/
    components/
    theme/
    features/
      onboarding/
      recommendations/
      settings/
      usecases/
  workers/
  di/
```

This does not need to be implemented all at once. It is a direction for gradual convergence.

## Main Cleanup Areas

### 1. Consolidate `network` and `rest`

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

Problem:

- `network` already contains API interfaces and client setup.
- A separate `rest` package creates uncertainty about where remote concerns belong.

Plan checklist:

- [ ] Pick one remote-data package structure and standardize on it.
- [ ] Confirm `data/remote/` as the long-term home for remote concerns.
- [ ] Move `AkilimoApi` into the chosen remote-data package.
- [ ] Move `ApiClient` into the chosen remote-data package.
- [ ] Move interceptors and adapters into the chosen remote-data package.
- [ ] Move remote utilities into the chosen remote-data package.
- [ ] Remove `rest` or mark it unused once migration is complete.

Exit checklist:

- [ ] There is exactly one obvious place for Retrofit services.
- [ ] There is exactly one obvious place for API clients.
- [ ] There is exactly one obvious place for interceptors and adapters.
- [ ] There is exactly one obvious place for network monitoring utilities.

### 2. Reduce generic buckets

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

Problem:

- `helper`, `utils`, `interfaces`, and some `extensions` packages make ownership unclear.
- These packages become dumping grounds and make dependencies harder to reason about.

Plan checklist:

- [ ] Audit every file in `helper`.
- [ ] Audit every file in `utils`.
- [ ] Audit every file in `interfaces`.
- [ ] Audit every file in `extensions`.
- [ ] Move feature-specific files into feature packages.
- [ ] Move data-related files into `data`.
- [ ] Move navigation-related files into `navigation`.
- [ ] Move shared UI files into `ui/components`.
- [ ] Move worker-related files into `workers`.
- [ ] Move domain-oriented files into `domain`.
- [ ] Keep only true cross-cutting utilities in narrowly named packages.

Exit checklist:

- [ ] No new files are added to generic buckets unless clearly justified.
- [ ] Existing generic packages shrink substantially or disappear.

### 3. Organize UI by feature more consistently

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

Problem:

- UI is partially feature-grouped, but ViewModels remain centralized in `ui/viewmodels`.
- This increases distance between screens, state, and behavior.

Plan checklist:

- [ ] For new work, colocate screens with their related state and ViewModel where practical.
- [ ] Introduce feature-local screen state types where needed.
- [ ] Introduce feature-local event/effect types where needed.
- [ ] Stop extending centralized `ui/viewmodels` for brand-new feature code by default.
- [ ] Use the target shape below as the default for touched or new features.

Example target shape:

```text
ui/features/usecases/fertilizer/
  FertilizerScreen.kt
  FertilizerViewModel.kt
  FertilizerUiState.kt
```

- [ ] Migrate existing centralized `ui/viewmodels` incrementally when features are touched.

Exit checklist:

- [ ] New features follow feature-local packaging.
- [ ] Older screens are migrated opportunistically when touched.

### 4. Split navigation by feature

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[x] Done`

Problem:

- `AkilimoNavHost.kt` will keep growing as more routes are added.
- One large graph file becomes harder to scan and review.

Plan checklist:

- [x] Keep a single root `NavHost`.
- [x] Split onboarding route registration into `onboardingGraph(...)`.
- [x] Split recommendations route registration into `recommendationsGraph(...)`.
- [x] Split settings route registration into `settingsGraph(...)`.
- [x] Split use-case route registration into `usecaseGraph(...)`.
- [ ] Centralize or group route definitions under `navigation/routes`.

Exit checklist:

- [x] The root nav host only wires top-level graphs.
- [x] Feature route registration is no longer all in one file.

### 5. Resolve migration residue

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

Problem:

- `wizard` and some onboarding structure suggest old and new UI flows coexist without a clean final shape.
- This is expected during migration, but it should not remain indefinite.

Plan checklist:

- [ ] Identify legacy or transitional packages.
- [ ] Mark each such package as `active`, `transitional`, or `deprecated` in docs.
- [ ] Define the destination package for every transitional package.
- [ ] Define removal conditions for every deprecated package.

Exit checklist:

- [ ] Migration-only packages have explicit status.
- [ ] Migration-only packages have a documented end-state.

### 6. Strengthen the repository boundary

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

Problem:

- The repository layer is currently useful, but should remain the only UI-facing data boundary.
- Without discipline, ViewModels may drift toward direct DAO or remote usage.

Plan checklist:

- [ ] Preserve the rule that ViewModels talk to repositories only.
- [ ] Prevent direct DAO access from screens and ViewModels.
- [ ] Prevent direct Retrofit access from screens and ViewModels.
- [ ] Keep repository APIs stable where possible during reorganization.
- [ ] Reassess repository naming if entity-centric names become a maintenance problem.

Exit checklist:

- [ ] No screen reaches directly into DAO infrastructure.
- [ ] No screen reaches directly into Retrofit infrastructure.
- [ ] No ViewModel reaches directly into DAO infrastructure.
- [ ] No ViewModel reaches directly into Retrofit or worker infrastructure.

### 7. Improve test coverage before major refactors

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

Problem:

- The main source set is much larger than the test set.
- This increases risk for package moves and navigation cleanup.

Plan checklist:

- [ ] Prioritize ViewModel tests.
- [ ] Prioritize repository behavior tests.
- [ ] Add tests for navigation result handling.
- [ ] Add tests for worker scheduling behavior where practical.
- [ ] Add tests when touching brittle or central code paths.
- [ ] Treat migration-area coverage as a prerequisite for larger structural changes.

Exit checklist:

- [ ] Core flows have reliable ViewModel coverage.
- [ ] Core flows have reliable repository coverage.
- [ ] Structural refactors are backed by tests, not manual confidence only.

## Suggested Rollout

### Phase 1: Low-risk cleanup

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[x] In progress` `[ ] Blocked` `[ ] Done`

- [x] Add this plan to docs
- [ ] Stop adding new files to `helper`, `utils`, `interfaces`, and `rest`
- [ ] Define package placement rules for new code
- [ ] Remove obvious dead or duplicate structure where safe

### Phase 2: Navigation and package convergence

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[x] In progress` `[ ] Blocked` `[ ] Done`

- [x] Split `AkilimoNavHost` into feature graph functions
- [ ] Start moving new or touched features toward feature-local UI packaging
- [ ] Consolidate remote infrastructure into one package path

### Phase 3: Migration residue reduction

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

- [ ] Identify transitional packages such as `wizard`
- [ ] Move remaining Compose-first code into target feature packages
- [ ] Remove deprecated leftovers once no longer referenced

### Phase 4: Re-evaluate modularization

Owner: `[ ] Unassigned`
Status: `[ ] Not started` `[ ] In progress` `[ ] Blocked` `[ ] Done`

- [ ] Reassess whether Gradle modularization is worth the cost after package boundaries and tests improve
- [ ] Evaluate `core-ui` as a candidate future module
- [ ] Evaluate `data` as a candidate future module
- [ ] Evaluate `feature-onboarding` as a candidate future module
- [ ] Evaluate `feature-settings` as a candidate future module
- [ ] Evaluate `feature-recommendations` as a candidate future module

This should only happen if the single-module structure becomes a real bottleneck.

## Package Placement Rules For New Code

Use these rules immediately:

- New Compose screens go under feature-specific UI packages.
- New ViewModels should be colocated with the feature when practical.
- New remote code should go into the chosen remote-data package, not `rest`.
- Do not add new files to `helper` or `utils` unless there is a strong, explicit reason.
- Prefer specific names over generic names like `Manager`, `Helper`, or `Utils`.
- Keep repositories as the only data boundary used by ViewModels.

## Risks

- Moving files without tests can cause subtle regressions.
- Renaming packages during active feature work can create merge friction.
- Over-cleaning too early can slow product delivery.

Mitigation:

- Refactor in small slices.
- Prefer opportunistic cleanup when already touching an area.
- Back structural work with targeted tests.

## Immediate Next Actions

- [x] Update team conventions to ban new additions to `rest`, `helper`, `utils`, and `interfaces` unless justified.
- [x] Split `AkilimoNavHost.kt` into feature graph registration functions.
- [ ] Create a package audit checklist for `helper`, `utils`, `interfaces`, `extensions`, `wizard`, `network`, and `rest`.
- [ ] Start colocating new feature UI code instead of extending centralized `ui/viewmodels`.
- [ ] Add tests around the most central ViewModels before larger structural moves.

## Definition Of Success

The cleanup is successful if:

- [ ] A new engineer can predict where code belongs without guesswork.
- [ ] Feature code is easier to trace end-to-end.
- [ ] Navigation is split into manageable units.
- [ ] Remote and data responsibilities are not duplicated across packages.
- [ ] Generic catch-all packages stop growing.
- [ ] Structural changes can be made safely because tests cover key flows.
