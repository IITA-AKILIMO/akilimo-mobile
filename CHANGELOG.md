# Changelog

All notable changes to AKILIMO will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [30.0.0] - 2026-02-23

### Features

- ✨ feat(ui): implement language selection restart prompt
- 🌐 i18n(activity): implement dynamic locale switching across all activities

### Bug Fixes

- 🐛 fix(fragment): use singleton pattern for session manager for better consistency
- 🐛 fix: LocaleHelper support for Android N and above locale setting
- 🐛 fix(cassava-market): correct unit price calculation and adapter update logic
- Fix/input text
- Fix/443 missing fertilizer options

### Code Refactoring

- ♻️ refactor(repository): implement thread-safe singleton pattern for SessionManager (BREAKING CHANGE)
- ♻️ refactor(i18n): simplify locale definitions by removing country-specific prefixes
- ♻️ refactor(config): use singleton pattern for SessionManager in AppConfig

### i18n

- 🌐 i18n: add language restart warning and requirement strings
- 🌐 i18n: update strings.xml for Rwandan localization support
- 🌐 i18n: fix translations for navigation buttons and update feedback/error messages

### Styling

- 💄 style: update layout for language selection screen for Material Design compliance
- 💄 style: update vector drawables for info outline and language icons
- 💄 style: update bottom sheet price selection layout with drag handle and stacked buttons
- 💄 style: update bg_drag_handle.xml with new shape definition

### Miscellaneous Tasks

- ✨ chore(permissions): add advertising id permission for Google Play compliance
- 👷 ci(.github): remove redundant branch checks in workflow
- 👷 ci(gradle): implement time-based cache expiration
- 👷 ci(gradle): simplify checkout action
- 👷 ci: simplify workflow to build on tags
- 👷 ci: add github workflow for unit tests
- 👷 ci: trigger release on pull request merge
- 👷 ci(workflow): add path filters to release trigger
- Internal ci(github): add concurrency to release workflow
- ✨ feat(build): add firebase analytics and crashlytics
- 👷 ci: update pr automation workflow
- 👷 ci: update pr automation token mechanism
- 👷 ci: optimize unit test workflow caching

### Documentation

- Docs: update changelog

## [29.0.2] - 2026-01-09

### Documentation

- Docs: update changelog 

Co-authored-by: github-actions[bot] <github-actions[bot]@users.noreply.github.com>

### Miscellaneous Tasks

- 👷 ci: update release workflow triggers and push
- 👷 ci(release): update push target for changelog
- 👷 ci: restrict release workflow to main branch
- Next release 
- 👷 ci(gradle): use github app token for release workflow
- 👷 ci: fix formatting in release workflow
- 👷 ci: update github actions bot configuration
- Ci(workflow): update git push command in release
- 👷 ci(gradle): fix changelog commit and push action
- 👷 ci(gradle): optimize release workflow and auth
- 👷 ci(release): improve changelog push and tag flow
- 👷 ci: improve changelog update flow in release workflow
- 👷 ci: update changelog handling in release workflow

## [29.0.1] - 2025-12-30

### Miscellaneous Tasks

- Ci: update release workflow commit message
- ♻️ refactor(network): align feedback dto with api response
- 👷 ci(release): improve changelog automation
- 👷 ci(gradle): disable configuration cache for sonar
- Next release 
- Ci(workflow): fix changelog commit message format
- Ci: improve changelog generation workflow

### Styling

- Style(database): fix typo in todo comment

## [29.0.0] - 2025-12-30

### Miscellaneous Tasks

- Next release 
- 👷 ci: update github actions cache key
- 👷 ci: remove automatic release tagging
- 👷 ci: add automated release workflow
- Ci: trigger release workflow on push to main

## [28.1.1] - 2025-12-28

### Miscellaneous Tasks

- Next release 

## [28.1.0] - 2025-12-28

### Bug Fixes

- Fix(tests): Correct EnumCountry mapping for TZ test

### Code Refactoring

- Refactor(tillage): use EnumOperation enum directly
- Refactor(MandatoryInfo): remove unused radio index fields
- Refactor(navigation): update intent in FrActivity
- Refactor(recommendation): rename DstRecommendationActivity to GetRecommendationActivity
- Refactor: remove unused SPH activity layout
- Refactor(recommendation): implement use case screen
- Refactor(ui): Animate selection state in adapters
- Refactor(ui): Remove text color animation from adapters
- Refactor(theme): Update gradient overlay color
- Refactor(recommendations): Tweak recommendation card image layout
- Refactor(recommendation): Update intercropping with sweet potato use cases
- Refactor(ui): Adjust window insets handling in BaseActivity
- Refactor(recommendation): Invert icon visibility logic
- Refactor(recommendations): Clean up and enable recommendation options
- Refactor(ui): Re-enable stepper fragments and integrate `SimpleStepperListener`
- Refactor(ui): Use string resources for exit dialog text in `HomeStepperActivity`
- Refactor(ui): Simplify `sizeUnitLabel` retrieval in `WeedControlCostsActivity`
- Refactor(db): Re-order imports and cleanup in `AppDatabase`
- Refactor(location): migrate to mapbox maps sdk v10
- Refactor(permissions): modernize permission handling logic
- Refactor(permissions): abstract location permission checks
- Refactor(maps): migrate LocationPickerActivity to Mapbox SDK v11
- Refactor(location): modernize location fetching logic

### Features

- Feat(security): update network security configuration
- Feat(SplashActivity): initialize Retrofit with context
- Feat(security): add network security configuration
- Feat:migration to git history
- Feat(recommendations): Add more advice options
- Feat(recommendations): Enable more advice options for IC Maize
- Feat(build): Define base URLs in default config and remove from release/debug
- Feat(build): Automate `versionName` and `versionCode` generation
- Feat(deps): update mapbox sdk and add new dependencies
- Feat(res): add location pin icon
- Feat(deps): integrate swipe-to-refresh and location services
- Feat(location): add location helper utility
- Feat(permissions): add permission helper class
- Feat/16kb support 

### Miscellaneous Tasks

- 🔧 ci(workflows): exclude `android-19` branch from `push` triggers
- ✨ feat(proguard): add rules for Jackson annotations and Java 8 compatibility
- 🗑️ chore(build): remove commented-out dependencies from build.gradle
- 🔧 chore(build): update dependencies and configuration settings
- Chore(Akilimo): remove comment from AndroidThreeTen initialization
- ✨ feat(layout): add NestedScrollView to WelcomeFragment
- ✨ feat(network): add custom SSLSocketFactory for older Android versions
- 🔧 ci(workflow): enable artifact builds for fix branches
- 🔧 chore(ci): restrict artifact builds to beta and main branches
- ⚡️ perf(fragments): remove IFragmentCallBack and use Fragment Result API
- ♻️ refactor(strings): rename `exact_fertilizer_price` to `exact_price`
- ♻️ refactor(TractorAccessActivity): use view binding and clean up code
- ✨ feat(inherit): add BaseBottomSheetDialogFragment
- ♻️ refactor(CostBaseActivity): migrate to use ViewBinding
- ✨ feat(app/src/main/res/values/dimens.xml): Added new dimension value
- ♻️ refactor(entity): update CurrentPractice entity with ColumnInfo and enums
- ♻️ refactor(SummaryFragment): simplify string building and improve context usage
- ♻️ refactor(TillageOperationFragment): improve data handling and UI updates
- ✨ feat(viewmodel): add TillageOperationViewModel and Factory
- ✨ feat(inherit): add BindBaseStepFragment for ViewBinding
- ♻️ refactor(BaseFragment): migrate to ViewBinding
- ✨ feat(LocationFragment): initialize and update mapBoxToken variable
- ♻️ refactor(BioDataFragment): improve code readability and data handling
- ✨ feat(data): add indexOfValue extension function to InterestOption list
- ✨ feat(data): update indexOfValue function in InterestOption.kt
- ✨ feat(data): refactor InterestOption to use ValueOption interface
- ✨ feat(dao): remove unused update method from UserProfileDao
- ✨ feat(app): Enhance CountryFragment with CountryPickerDialog and allowed countries
- ✨ feat(entities): Remove redundant index tracking fields in UserProfile
- ✨ feat(profiles): Update profile data handling and DAO operations
- ♻️ refactor(layout): migrate dialog_field_size.xml to Material Components
- ✨ feat(dialog): update DateDialogPickerFragment to use DialogFragment and improve date handling
- ♻️ refactor(FieldSizeFragment): simplify logic and use view binding
- 🎨 style(TillageOperation): update button styles and layout
- ✨ feat(MathHelper): fix rounding logic and improve precision handling
- ✨ fix(CountryFragment): remove redundant session update after database insertion
- 💄 feat(activity): improve system window fitting and add bottom padding
- 💄 chore(layout): add bottom padding and enable system window fitting across activities
- Chore: Update focus colors
- Chore(strings): Update string resource keys for sweet potato
- Chore(enums): Update string resource key for sweet potato market outlet
- Chore(manifest): Add SweetPotatoMarketActivity to AndroidManifest
- Chore(enums): Comment out weed management advice
- Chore(ci): Temporarily disable actor check in PR automation workflow
- Chore(ui): Add `SimpleStepperListener` base implementation for stepper events
- Chore(network): Add `LocalDateAdapter` for JSON (de)serialization with Moshi
- Chore(utils): Add `NumberHelper` with null-safe Double extensions
- Chore(utils): Add `orUnavailable` extension for null-safe default strings
- Chore(network): Add `LocalDateAdapter` to Moshi instance in `ApiClient`
- Chore(network): Add POST endpoint for computing recommendations in `AkilimoApi`
- Chore(enum): Add `produce` function to `EnumProduceType` for mapping produce types
- Chore(enum): Update `EnumUnitOfSale` weights to `Double` type for precision adjustments
- Chore(model): Remove default values in `UserInfo` properties and add `riskAttitude` field
- Chore(utils): Refactor `RecommendationBuilder` to improve readability and modularize logic
- Chore(ui): Integrate `RecommendationBuilder` and `AkilimoApi` in `GetRecommendationActivity` for API-based recommendations
- Chore(model): Refactor `ComputeRequest` structure and field types for improved consistency and clarity
- Chore(ui): Add `EnumUseCase` handling in various activities for improved use case specificity
- Chore(ui): Update `GetRecommendationActivity` to set toolbar title and enhance recommendation handling logic
- Chore(ui): Pass `EnumUseCase` as Parcelable to `GetRecommendationActivity` for enhanced intent handling
- Chore(utils): Update `RecommendationBuilder` to set intercrop recommendations based on `EnumUseCase`
- Chore(ui): Refactor coroutine scope usage in `GetRecommendationActivity` for streamlined thread handling and error management
- Chore(ui): Update card background and elevation styles in `fragment_area_unit.xml` for layout consistency
- Chore(ui): Update card elevation and background styles in `fragment_bio_data.xml` for visual consistency
- Chore(utils): Update `RecommendationBuilder` logic to conditionally set recommendations based on `EnumUseCase`
- Chore(ui): Simplify `Toast` usage in `GetRecommendationActivity` by removing fully qualified references
- Chore(ui): Update card background styles across layouts to use `?attr/colorSurfaceVariant` for consistency
- Chore(release): Bump version to `30.0.0` in `nextrelease.txt`
- 💚 ci(android): Ensure latest tag file exists in workflow
- 👷 ci(android): Add caching for unit test results
- 💚 ci(android): Disable release notes generation in workflow
- 📝 docs: Add CHANGELOG file
- ♻️ refactor(.github): Overhaul release notes generation script
- Wrench(config): Update git-cliff configuration for AKILIMO
- 🔥 chore(fastlane): Remove versioned changelogs
- 📝 docs: Update and reformat changelog
- 🚀 chore(build): Relocate whatsnew files to release directory
- ♻️ refactor(script): Change release notes output directory
- 🚀 build(ci): Allow manual and PR-triggered workflow runs
- :memo:(whatsnew): Update Swahili release notes
- ♻️ refactor(ci): Improve release tagging and workflow reliability
- :construction_worker: ci(android): Temporarily disable unit_test dependency
- 👷 ci(workflows): Simplify unit test trigger condition
- 💚 fix(ci): Always run unit tests and fix artifact caching
- 💚 fix(ci): Correct release artifact upload step name
- ♻️ refactor(ci): Remove -beta suffix from release tag
- 🏗️ ci(android): Only update build number on successful release
- 👷 ci(.github): Rename workflow to Android CI
- ♻️ refactor(locale): Standardize locale definitions and usage
- ♻️ refactor(ci): Simplify workflow trigger
- 👷 build(ci): Fix branch name for production release trigger
- ♻️ refactor(ci): Improve release trigger conditions for bump-and-tag
- 🔥 chore(ci): Remove bump-and-tag workflow
- 👷 build(ci): Add Kotlin file pattern to todo-checker
- 💚 ci(workflows): Fix branch name in todo-checker
- Ci/todo 
- ♻️ refactor(ci): Standardize cache key for build artifacts
- ♻️ refactor(ui): convert location info card to constraintlayout
- 👷 ci: improve build caching and versioning logic
- 👷 ci: trigger workflow on yml changes
- 💥 feat(deps)!: migrate to mapbox v11 and modernize location handling
- Ci(release): automate changelog generation

### Revert

- Revert(release): set next version to 28.0.0

### Styling

- Style: remove unused imports
