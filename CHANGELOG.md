# Changelog

All notable changes to AKILIMO will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Documentation

- Docs: update changelog


## [30.0.0] - 2026-02-23

### Bug Fixes

- Fix/input text 

- Fix/443 missing fertilizer options 


### Documentation

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog


### Miscellaneous Tasks

- 👷 ci(gradle): implement time-based cache expiration

Add a 20-minute timestamp bucket to the GitHub Actions cache key. This
ensures build artifacts are refreshed periodically to prevent stale
outputs.

- 👷 ci(gradle): simplify checkout action

Remove unnecessary fetch-depth configuration from the checkout step. This
cleans up the workflow file since full history is no longer required.

- 👷 ci: simplify workflow to build on tags

Streamline the Android CI process by removing unit test jobs and
restructuring the workflow to trigger only on tag pushes.

- 👷 ci: add github workflow for unit tests

Add a comprehensive CI pipeline to automate unit testing with Gradle
caching and test result reporting.

- 👷 ci: trigger release on pull request merge

Update the release workflow to trigger only when a pull request is merged to main. This prevents unnecessary tag creation from direct pushes or unmerged closed PRs.

- 👷 ci(workflow): add path filters to release trigger

Add push exclusions for non-code files and documentation to prevent
unnecessary workflow executions on the release pipeline.

- Internal ci(github): add concurrency to release workflow

Add concurrency configuration to prevent redundant release jobs from
running simultaneously. This ensures only the latest trigger proceeds.

- ✨ feat(build): add firebase analytics and crashlytics

Uncomment and configure Firebase Analytics and Crashlytics dependencies
to enable crash reporting and usage tracking.

- 👷 ci: update pr automation workflow

Refine auto-approval triggers and add filters to only process pull
requests from automated bots like Dependabot.

- 👷 ci: update pr automation token mechanism

Remove restrictive user filters and switch to GitHub App tokens for
reliable automated pull request approvals.

Closes #42

- 👷 ci: optimize unit test workflow caching

Update concurrency and cache keys to use commit SHA and disable
cancellation to ensure test results are reliably saved.

- 💄 style(ui): update price selection bottom sheet layout

Refactor ID naming conventions and improve spacing, colors, and button
alignment for better visual consistency.

Closes #420

- ♻️ refactor(i18n): simplify locale definitions

Remove country-specific prefixes from locale names for better clarity and consistency.

- ♻️ refactor(config): use singleton pattern for SessionManager

Update AppConfig to use the static getter for SessionManager instead of
manual instantiation to ensure consistent state and resource efficiency.

- Next release 

ci(.github): remove conditional branch checks in workflow

Remove redundant `if` conditions for branch checks (`main`, `hotfix`) in
the deployment pipeline, as the workflow is already triggered only for
these branches.

Co-authored-by: masgeek <barsamms@gmail.com>

- ✨ chore(permissions): add advertising id permission

Include the AD_ID permission to comply with Google Play policies for
apps targeting Android 13 and above.


## [29.0.2] - 2026-01-09

### Documentation

- Docs: update changelog 

Co-authored-by: github-actions[bot] <github-actions[bot]@users.noreply.github.com>

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog

- Docs: update changelog


### Miscellaneous Tasks

- 👷 ci: update release workflow triggers and push

Include the develop branch in release triggers and simplify the git push
command using origin HEAD for better reliability.

- 👷 ci(release): update push target for changelog

Update the push command to use the GITHUB_TOKEN for authentication and
target the main branch instead of the changelog branch.

- 👷 ci: restrict release workflow to main branch

Remove the develop branch from the push trigger to ensure release
artifacts are only generated from production-ready code.

- Next release 

👷 ci: update release workflow triggers and push

Include the develop branch in release triggers and simplify the git push
command using origin HEAD for better reliability.

---------

Co-authored-by: masgeekw <barsamms@gail.com>
Co-authored-by: Sammy M <barsamms@gmail.com>
Co-authored-by: github-actions[bot] <41898282+github-actions[bot]@users.noreply.github.com>

- 👷 ci(gradle): use github app token for release workflow

Implement GitHub App authentication for the checkout process to bypass
default token limitations and handle automated tagging properly.

- 👷 ci: fix formatting in release workflow

Remove unnecessary whitespace in the release workflow configuration to
maintain clean YAML structure.

- 👷 ci: update github actions bot configuration

Update the git user credentials and simplify the push command for
automated changelog commits.

- Ci(workflow): update git push command in release

Simplify the push command by removing the explicit origin and branch name. This relies on the default tracking branch configuration.

- 👷 ci(gradle): fix changelog commit and push action

Update the workflow to use a GitHub App token for authentication and ensure
changes are pushed explicitly to the main branch.

- 👷 ci(gradle): optimize release workflow and auth

Update GitHub Actions release process to use App Token for authentication and improve changelog commit logic. These changes ensure consistent permissions across checkout, tagging, and deployment steps.

- 👷 ci(release): improve changelog push and tag flow

Add git pull rebase and fetch commands to prevent push conflicts during
the automated release process. Clean up unnecessary comments.

- 👷 ci: improve changelog update flow in release workflow

Refactor the git operations to check for changelog changes before
staging and rebasing to prevent unnecessary push attempts.

Closes #42

- 👷 ci: update changelog handling in release workflow

Stash local changelog changes before pulling from remote to avoid rebase
conflicts during the release process.


## [29.0.1] - 2025-12-30

### Miscellaneous Tasks

- Ci: update release workflow commit message

Standardize the commit message in the release workflow to follow Conventional Commits. This change improves clarity and consistency in the CI pipeline's commit history.

- ♻️ refactor(network): align feedback dto with api response

Rename RecommendationFeedback DTO and update its data structure to
match the new backend API specification for user feedback.

- 👷 ci(release): improve changelog automation

Update GitHub Actions workflow to handle empty commits gracefully and
standardize git configuration syntax.

- 👷 ci(gradle): disable configuration cache for sonar

Pass --no-configuration-cache to the sonar task to prevent build failures. The Sonar Gradle plugin is currently incompatible with the configuration cache.

- Next release 

ci: update release workflow commit message

Standardize the commit message in the release workflow to follow
Conventional Commits. This change improves clarity and consistency in
the CI pipeline's commit history.

---------

Co-authored-by: masgeekw <barsamms@gail.com>
Co-authored-by: Sammy M <barsamms@gmail.com>
Co-authored-by: github-actions[bot] <41898282+github-actions[bot]@users.noreply.github.com>

- Ci(workflow): fix changelog commit message format

Correct the commit command in the release workflow to follow conventional commit standards. This ensures consistency in the commit history.

- Ci: improve changelog generation workflow

Refine the release workflow to prevent empty commits and ensure the changelog branch is properly handled. This makes the changelog generation more robust and avoids unnecessary repository history entries.


### Styling

- Style(database): fix typo in todo comment

Correct a spelling mistake in a code comment to improve readability.


## [29.0.0] - 2025-12-30

### Miscellaneous Tasks

- Next release 

👷 ci: improve build caching and versioning logic 

---------

Co-authored-by: masgeekw <barsamms@gail.com>
Co-authored-by: Sammy M <barsamms@gmail.com>

- 👷 ci: update github actions cache key

Rename the cache key from 'akilimo-builds' to 'akilimo-artifacts' for better clarity and consistency in the CI workflow.

- 👷 ci: remove automatic release tagging

Remove the `create_release_tag` job to switch to manual release management. Fetch full git history to support changelog generation tools.

- 👷 ci: add automated release workflow

Add a GitHub Actions workflow to automatically create tags and releases
after the main branch CI succeeds or when triggered manually.

- Ci: trigger release workflow on push to main

Simplify the release workflow by triggering it directly on pushes to the main branch, instead of waiting for the "Android CI" workflow to complete. This removes complexity and potential delays in the release process.


## [28.1.1] - 2025-12-28

### Miscellaneous Tasks

- Next release 

♻️ refactor(ci): Standardize cache key for build artifacts

Update the cache key in the GitHub Actions workflow to use a more
standardized and descriptive prefix. This improves clarity and
consistency across different workflows.

---------

Co-authored-by: masgeekw <barsamms@gail.com>
Co-authored-by: Sammy M <barsamms@gmail.com>


## [28.1.0] - 2025-12-28

### Bug Fixes

- Fix(tests): Correct EnumCountry mapping for TZ test


### Code Refactoring

- Refactor(tillage): use EnumOperation enum directly

- Updated `loadOperationCost` and `showDialogFullscreen` methods in `ManualTillageCostActivity.kt` to accept `EnumOperation` and `EnumOperationType` enums directly instead of their string representations.
- This change improves type safety and readability by leveraging Kotlin's enum capabilities.

- Refactor(MandatoryInfo): remove unused radio index fields

- Removed `areaUnitRadioIndex` and `fieldSizeRadioIndex` from the `MandatoryInfo` entity.

These fields were no longer used in the application logic.

- Refactor(navigation): update intent in FrActivity

- Change the target `Intent` from `DstRecommendationActivity` to `GetRecommendationActivity` on a button click.

- Refactor(recommendation): rename DstRecommendationActivity to GetRecommendationActivity

- Update the detekt baseline file to reflect the renaming of `DstRecommendationActivity.kt` to `GetRecommendationActivity.kt`.

- Refactor: remove unused SPH activity layout

The layout file `activity_sph.xml` has been deleted as it is no longer in use.

- Refactor(recommendation): implement use case screen

- Convert `SphActivity` to a use case selection screen for recommendations.
- Inherit from `BaseActivity` and use view binding.
- Implement a `RecyclerView` to display use case options:
    - Planting and Harvest Dates
    - Cassava Market Outlet
    - Current Cassava Yield
- Track completion status of each use case and update the UI accordingly.
- Use `ActivityResultLauncher` to handle navigation and results from sub-activities.

- Refactor(ui): Animate selection state in adapters

- Implement background and text color animations for selected items in `MaizePerformanceAdapter` and `CassavaYieldAdapter`.
- Use `animateCardBackground` and `animateTextColor` extension functions for smoother UI transitions.
- Update color resources from `color_accent` to `color_focus` and `color_on_primary` for better theme consistency.

- Refactor(ui): Remove text color animation from adapters

- Drop `animateTextColor` in `MaizePerformanceAdapter` and `CassavaYieldAdapter` for simplified UI logic.

- Refactor(theme): Update gradient overlay color

- Change the end color of the gradient overlay from a semi-transparent black to `color_primary_variant`.
- Use `@android:color/transparent` for the start color.

- Refactor(recommendations): Tweak recommendation card image layout

- Reduce card elevation from 4dp to 2dp.
- Adjust margins for the title `TextView` to use standard spacing dimensions.
- Change title text color to `@color/color_on_primary`.
- Remove text shadow and styling attributes.
- Allow title to wrap by setting `ellipsize` to `none`.

- Refactor(recommendation): Update intercropping with sweet potato use cases

- Replace `MAIZE_MARKET_OUTLET` with `SWEET_POTATO_MARKET_OUTLET` in the use case options.
- Remove `MAIZE_PERFORMANCE` use case.
- Update intent mapping to direct `SWEET_POTATO_MARKET_OUTLET` to `CassavaMarketActivity`.

- Refactor(ui): Adjust window insets handling in BaseActivity

- Add support for system bars insets using `ViewCompat` and `WindowInsetsCompat`.
- Apply padding adjustments to the root view based on insets.

- Refactor(recommendation): Invert icon visibility logic

The constructor parameter `hideIcon` is renamed to `showIcon` to better reflect its purpose. The conditional logic for displaying the completion badge icon is updated to check for `showIcon` being true instead of `hideIcon` being false.

- Refactor(recommendations): Clean up and enable recommendation options

- Remove unused `Toast` import and display logic.
- Re-enable "Best Planting Practices" and "Scheduled Planting" advice options.
- Replace empty `else` block with `Unit` for conciseness.
- Simplify adapter configuration by using `showIcon = false`.
- Remove redundant `null` case in `when` expression for intent creation.

- Refactor(ui): Re-enable stepper fragments and integrate `SimpleStepperListener`

- Refactor(ui): Use string resources for exit dialog text in `HomeStepperActivity`

- Refactor(ui): Simplify `sizeUnitLabel` retrieval in `WeedControlCostsActivity`

- Refactor(db): Re-order imports and cleanup in `AppDatabase`

Re-organized import statements for better readability and removed a redundant commented-out line for `fallbackToDestructiveMigration()`.

- Refactor(location): migrate to mapbox maps sdk v10

The Mapbox Maps SDK has been updated from v6 to v10. This migration improves performance and ensures future compatibility.

- Refactor(permissions): modernize permission handling logic

Refactor the permission handling logic to use the modern Activity Result
API, improving code clarity and lifecycle safety. This removes the
deprecated `onRequestPermissionsResult` method and centralizes permission
checks in a new `PermissionHelper` class.

- Refactor(permissions): abstract location permission checks

Centralize location permission logic into the PermissionHelper class. This
improves reusability and simplifies permission handling across the app.

- Refactor(maps): migrate LocationPickerActivity to Mapbox SDK v11

This commit migrates the LocationPickerActivity from the deprecated
Mapbox SDK v10 to the current v11, ensuring future compatibility and
leveraging modern APIs. The implementation is updated to use coroutines
for location fetching, annotations plugin for markers, and the new
location component.

- Refactor(location): modernize location fetching logic

Update location helper to use modern APIs and improve robustness.
This replaces the deprecated `LocationRequest.Builder` and adds a
fallback to `lastLocation` for older Android versions.


### Features

- Feat(security): update network security configuration

- Added a `base-config` to trust system-provided certificates.
- This ensures that the application trusts the default set of CAs provided by the Android system for secure connections.

- Feat(SplashActivity): initialize Retrofit with context

- Passed `this` (Context) to `RetrofitManager.init` to enable context-dependent initializations within the Retrofit manager.
- Commented out the developer mode shortcut to `RecommendationsActivity` to ensure the standard app flow.

- Feat(security): add network security configuration

- Added `android:networkSecurityConfig="@xml/network_security_config"` to the `application` tag in `AndroidManifest.xml`.

This change allows for customization of network security settings, such as trusting specific CAs or disabling cleartext traffic.

- Feat:migration to git history

- Feat(recommendations): Add more advice options

- Enable Scheduled Planting and Harvest (SPH) recommendations.
- Add Intercropping with Maize (IC_MAIZE) recommendation.
- Add Intercropping with Sweet Potato (IC_SWEET_POTATO) recommendation.
- Disable Best Planting Practices (BPP) recommendation for now.

- Feat(recommendations): Enable more advice options for IC Maize

- Enable Available Fertilizers (CIM) recommendation.
- Enable Planting and Harvest recommendation.
- Enable Cassava Market Outlet recommendation.

- Feat(build): Define base URLs in default config and remove from release/debug

Moves the `buildConfigField` declarations for `AKILIMO_BASE_URL` and `FUELROD_BASE_URL` into the `defaultConfig` block. This centralizes the base URL definitions and removes their redundant declarations from the `release` and `debug` build types.

- Feat(build): Automate `versionName` and `versionCode` generation

Added functions in `app/build.gradle.kts` to compute `versionName` and `versionCode` dynamically using environment variables or default values. Writes the release version name to `nextrelease.txt`.

- Feat(deps): update mapbox sdk and add new dependencies

Upgrade Mapbox SDK from v9 to v11 and add SwipeRefreshLayout and
Play Services Location to support new map features and UI interactions.

BREAKING CHANGE: The Mapbox SDK has been upgraded from v9 to v11.
This requires significant code changes for map integration due to
API differences between the major versions.

- Feat(res): add location pin icon

Add a vector drawable for the location pin icon to be used in the app's user interface.

- Feat(deps): integrate swipe-to-refresh and location services

Add SwipeRefreshLayout for UI enhancements and Google Location Services for location-based features. This also removes unused Mapbox annotation and places plugins.

- Feat(location): add location helper utility

Create a new helper class to abstract the logic for obtaining the
user's current GPS location. This simplifies location fetching.

- Feat(permissions): add permission helper class

Create a centralized helper class to manage all location and other runtime permission checks and requests. This improves code reusability and simplifies permission handling logic across the application.

- Feat/16kb support 


### Miscellaneous Tasks

- 🔧 ci(workflows): exclude `android-19` branch from `push` triggers

- Updated `merge-to-develop.yml` to ignore `android-19` branch on push triggers.

- ✨ feat(proguard): add rules for Jackson annotations and Java 8 compatibility

- Preserve Jackson-specific classes and annotations in ProGuard rules
- Add rules for Java 8 features to prevent potential BootstrapMethodError

- 🗑️ chore(build): remove commented-out dependencies from build.gradle

* Cleaned up the build.gradle file by removing unnecessary commented-out dependencies.
* This helps improve readability and maintainability of the build configuration.

- 🔧 chore(build): update dependencies and configuration settings

* Upgraded `okhttp` and `retrofit` libraries to latest versions.
* Migrated from Jackson to Moshi for JSON parsing.
* Updated Kotlin and Java compatibility settings to version 21.
* Refined build configuration for better clarity and consistency.

- Chore(Akilimo): remove comment from AndroidThreeTen initialization

- Removed an unnecessary comment from the AndroidThreeTen initialization line.

The `AndroidThreeTen.init(this)` call is self-explanatory for initializing the library, so the comment was redundant.

- ✨ feat(layout): add NestedScrollView to WelcomeFragment

- Wrapped the `ConstraintLayout` in `fragment_welcome.xml` with a `NestedScrollView`.
- Adjusted margins for child views within the `ConstraintLayout` to improve spacing.

This change allows the content of the WelcomeFragment to scroll if it exceeds the screen height, improving usability on smaller devices.

- ✨ feat(network): add custom SSLSocketFactory for older Android versions

- Updated `RetroFitFactory` to include a custom `SSLSocketFactory` for Android versions below 7.1.1 (Nougat MR1).
- This involves loading a custom CA certificate (`isrg_root_x1.pem`) from raw resources to ensure secure connections on older devices.
- The `RetrofitManager` now requires a `Context` to facilitate loading of the certificate.
- The `OkHttpClient` builder is now configured to use this custom SSL context if the Android version is below N_MR1.

This change enhances compatibility and security for users on older Android devices by addressing potential SSL/TLS issues.

BREAKING CHANGE: The `RetrofitManager` now requires a `Context` to be passed during its initialization. This is necessary to load a custom CA certificate for improved SSL/TLS compatibility on Android versions below 7.1.1 (Nougat MR1).

- 🔧 ci(workflow): enable artifact builds for fix branches

- Modified the `build_artifacts` job in `android.yml` to trigger on branches containing `fix/` in addition to `beta` and `main`.

This change allows for the generation of build artifacts for bugfix branches, facilitating testing and deployment of fixes.

- 🔧 chore(ci): restrict artifact builds to beta and main branches

- Updated the `build_artifacts` job in `android.yml` to only run on `beta` and `main` branches.
- Removed the condition to build artifacts for branches containing `fix/`.

This change streamlines the CI process by ensuring artifacts are only built for designated release branches, reducing unnecessary builds for fix branches.

- ⚡️ perf(fragments): remove IFragmentCallBack and use Fragment Result API

- Removed the `IFragmentCallBack` interface and its implementations in `HomeStepperActivity`.
- Updated `WelcomeFragment` to use the Fragment Result API for communication with its parent by introducing `CLOSE_REQUEST_KEY`.
- Removed the deprecated `onAttachFragment` override in `HomeStepperActivity`.

This change improves fragment communication by leveraging the modern Fragment Result API, reducing boilerplate and potential memory leaks associated with direct interface callbacks.

- ♻️ refactor(strings): rename `exact_fertilizer_price` to `exact_price`

- Renamed the string resource `exact_fertilizer_price` to `exact_price` in English, Kinyarwanda, and Swahili translations.
- Added a new string `lbl_error_invalid_cost` for invalid cost input.

This change generalizes the string resource for specifying an exact price, making it reusable beyond just fertilizer.

- ♻️ refactor(TractorAccessActivity): use view binding and clean up code

- Migrated to view binding by removing manual `findViewById` calls and using the generated binding class.
- Replaced direct member variable access with `with(binding)` blocks for cleaner view interaction.
- Simplified logic for setting visibility of tractor implement options.
- Refactored dialog creation and handling for operation costs.
- Reduced redundant string formatting for dialog titles and hints by creating a helper function `getCostTitle`.
- Updated data saving logic to be more concise.
- Utilized enum names directly for operation names in `loadOperationCost` and dialog callback.

- ✨ feat(inherit): add BaseBottomSheetDialogFragment

- Introduced a new abstract class `BaseBottomSheetDialogFragment` extending `BottomSheetDialogFragment`.
- This class provides common dependencies like `mathHelper`, `sessionManager`, `database`, and `currencySymbol` for bottom sheet dialog fragments.

- ♻️ refactor(CostBaseActivity): migrate to use ViewBinding

- Changed `CostBaseActivity` to inherit from `BindBaseActivity<T>` instead of `BaseActivity`.
- This allows `CostBaseActivity` and its subclasses to leverage ViewBinding for safer and more efficient view access.

- ✨ feat(app/src/main/res/values/dimens.xml): Added new dimension value
- Modified existing dimen_100 to 105dp to maintain a consistent spacing
- Added a new dimen_250 for better responsive design

No breaking changes were made in this commit.

- ♻️ refactor(entity): update CurrentPractice entity with ColumnInfo and enums

- Renamed table from `current_practice` to `current_practices`.
- Added `@ColumnInfo` annotations to all fields for explicit database column mapping.
- Changed `weedControlTechnique` to `weedControlMethod` and updated its type from `String?` to `EnumWeedControlMethod`.
- Updated types for `ploughingMethod`, `ridgingMethod`, and `harrowingMethod` from `String?` to `EnumOperationMethod`.
- Initialized `weedRadioIndex` to -1.

- ♻️ refactor(SummaryFragment): simplify string building and improve context usage

- Updated `buildFieldInfo`, `buildPloughStr`, and `buildRidgeStr` methods to accept `Context` as a parameter, reducing repetitive calls to `requireContext()`.
- Simplified logic in `buildPloughStr` and `buildRidgeStr` by using `when` expressions with `EnumOperationMethod`.
- Removed unused `pickedLocation` variable.
- Removed empty `onError` method override.

- ♻️ refactor(TillageOperationFragment): improve data handling and UI updates

- Replaced `CheckBox` with `MaterialButtonToggleGroup` for tillage operations.
- Data is now loaded asynchronously using `lifecycleScope` and `withContext` for database operations.
- UI updates are handled by `updateUiFromCurrentPractice` after data loading or modification.
- `handleTillageOperationChange` now manages changes in tillage operations and triggers saving.
- `showOperationsDialog` uses `EnumOperation` and updates `currentPractice` based on dialog results.
- `saveCurrentPracticeToDatabase` saves the `currentPractice` entity asynchronously.
- Improved logging for database operations and error handling.
- Implemented `DefaultDispatcherProvider` for managing coroutine dispatchers, enhancing testability.
- Removed deprecated data refreshing logic in `onSelected`.
- Ensured `_binding` is set to null in `onDestroyView` to prevent memory leaks.

- ✨ feat(viewmodel): add TillageOperationViewModel and Factory

- Introduced `TillageOperationViewModel.kt` to manage UI-related data for tillage operations.
  - It handles loading and saving `CurrentPractice` data to the database.
  - Exposes `LiveData` for `currentPractice`, `dataIsValid`, and `errorMessage`.
  - Includes methods to update ploughing and ridging operations.
  - Uses `viewModelScope` for coroutine-based asynchronous operations.
  - Integrates Sentry for error reporting.

- Added `TillageOperationViewModelFactory.kt` to provide instances of `TillageOperationViewModel`.
  - This factory is necessary because `TillageOperationViewModel` has constructor dependencies (Application).

- ✨ feat(inherit): add BindBaseStepFragment for ViewBinding

Introduced `BindBaseStepFragment`, a new abstract base class for fragments that utilize ViewBinding within a Stepper layout.

- This class simplifies ViewBinding setup by handling binding inflation and lifecycle management.
- Subclasses must implement `inflateBinding` to provide the specific binding class and `onBindingReady` to perform actions once the binding is initialized.
- It extends `BaseStepFragment` and provides a default implementation for `verifyStep`.

- ♻️ refactor(BaseFragment): migrate to ViewBinding

- Introduced type parameter `T : ViewBinding` to `BaseFragment`.
- Added a private nullable `_binding` property and a protected non-null `binding` property for accessing the view binding.
- Implemented `inflateBinding` as an abstract method to be overridden by subclasses for inflating their specific layout.
- Modified `onCreateView` to use `inflateBinding` for setting up the fragment's view.
- Removed the abstract `loadFragmentLayout` method as its functionality is now handled by `inflateBinding`.
- Removed the `loadLocationInfo` utility method.
- Removed `onCreate` override, as `setHasOptionsMenu(false)` and `appVersion` initialization can be handled by subclasses or alternative lifecycle methods.

- ✨ feat(LocationFragment): initialize and update mapBoxToken variable

- Replaced MAP_BOX_ACCESS_TOKEN with mapBoxToken for consistency
- Added countryCode and countryName variables for better state management

This update refactors the code to use a more explicit variable naming for the Mapbox token and introduces additional variables for country information, improving code clarity and future maintainability.

BREAKING CHANGE: The variable MAP_BOX_ACCESS_TOKEN is replaced by mapBoxToken; ensure all references are updated accordingly.

- ♻️ refactor(BioDataFragment): improve code readability and data handling

- Renamed variables for clarity (e.g., `phoneIsValid` to `isPhoneValid`, `myMobileCode` to `selectedMobileCode`).
- Simplified spinner item selection logic by directly assigning values or using `getOrNull`.
- Removed redundant null checks and used more concise Kotlin idioms (e.g., `isBlank()` instead of `TextUtils.isEmpty()`).
- Updated `refreshData` to directly use `userProfile` properties instead of intermediate variables.
- Streamlined `saveBioData` by removing intermediate variables and directly using binding values.
- Replaced `indexOfValue` lookup in `refreshData` for spinner selection with direct index if available or using a helper function.
- Consolidated `update` and `insert` database operations into a single `insert` call as Room handles upserts.
- Removed unused `onError` method.

- ✨ feat(data): add indexOfValue extension function to InterestOption list

- Enhance utility for retrieving index based on value
- Default to zero if value not found

This addition simplifies searching for specific interest options within lists, improving code readability and efficiency.

- ✨ feat(data): update indexOfValue function in InterestOption.kt

- Return -1 when value is not found, instead of defaulting to 0
- Improve accuracy of index lookup for interest options

This change corrects the indexOfValue function to properly indicate when a value does not exist in the list, enhancing reliability and correctness.

- ✨ feat(data): refactor InterestOption to use ValueOption interface

- Enhanced type safety and flexibility by introducing ValueOption interface
- Updated list extension function to operate on List<ValueOption> instead of concrete classes

This change allows for better scalability and easier integration of different option types, such as CountryOption alongside InterestOption.

BREAKING CHANGE: The list extension function now requires a List of ValueOption instead of List<InterestOption>.

- ✨ feat(dao): remove unused update method from UserProfileDao

- Removed the deprecated update function to streamline the DAO interface
- Simplified codebase by eliminating redundant method

This change refactors the UserProfileDao by removing an unused update function, reducing potential confusion and maintenance overhead.

BREAKING CHANGE: The update method is no longer available in UserProfileDao. Existing code relying on this method will need to be updated accordingly.

- ✨ feat(app): Enhance CountryFragment with CountryPickerDialog and allowed countries

- Limited country options to Nigeria and Tanzania
- Improved country selection flow with dialog interface

This update refactors the country selection logic by replacing the previous AlertDialog with a dedicated CountryPickerDialogFragment, enhancing user experience. Additionally, the list of selectable countries is restricted to specific allowed countries, ensuring relevant options are presented to users.

BREAKING CHANGE: The country selection UI has been changed from a dialog to a custom fragment-based picker, and only Nigeria and Tanzania are now available for selection.

- ✨ feat(entities): Remove redundant index tracking fields in UserProfile

- deleted selected_gender_index, selected_interest_index, selected_country_index, selected_risk_index fields
- cleaned up data model for simplicity and accuracy

These changes streamline the UserProfile entity by eliminating unnecessary index tracking properties, reducing potential bugs and improving code clarity.

BREAKING CHANGE: The removed fields will no longer be accessible; ensure dependent components are updated accordingly

- ✨ feat(profiles): Update profile data handling and DAO operations

- Changed profile update calls from update() to insert() to ensure proper data persistence
- Modified fragment lifecycle methods to replace deprecated or incorrect overrides with onBindingReady
- Refactored various fragment logic to improve data validity checks and UI interaction
- Improved enum class EnumInvestmentPref with string resource IDs and associated risk values
- Fixed email validation logic in BioDataFragment.kt to correctly validate emails
- Updated DstRecommendationActivity to insert or update profile accordingly
- Replaced fragment callback setup with onBindingReady for consistent initialization
- Enhanced code consistency, readability, and adherence to best practices across fragments

BREAKING CHANGE: Profile DAO operations now use insert() instead of update() to prevent data loss; ensure DAO is configured for conflict handling if needed.

- ♻️ refactor(layout): migrate dialog_field_size.xml to Material Components

- Replaced `androidx.appcompat.widget.LinearLayoutCompat` with `LinearLayout`.
- Updated `TextView` for dialog title to use `TextAppearance.MaterialComponents.Subtitle1` and `?attr/colorOnBackground`.
- Replaced `EditText` with `com.google.android.material.textfield.TextInputLayout` and `com.google.android.material.textfield.TextInputEditText` for the field size input.
- Replaced `androidx.appcompat.widget.AppCompatButton` with `com.google.android.material.button.MaterialButton` for submit and cancel buttons.
- Applied Material Design styles (`AppMaterialButton`, `AppMaterialButton.Outline`) to the buttons.
- Adjusted layout structure and spacing for consistency with Material Design guidelines.

- ✨ feat(dialog): update DateDialogPickerFragment to use DialogFragment and improve date handling

- Refactors the class to extend DialogFragment instead of AppCompatDialogFragment
- Implements DatePickerDialog.OnDateSetListener directly
- Adds factory methods for creating instances for planting and harvesting dates
- Simplifies date initialization and includes better date range logic
- Replaces targetFragment.onActivityResult with direct callback invocation for dismiss handling

This update modernizes the date picker dialog component, enhancing lifecycle management and clarity.

BREAKING CHANGE: The class now uses DialogFragment and a new dismissal callback system, replacing previous approach.

- ♻️ refactor(FieldSizeFragment): simplify logic and use view binding

- Replaced direct view manipulation with ViewBinding for safer and more concise code in `showCustomDialog`.
- Streamlined radio button selection logic by directly invoking `radioSelected` in click listeners.
- Simplified `setFieldLabels` by using an enum (`EnumAreaUnit`) to manage area unit labels and visibility.
- Renamed variables for clarity (e.g., `areaSize` to `areaSizeInput`).
- Removed redundant null checks and simplified conditional logic.
- Utilized Kotlin scope functions (e.g., `with`, `apply`, `let`) for better readability.
- Updated `onSelected` and `verifyStep` to reflect changes in data handling.

- 🎨 style(TillageOperation): update button styles and layout

- Changed `MaterialButtonToggleGroup` width to `match_parent` and added padding.
- Updated `MaterialButton` styles from `Widget.MaterialComponents.Button.OutlinedButton` to `AppMaterialButton.Outline`.
- Removed explicit `strokeColor` from `tillage_btn_ridging` as it's now handled by the style.

- ✨ feat(MathHelper): fix rounding logic and improve precision handling

- Corrected rounding from Math.round to Kotlin's roundToInt for consistency
- Fixed decimal place rounding to use proper scaling and division

This update enhances numerical accuracy and stability in mathematical computations within the MathHelper utility.

- ✨ fix(CountryFragment): remove redundant session update after database insertion

- Removed setting the country in session manager immediately after profile info insertion

This change ensures session data is only updated through the correct flow and prevents possible inconsistencies.

BREAKING CHANGE: The session manager's country is no longer set immediately after profile insertion within this method.

- 💄 feat(activity): improve system window fitting and add bottom padding

- Enabled `fitsSystemWindows` to handle insets properly.
- Added bottom padding using a large spacing dimension.

This change improves UI layout consistency and better handles system window insets.

- 💄 chore(layout): add bottom padding and enable system window fitting across activities

- Applied `fitsSystemWindows` for improved window inset handling.
- Added consistent bottom padding using a large spacing dimension for better UI layout.

- Chore: Update focus colors

- Update `color_focus` and `color_dark_focus`.
- Add new `color_focus_outline` and `color_dark_focus_outline`.

- Chore(strings): Update string resource keys for sweet potato

- Rename `lbl_sweet_potato_prices` to `lbl_market_outlet_sweet_potato`.
- Rename `lbl_how_do_you_sell_your_sweet_potato` to `lbl_sweet_potato_outlets`.
- Update translations for English, Kinyarwanda, and Swahili.

- Chore(enums): Update string resource key for sweet potato market outlet

- Chore(manifest): Add SweetPotatoMarketActivity to AndroidManifest

- Chore(enums): Comment out weed management advice

Temporarily disables the `WEED_MANAGEMENT` advice option in the `EnumAdvice` enum.

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

Add steps to the Android CI workflow to handle cases where the
`LATEST_TAG_FILE` might not exist.

If the tag file is missing, it will now be created with a default
value of `v1.0.0` to prevent the build from failing. This improves
the robustness of the release process.

- 👷 ci(android): Add caching for unit test results

Implement caching for unit test results in the Android CI workflow. This will skip running tests if the underlying source code and test files have not changed, speeding up the build process.

- Use `actions/cache@v4` to store and restore test results.
- Generate a cache key based on hashes of relevant source, test, and Gradle files.
- Add a step to check for a cache hit and conditionally skip the `testDebugUnitTest` task.

- 💚 ci(android): Disable release notes generation in workflow

Temporarily comment out the steps for generating and copying release notes within the Android CI workflow. This is a temporary measure to fix a failing build process. The release note generation script will be re-enabled after further investigation.

- 📝 docs: Add CHANGELOG file

Introduce CHANGELOG.md to track project history, including features, bug fixes, refactoring, and other significant changes.

The changelog is structured to follow Keep a Changelog principles, making it easier for contributors and users to see what has changed between releases.

- ♻️ refactor(.github): Overhaul release notes generation script

Refactor the `generate_release_notes.py` script to improve its structure, functionality, and robustness for creating localized Play Store release notes.

The script is now encapsulated in a `PlayStoreReleaseNotesGenerator` class, providing better organization and reusability. Key improvements include:
- Prioritization of changelog sections to ensure critical information appears first.
- Intelligent truncation to respect the 500-character Play Store limit without cutting off mid-sentence.
- Added support for generating version-specific changelogs (e.g., `28.1.0.txt`) alongside the general `whatsnew` file for F-Droid.
- Enhanced localization support and cleaner markdown parsing.
- Command-line arguments (`--preview`, `--changelog`, `--output`) for easier use in CI/CD pipelines and local development.

- Wrench(config): Update git-cliff configuration for AKILIMO

Overhaul the `cliff.toml` configuration to align with the specific needs of the AKILIMO project. This update refines the changelog's structure, header, footer, and commit grouping logic.

The previous generic configuration has been replaced with a more structured and descriptive setup. Changes include adding a detailed changelog header, customizing the body template for better readability, and simplifying commit group names. The configuration for remote repository details has also been added.

- 🔥 chore(fastlane): Remove versioned changelogs

Delete Fastlane metadata for version 28.1.0, including changelogs
and whatsnew files for all languages. This cleanup removes static,
version-specific files that are no longer needed in the repository.

- 📝 docs: Update and reformat changelog

Restructure and update the changelog to improve readability and conform to the "Keep a Changelog" standard. This change organizes all historical release notes under clear, versioned headings with categorized changes (e.g., Features, Bug Fixes, Refactor).

- Standardize changelog format for all releases.
- Group changes by type (Features, Fix, Refactor, etc.).
- Corrected various formatting inconsistencies and typos.

- 🚀 chore(build): Relocate whatsnew files to release directory

Move whatsnew files from the `distribution/` directory to the new `release/` directory to better align with the build and release process structure. This change consolidates release-specific assets.

- ♻️ refactor(script): Change release notes output directory

The output directory for the generated release notes has been moved
from a hardcoded path (`distribution/whatsnew`) to be relative to the
changelog file's location (`<changelog_parent>/release/distribution/whatsnew`).

This refactor makes the script more flexible and less dependent on a
specific project structure, improving its portability. A comment explaining
the new directory logic has also been added for clarity.

- 🚀 build(ci): Allow manual and PR-triggered workflow runs

Removes the branch restriction that limited the 'bump-and-tag' workflow to only run on the 'main' branch.

This change enables the workflow to be triggered manually via `workflow_dispatch` or by pull requests targeting any branch, providing more flexibility for testing and release processes.

- :memo:(whatsnew): Update Swahili release notes

Removes the English translation from the Swahili "What's New" file to keep the content language-specific.

- ♻️ refactor(ci): Improve release tagging and workflow reliability

Streamline the Android CI workflow by refining the tag fetching and versioning process. Reorder steps to ensure tag information is fetched correctly and consistently before being used. This change improves the robustness of the release creation process.

- :construction_worker: ci(android): Temporarily disable unit_test dependency

Temporarily disable the `unit_test` job dependency for the `build_artifacts` job.
This change sets the `cached_artifacts_exist` output to `false` to force a rebuild.

- 👷 ci(workflows): Simplify unit test trigger condition

Removes a redundant `||` from the `if` condition for the `unit_test` job in the Android CI workflow.

- 💚 fix(ci): Always run unit tests and fix artifact caching

- 💚 fix(ci): Correct release artifact upload step name

The job name for uploading release artifacts was mislabeled as
"Upload release artifacts" when it should be "Upload production artifacts"
to accurately reflect its purpose in the CI/CD pipeline.

- ♻️ refactor(ci): Remove -beta suffix from release tag

Simplify the release tagging process by removing the "-beta" suffix. The tag name is now read directly from the tag file, ensuring production release tags are clean.

- 🏗️ ci(android): Only update build number on successful release

Ensure the build number update script is only triggered after a
successful upload to either the beta or production track on the
Google Play Store. This prevents build number increments on
failed deployments.

- ```
🏗️ ci(android): Only update build number on successful release

Ensure the build number update script is only triggered after a
successful upload to either the beta or production track on the
Google Play Store. This prevents build number increments on
failed deployments.

- 👷 ci(.github): Rename workflow to Android CI

This commit updates the display name of the Android build workflow for better clarity in the GitHub Actions UI. The file itself remains unchanged, but this name change makes its purpose more immediately understandable from the list of workflows.

- ♻️ refactor(locale): Standardize locale definitions and usage

Explicitly type Locale objects for better type safety and clarity.
Remove unused `KenyaSwahili` and `Locale.ENGLISH` in favor of the
explicitly defined `English` constant. Add `RwandaKinyarwanda` to
the list of supported locales to expand language options.

- ♻️ refactor(ci): Simplify workflow trigger

Removes the `paths-ignore` configuration from the push trigger to ensure the workflow runs on every push to the main branch, simplifying the trigger logic.

- 👷 build(ci): Fix branch name for production release trigger

Corrects a typo in the GitHub Actions workflow file, changing 'mains' to 'main' to ensure that commits to the main branch correctly trigger the production release upload to Google Play.

- ♻️ refactor(ci): Improve release trigger conditions for bump-and-tag

Refine the `bump-and-tag` workflow to trigger only on merges to the `main` branch, preventing it from running on direct pushes. This ensures that version bumps and releases are created only after pull request reviews. The checkout action is also updated to fetch the full git history, enabling more accurate changelog generation.

- 🔥 chore(ci): Remove bump-and-tag workflow

The `bump-and-tag.yml` workflow has been removed as its functionality for creating release tags and GitHub releases has been consolidated into the main `android.yml` CI pipeline.

- Next release 

👷 build(ci): Fix branch name for production release trigger

Corrects a typo in the GitHub Actions workflow file, changing 'mains' to
'main' to ensure that commits to the main branch correctly trigger the
production release upload to Google Play.

---------

Co-authored-by: masgeekw <barsamms@gail.com>

- 👷 build(ci): Add Kotlin file pattern to todo-checker

Configure the todo-checker workflow to only scan for TODOs
in Kotlin (`.kt`) and Kotlin script (`.kts`) files.

- 💚 ci(workflows): Fix branch name in todo-checker

Correct the branch name from `develop` to `develops` to ensure the todo-checker workflow triggers correctly on push events.

- Ci/todo 

- ♻️ refactor(ci): Standardize cache key for build artifacts

Update the cache key in the GitHub Actions workflow to use a more
standardized and descriptive prefix. This improves clarity and
consistency across different workflows.

- ♻️ refactor(ui): convert location info card to constraintlayout

Refactor the location picker's info card from nested LinearLayouts to a more efficient ConstraintLayout. This simplifies the view hierarchy and improves layout performance.

- 👷 ci: improve build caching and versioning logic

Refactor the CI workflow to fetch the VERSION_CODE dynamically and use it to create more reliable cache keys for tests and builds. This ensures cache is properly invalidated when the version changes.

- 👷 ci: trigger workflow on yml changes

The CI workflow will now run when there are changes to any `.yml` files, ensuring that workflow modifications are properly validated.

- 👷 ci: improve build caching and versioning logic 

- 💥 feat(deps)!: migrate to mapbox v11 and modernize location handling

This commit migrates the app from Mapbox SDK v10 to v11, requiring significant refactoring of location and permission logic. It also introduces new dependencies for location services and updates the build configuration.

- Ci(release): automate changelog generation

Add a git-cliff action to the release workflow to automatically generate
and commit a CHANGELOG.md file.

- Next release 

🔥 chore(ci): Remove bump-and-tag workflow

The `bump-and-tag.yml` workflow has been removed as its functionality
for creating release tags and GitHub releases has been consolidated into
the main `android.yml` CI pipeline.

---------

Co-authored-by: masgeekw <barsamms@gail.com>


### Revert

- Revert(release): set next version to 28.0.0

Revert the next release version from 30.0.0 back to 28.0.0. This
change was made to align with an updated release strategy.


### Styling

- Style: remove unused imports

Remove unnecessary import statements across various files to improve code cleanliness.


## [26.3.0] - 2025-06-05

### Code Refactoring

- Refactor: move `initToolbar` deprecation notice

The `@Deprecated` annotation for the `initToolbar` function has been moved below the `setupToolbar` function in `BaseActivity.kt`.

- Refactor(FieldSizeFragment): simplify radio button selection logic

- Updated `setOnCheckedChangeListener` for `rdgFieldSize` to directly use `checkedId` and ensure a `RadioButton` exists before proceeding.
- The logic now only reacts to user presses, preventing programmatic selection changes from triggering `radioSelected`.

- Refactor: replace `MyBaseActivity` with `BaseActivity`


### Features

- Feat(icon): add back arrow icon

- Added a new vector drawable `ic_back.xml` for a back arrow.
- The icon is 24dp x 24dp and tinted black.
- It is auto-mirrored for right-to-left layouts.

- Feat: introduce `MyBaseActivity` as a base for activities

This commit introduces a new abstract class `MyBaseActivity` that extends `BaseActivity`.

The following methods from `BaseActivity` are deprecated and overridden to throw `UnsupportedOperationException` in `MyBaseActivity`:
- `initToolbar()`: Recommends using `setupToolbar(toolbar, titleResId)` instead.
- `initComponent()`: Marked for complete removal.
- `validate(backPressed: Boolean)`: Marked for removal.

- Feat(strings): add location selected prompt

- Added a new string resource `lbl_location_selected_prompt`.
- This string will be used to inform the user that a location has been selected and they can press OK to save and exit.

- Feat: introduce `BindBaseActivity` for ViewBinding

This commit introduces a new abstract class `BindBaseActivity<T : ViewBinding>` which extends `BaseActivity`.

This class simplifies the usage of ViewBinding in activities by:
- Providing a protected `binding` property to access the inflated ViewBinding instance.
- Abstracting the binding inflation logic into an `inflateBinding()` method that subclasses must implement.
- Handling the binding inflation in `onCreate()` and setting the content view.
- Nullifying the binding in `onDestroy()` to prevent memory leaks.


### Miscellaneous Tasks

- 🤖 ci(sonar-checks.yml): remove `paths-ignore` from push trigger

The `paths-ignore` configuration has been removed from the `push` trigger in the `sonar-checks.yml` workflow. This means SonarCloud analysis will now run on pushes to the `develop` branch regardless of the paths modified.

- 🤖 ci(sonar-checks.yml): disable shallow clone

- Configured `actions/checkout` to perform a full clone (`fetch-depth: 0`) instead of a shallow clone. This is often required for more accurate SonarQube analysis.

- 🤖 ci(sonar-checks.yml): add SonarQube Quality Gate action

- Added a new step "Wait for Quality Gate result" to the workflow.
- This step uses the `sonarsource/sonarqube-quality-gate-action@v1.1.0` action.
- It relies on `build/sonar/report-task.txt` for scan metadata.
- The `SONAR_TOKEN` secret is used for authentication.

- ✨ **ci(sonar-checks): update SonarQube Quality Gate action**

- Increased `pollingTimeoutSec` for Quality Gate result to 600 seconds.
- Commented out `scanMetadataReportFile` parameter.

- 🐛 **ci(sonar-checks): update scanMetadataReportFile path**

- Changed the path for `scanMetadataReportFile` from `build/sonar/report-task.txt` to `app/build/sonar/report-task.txt`.

- ♻️ ci(sonar-checks.yml): rename "Wait for Quality Gate result" step

The step "Wait for Quality Gate result" in the Sonar checks workflow has been renamed to "SonarQube Quality Gate check".
The `id` for this step has also been set to `sonarqube-quality-gate-check`.

- Refactor(activity_investment_amount.xml): update layout and component IDs

- Renamed various UI component IDs for clarity (e.g., `app_bar_layout` to `appBarLayout`, `collapsing_toolbar` to `collapsingToolbar`).
- Adjusted layout parameters, including `layout_height` of `AppBarLayout` and `CollapsingToolbarLayout`.
- Updated padding and margins for better spacing.
- Changed `EditText` to `TextInputEditText` within a `TextInputLayout` for improved Material Design compliance.
- Modified button style and properties.
- Added comments for better layout structure understanding.

- Refactor InvestmentAmountActivity

This commit refactors the `InvestmentAmountActivity` for improved readability and maintainability.

Key changes include:

- **View Binding:** Implemented view binding to replace `findViewById` calls, reducing boilerplate and improving type safety.
- **Simplified Logic:** Streamlined conditional logic and variable assignments.
- **Method Renaming:** Renamed methods for better clarity (e.g., `loadInvestmentAmount` to `loadInvestmentAmounts`, `addInvestmentRadioButtons` to `addRadioButtons`).
- **Constant Extraction:** Extracted hardcoded values into constants (e.g., `MIN_INVESTMENT_USD`, `INVALID_SELECTION`).
- **Code Organization:** Reorganized code blocks for better flow and readability.
- **Listener Setup:** Centralized listener setup in `setupListeners()`.
- **String Formatting:** Improved string formatting for labels and hints.
- **Deprecated Methods Removal/Marking:** Removed or marked unused/deprecated methods (`initToolbar`, `initComponent`, `validate`).
- **Input Validation:** Refined input validation logic for investment amount.
- **Error Handling:** Improved clarity in error messages and dialogs.
- **API Call:** Ensured API call for investment amounts is made with the correct `countryCode`.

- Refactor: improve layout structure and naming in `activity_recommendations_activity.xml`

- Updated ID names for better clarity (e.g., `app_bar_layout` to `appBarLayout`, `image_header` to `headerImage`).
- Removed `android:scrollingCache="true"` from `NestedScrollView` and `RecyclerView` as it's often not needed and can have performance implications.
- Simplified `NestedScrollView` by removing `android:clipToPadding="false"`.
- Adjusted padding and margins for better visual spacing within the layout.
- Added comments to delineate major sections of the XML (Collapsing AppBar, Scrollable Content).
- Set `android:layout_height="wrap_content"` for `RecyclerView` to allow dynamic height based on content.
- Changed `TextView` (`introText`) width to `match_parent` and centered the text.
- Standardized `textAppearance` for `introText`.

- Refactor: Update GPSTracker and location retrieval logic

- Refactored `GPSTracker` class:
    - Replaced `getLatitude()` and `getLongitude()` methods with `latitudeValue` and `longitudeValue` properties.
- Updated `LocationFragment` and `MapBoxActivity`:
    - Changed to use the new `latitudeValue` and `longitudeValue` properties from `GPSTracker`.
    - Added `@RequiresPermission` annotation to `currentLocation` getter in `LocationFragment` and `initCurrentLocation` method in `MapBoxActivity` to explicitly state the required location permissions.

- 🐞fix(fieldSize): correct data validation logic

- Initialized `dataIsValid` to `false` in `refreshData()` to ensure it reflects the current state.
- Set `dataIsValid` to `true` in `refreshData()` only if `areaSize` is greater than 0.
- Updated `saveData()` to set `dataIsValid` based on whether `areaSize` is greater than 0.
- Changed the condition in `verifyStep()` to check if `areaSize` is less than or equal to 0.0 (instead of just 0) for more precise validation.
- Removed the unused `dataValid` variable.

- Refactor: Remove deprecated `initToolbar` and `initComponent` methods from `MyBaseActivity`

- Removed the overridden `initToolbar` method, which was marked as deprecated and advised to use `setupToolbar(toolbar, titleResId)` instead.
- Removed the overridden `initComponent` method, which was also marked as deprecated.
- Modified the `validate` method to log an error message to Sentry and throw an `UnsupportedOperationException` with a more specific message.

- ✨ feat(app): add dispatcher provider interface and default implementation

- Introduced `IDispatcherProvider` interface for coroutine dispatchers
- Implemented `DefaultDispatcherProvider` using `Dispatchers`

Facilitates better testability and separation of concerns for coroutine dispatchers.

- ♻️ refactor(FertilizersActivity): migrate logic to ViewModel for improved separation of concerns

- Refactored `FertilizersActivity` by introducing `FertilizersViewModel` to handle business logic
- Simplified UI components by leveraging data-binding and LiveData observers

This refactor enhances maintainability and testability by isolating logic into the ViewModel.

- ✨ feat(release): add script to generate multilingual release notes

- Implemented a Python script to create English and Swahili release notes from the changelog
- Included processing for Features, Bug Fixes, and Code Refactoring sections

This addition automates release notes generation for improved efficiency and localization.

- ✨ feat(build): update dependencies to latest versions

- Upgraded 'com.google.android.material:material' to v1.12.0
- Added 'com.facebook.shimmer:shimmer' v0.5.0
- Updated 'com.github.blongho:worldCountryData' to v1.5.3

Improves app compatibility, visuals, and functionality by using updated libraries.

- ✨ feat(viewmodels): improve fertilizer data management and synchronization

- Added local caching and sync logic with `SharedPreferences`
- Enhanced error handling during fertilizer data fetch and sync

Refactored `FertilizersViewModel` to streamline fertilizer data operations, introducing background sync, dynamic use-case filtering, and runtime updates for UI.

- ✨ feat(FertilizerGridAdapter): enhance fertilizer management functionality

- Renamed `items` to `availableFertilizers` for better clarity
- Added `setFertilizer` method to update a specific fertilizer by position
- Updated logic to use `availableFertilizers` uniformly across methods

Improves code readability and adds support for updating individual fertilizers dynamically.

- ✨ feat(layout): update recommendation card layouts

- Migrated to Material Components for improved UI consistency and modern styling
- Enhanced structure with new IDs, spacing, and material design attributes

This refactor ensures better visual coherence and alignment with modern UI practices.

- ✨ feat(dimensions): add new spacing and dimension values

- Add `spacing_1dp` for finer spacing control
- Add `dimen_110` for extended dimension options

- ✨ feat(adapter): refactor to use ViewBinding in CropPerformanceAdapter

- Integrated ViewBinding for cleaner and more efficient view binding.
- Updated onBindViewHolder and onCreateViewHolder logic to work with ViewBinding.

- ♻️ refactor(FertilizersViewModel): streamline fertilizer loading and syncing logic

- Replaced repetitive coroutine logic with `launchWithState` and `launchIgnoreErrors` helpers.
- Simplified local fertilizer fetching and synchronization processes.
- Updated variable naming for consistency and readability.

This refactor improves code clarity, reduces duplication, and centralizes error handling logic.

- 💥 removal(fertilizer-recommendation): remove FertilizerRecommendationActivity and related resources

- Deleted FertilizerRecommendationActivity and its associated layout files
- Removed RecAdapter, related functionality, and dependencies

This change eliminates unused components and improves maintainability.

- ✨ feat(FertilizerRecActivity): enhance navigation and streamline imports

- Introduced improved intent handling for better navigation logic
- Updated and organized imports to include specific activity dependencies

These changes enhance readability and maintainability of the code, facilitating smoother navigation across activities.

- ✨ feat(layout): enhance recommendation card UI

- Updated icon visibility and tint for better prominence
- Adjusted text properties for improved readability and layout spacing

Enhanced visual clarity and usability of the recommendation card component.

- ✨ refactor(adapter): improve RecOptionsAdapter code clarity and structure

- Renamed variables for better readability (e.g., `items` to `recommendationsList`)
- Updated accessor methods to align with naming conventions
- Removed unused redundant `recIconSpacer` when `displayArrow` is false

Improves code maintainability and adherence to clean code principles.

- ✨ refactor(FertilizerGridAdapter): simplify view binding and remove unused code

- Replaced manual view binding with ViewBinding (ListFertilizerGridRowBinding)
- Removed OnLoadMoreListener interface and related unused methods

Streamlined code by adopting ViewBinding, reducing error-prone manual references, and removing unused functionality.

- ✨ feat(DatesActivity): improve error handling and cleanup imports

- Replaced wildcard imports with a specific import for `Calendar`
- Enhanced `Toast` context by explicitly specifying `this@DatesActivity` for better clarity


## [26.2.0] - 2025-05-29

## [26.1.0] - 2025-05-28

## [26.0.1] - 2025-05-28

## [26.0.0] - 2025-05-27

### Bug Fixes

- Fix(dev): comment out direct navigation to RecommendationsActivity

- During development, the app was navigating directly to `RecommendationsActivity`.
- This change comments out that direct navigation, allowing the app to follow the normal flow to `HomeStepperActivity`.

- Fix(WelcomeFragment): remove unused HomeStepperActivity import

The import of `com.akilimo.mobile.views.activities.HomeStepperActivity` was removed from `WelcomeFragment.kt` as it was no longer in use.

- Fix(app): remove unused libraries and update initialization

- Removed `AppLocale` and `ViewPump` library initializations as they are no longer used.
- Simplified `AndroidThreeTen` initialization.
- Removed logic related to setting `AppLocale.desiredLocale`.


### Code Refactoring

- Refactor: move operation cost response to entities and update endpoint to use country code

- Refactor: update field names in compute request object

The field names were updated to match the latest changes made in the database.
- Replace CIM with maizeInterCropping
- Replace CIS with sweetPotatoInterCropping
- Replace name with useCaseName
- Replace FR with fertilizerRecommendation
- Replace BPP with bestPlantingPractices
- Replace SPP with scheduledPlanting
- Replace SPH with scheduledPlantingHighStarch

- Refactor: replace deprecated `kotlinx.android.parcel.Parcelize` with `kotlinx.parcelize.Parcelize`

- Refactor: rename UseCases to UseCase and update related code

The diff renames the `UseCases` class to `UseCase` and updates the code that references it. It also updates `useCase.name` to `useCase.useCaseName` in `RootYieldActivity`.

- Refactor: update version code computation in `app/build.gradle`

The `computeVersionCode` function in `app/build.gradle` has been updated.
The method for converting the `VERSION_CODE` environment variable to an integer has been changed from `toInteger()` to `Integer.valueOf()`.
The default value remains `1` if the environment variable is not set.

- Refactor(db): update database name and reset version

- Changed the database name from `AKILIMO_MAY_2025` to `AKILIMO_JUNE_2025`.
- Reset the database version from `9` to `1`.

- Refactor(.github/workflows): simplify `merge-to-develop.yml` trigger

- Removed `paths-ignore` configuration.
- The workflow will now trigger for all file changes on pushes to branches other than `main`.


### Features

- Feat(sentry): ppdate AndroidManifest.xml

- Feat: update OperationCost entity to use Long for ID and add JsonProperty annotation

The OperationCost entity was modified to use `Long` instead of `Int` for the `id` field. Additionally, the `@JsonProperty` annotation was added to the `id` field.

- Feat: rename and refactor UseCaseDao to align with data layer principles

The `UseCaseDao` class has been refactored to better align with Android's data layer best practices. Changes include:

-   Rename methods for clarity: `listAll` to `getAll`.
- Added methods for better alignment with CRUD operations `insertAll` and `updateAll`
-  Update `UseCaseDao` class to match with entities `UseCase` to manage `UseCase` entities.
- Rename `user` parameter to `useCase` to reflect real type
- Added method to update a single `UseCase` entity.
- Renamed table name in queries from `use_case` to `use_cases`.

- Feat: Refactor field operation cost handling and update use cases

This commit refactors the way field operation costs are handled and updates the use cases within the application. The changes include:

-   **OperationCostsDialogFragment:** Removed `currencyCode` and `currencySymbol` as separate keys, instead of `currencySymbol` use the `currencyCode`.
-   **ScheduledPlantingActivity:** Changed from using the `UseCases` entity to `UseCase`, update field names in `UseCase` entity. `insert` to `insertAll` method of the `useCaseDao`
-   **FieldOperationCostsDao:** Added `insertOrUpdate` method to `FieldOperationCostsDao` instead of `insert`.
-   **ManualTillageCostActivity:** Removed an unused `hintText` field. Refactored `fieldOperationCost` to be a local variable instead of a class property. Refactored `loadOperationCost` method, updated passing parameters in the method.  Refactor of loading the `OperationCostsDialogFragment`
-   **WeedControlCostsActivity:** Updated `insert` to `insertOrUpdate` method for `FieldOperationCostsDao`
-   **InterCropRecActivity:** Changed from using the `UseCases` entity to `UseCase`, update field names in `UseCase` entity. `insert` to `insertAll` method of the `useCaseDao`
-   **PlantingPracticesActivity:** Changed from using the `UseCases` entity to `UseCase`, update field names in `UseCase` entity. `insert` to `insertAll` method of the `useCaseDao`
-   **FertilizerRecActivity:** Changed from using the `UseCases` entity to `UseCase`, update field names in `UseCase` entity. `insert` to `insertAll` method of the `useCaseDao`
-   **TractorAccessActivity:** Updated `insert` to `insertOrUpdate` method for `FieldOperationCostsDao`.Refactored `loadOperationCost` method, updated passing parameters in the method.

- Feat: add dimen_58 to dimens.xml

- Feat: add FertilizerRecommendationActivity and update AndroidManifest

- Added `FertilizerRecommendationActivity` to the manifest file.
- Reorganized elements within the `<application>` tag.
- Moved the `FertilizerRecommendationActivity` declaration to within the `<application>` tag.
- Adjusted indentation for better readability.

- Feat(locale): introduce language selection and persistence

- Added `LanguageOption` data class to represent language choices.
- Implemented `LanguageManager` object to handle saving, retrieving, and setting the application's locale.
  - `saveLanguage`: Persists the selected language code in SharedPreferences.
  - `getLanguage`: Retrieves the saved language code, defaulting to English ("en").
  - `setLocale`: Updates the application's context with the specified language.
- Removed the static `APP_LOCALES` list.
- Reordered `LOCALE_COUNTRIES` to place English (Nigeria) first.

- Feat(ui): add `dimen_36` to dimens.xml

A new dimension value `dimen_36` (36dp) has been added to the `dimens.xml` file for UI consistency.

- Feat(strings): add `lbl_remember_details` string resource

A new string resource named `lbl_remember_details` with the value "Remember details" has been added to `app/src/main/res/values/strings.xml`.

- Feat(BioDataFragment): use custom spinner adapter and simplify UI logic

- Replaced `ArrayAdapter` with a custom `MySpinnerAdapter` for gender and interest spinners.
- Removed the "Remember details" checkbox and its associated logic. User information will now always be remembered.
- Refactored UI element initialization and listener setup for better readability using `binding.apply`.
- Streamlined `ccp` (Country Code Picker) initialization.

- Feat(icon): update calendar icon

Replaced the custom calendar icon with a simpler, material design icon.

- Feat(permissions): add POST_NOTIFICATIONS permission

- Added the `android.permission.POST_NOTIFICATIONS` permission to `AndroidManifest.xml`.
- This permission is required for apps targeting Android 13 (API level 33) and higher to send notifications.

- Feat(security): disable cleartext traffic in AndroidManifest

The `android:usesCleartextTraffic` attribute has been set to `false` in the `AndroidManifest.xml` file. This change enhances security by preventing the application from using cleartext network communication (e.g., HTTP).

- Feat(backup): disable auto backup

Set `android:allowBackup="false"` in `AndroidManifest.xml` to disable automatic app data backups.

- Feat(sonar): configure connected mode

- Added `.sonarlint/connectedMode.json` to enable SonarLint connected mode.
- Set SonarQube URI to `https://sonar.munywele.co.ke`.
- Configured project key to `IITA-AKILIMO_akilimo-mobile_abcb50d1-1abd-4e32-bd76-b385f65cfc5d`.


### Miscellaneous Tasks

- 🚧 chore(workflows): update Android CI workflow for consistency and version upgrades

- Updated `actions/checkout` to v4 in all workflow jobs.
- Upgraded `actions/setup-python` to v5 and explicitly set Python version to 3.9.
- Moved `chmod +x ./gradlew` step to ensure proper execution in all relevant jobs.
- Removed redundant blank lines for cleaner workflow definition.

- ♻️ refactor(CassavaMarketActivity): streamline code by removing unused variables and improving structure

- Removed redundant and unused variables such as `mathHelper`, `unitOfSaleEnum`, and `cassavaPriceList`.
- Streamlined logic by directly fetching required objects (e.g., `cassavaMarket`, `scheduledDate`).
- Replaced explicit nullable checks with safe call operators or default assignments.
- Enhanced readability by centralizing binding access and simplifying conditional blocks.
- Updated enum references to use `.name` and removed legacy methods like `.produce()`.

These changes improve readability, reduce clutter, and enhance code maintainability.

- ♻️ refactor(FertilizerPriceDialogFragment): simplify null checks and set default currency

- Set default value for `currencyCode` as "USD"
- Removed unnecessary null checks for `database`
- Simplified logic for fetching fertilizer prices

These changes improve code readability and reduce null-check redundancies.

- ✨ feat(MathHelper): add default value for decimalPlaces in roundToNDecimalPlaces

- Set `decimalPlaces` default parameter to 2.0 for improved usability.
- Simplifies function usage when specific precision is not provided.

- ♻️ refactor(BaseActivity): clean up deprecated variables and improve code quality

- Removed unnecessary `queue` variable and deprecated it.
- Deprecated `currency` variable and added a note for review.
- Refactored `currencyName` initialization to use `EnumCountry.Nigeria.currency()`.
- Updated Animatoo animations to explicitly reference `this@BaseActivity`.

Minor adjustments to reduce redundancy and improve clarity.

- ♻️ refactor(InvestmentAmountActivity): streamline logic and optimize code readability

- Removed unnecessary variable declarations and redundant initialization of `invAmount`.
- Replaced usage of hardcoded `List<InvestmentAmount>` with local variable context for better handling.
- Simplified logic for `btnFinish` click listener by reducing redundant checks and assignments.
- Improved readability by refactoring the `addInvestmentRadioButtons` and `setLabel` methods.
- Eliminated unused parameters and dead code for cleaner implementation.
- Updated logic for radio button tagging using `itemTag` for consistency.
- Ensured safe usage of `currencyCode` and `currencySymbol` with validation.

This refactoring enhances maintainability and improves overall code quality without altering functionality.

- ✨ **feat(dao): update query to use item_tag for fetching investment amounts**

- Updated `findOneByInvestmentId` to `findOneByItemTag` in `InvestmentAmountDao`.
- Changed query parameter from `id` to `item_tag` for more precise filtering.

> Refines the query logic to better align with new requirements.

- ✨ feat(build): migrate from Joda-Time to ThreeTenABP

- Replaced `android.joda` with `com.jakewharton.threetenabp` for modern date and time handling
- Removed `joda-time` from test dependencies

This improves compatibility with Java 8 Time API and ensures better long-term support.

- ✅ test(build.gradle): add dependencies for threetenabp and mockito-core testing

- Added `threetenabp` (1.4.9) and `mockito-core` (4.5.1) to `testImplementation` for improved test support.
- Enhances test capability for date-time handling and mocking in unit tests.

- ✨ feat(utils): enhance date formatting flexibility in DateHelper

- Added support for dynamic date format input in `formatToLocalDate` and `olderThanCurrent`
- Removed unused `dateTimeFormat` and its associated formatter to clean up the code

These changes provide greater flexibility in handling date formats and improve maintainability.

- ✨ feat(utils): add default date format parameter to `olderThanCurrent` function

- Set default date format to `dd/MM/yyyy` in `olderThanCurrent` function.
- Simplifies usage by allowing calls without explicitly specifying a date format.

- ✨ feat(DatesActivity): streamline date handling and binding usage

- Removed unused `scheduledDate` variable and updated logic to use a local instance.
- Enhanced date formatting with a customizable `dateFormat`.
- Replaced outdated view references with `binding` for better readability and maintainability.
- Refined `DateHelper` usage by directly importing specific methods.
- Simplified null checks and improved clarity in database operations.

- ♻️ refactor(models): migrate `OperationCost` from `models` to `entities` package

- Updated references to `OperationCost` across multiple classes (`OperationCostsDialogFragment`, `ManualTillageCostActivity`, `CostBaseActivity`, and `TractorAccessActivity`).
- Ensured consistency by relocating `OperationCost` and `OperationCostResponse` to the `entities` package.

This change improves code organization and better reflects domain-driven design principles.

- ✨ refactor(entities): improve and rename `UseCases` entity structure

- Renamed `UseCases` to `UseCase` for better singular naming consistency.
- Updated table name from `use_case` to `use_cases`.
- Introduced `@ColumnInfo` annotations for explicit column definitions and indexing.
- Renamed and clarified property names for greater readability and context.

These changes enhance code clarity and improve Room database schema definitions.

- ✨ feat(dao): add `OperationCostDao` interface for database operations

- Implemented DAO for `OperationCost` entity with CRUD methods
- Included queries to fetch, insert, update, and delete operation costs
- Provided a method to delete all records from the `mandatory_info` table

Enables efficient interaction with the database for `OperationCost` data management.

- ✨ feat(CostBaseActivity): enhance operation cost loading with local database support

- Added local database filtering for operation costs before making API calls.
- Removed `operationCostList` property; costs now fetched directly from DAO or API.
- Improved efficiency by caching operation costs in the database.

This reduces unnecessary API calls and optimizes data retrieval.

- ✨ feat(activity): enhance ManualTillageCostActivity dialog handling

- Replaced `operationName` with `operationType` in `showDialogFullscreen` for better clarity.
- Removed redundant `operationCostList` parameter in dialog arguments.
- Simplified locale handling for `translatedUnit` via `getCurrentLocale()`.
- Eliminated cost rounding logic, directly using `selectedCost` for more precise calculations.
- Refactored redundant string formatting for Swahili locale handling.

This update improves code readability, dialog parameter clarity, and ensures more accurate cost calculations.

- ✨ feat(TractorAccessActivity): enhance operation cost loading with tractor-specific operation type

- Updated `loadOperationCost` to use `EnumOperationType.TRACTOR.name` for better clarity and consistency.
- Added `operationType` parameter to `showDialogFullscreen` for improved dialog handling.
- Removed redundant rounding logic for cost calculations, allowing direct retention of selected values.

These changes improve the flexibility and accuracy of operation cost management.

- ✨feat(TillageOperationFragment): streamline dialog management and simplify operation type handling

- Introduced `showDialogFragmentSafely` utility for cleaner dialog transactions.
- Replaced `operationName()` with `name` for EnumOperationType properties, reducing redundancy.
- Removed redundant fragment transaction code for improved readability and maintainability.

- ✨ refactor(RecommendationsActivity): simplify and optimize recommendation initialization

- Refactored repetitive recommendation object creation using `apply` for cleaner and more concise code.
- Renamed variable `currency` to `currencyCode` for consistency.
- Commented out unused `useCase` member variable.
- Adjusted `useCase` handling logic within the recommendation intent.

These changes improve code readability, maintainability, and consistency without altering functionality.

- ✨ feat(layout): add new button layout to recommendations dialog

- Include `bottom_two_buttons_layout` in the recommendations channel dialog.
- Assigned the ID `@+id/two_buttons` to the new layout for easier reference.

This update enhances UI functionality by introducing a reusable button layout.

- ✨ feat(ui): enhance bottom button layout with Material Design components

- Replaced `AppCompatButton` with `MaterialButton` for modern styling.
- Updated layout properties for better alignment, spacing, and elevation.
- Applied Material Design theming, including button styles, corner radius, and stroke properties.
- Increased padding and adjusted dimensions for improved UI consistency.

- ✨ feat(layout): update bottom button layout to use Material Design components

- Replaced `AppCompatButton` with `MaterialButton` for improved consistency with Material Design.
- Adjusted button dimensions, padding, and styling for enhanced UI/UX.
- Added elevation and background attributes for better visual hierarchy.

This update modernizes the layout and aligns it with Material Design principles.

- ✨ feat(InterCropRecActivity): refactor and streamline recommendation logic

- Removed unused variables and redundant member fields for cleaner, minimal code.
- Replaced nullable accessor calls with `let` to simplify null checks and improve readability.
- Consolidated adapter setup for `RecyclerView` to eliminate duplications.
- Converted repetitive string resource retrievals to a more dynamic approach.
- Improved navigation intent initialization with a `when` expression, reducing boilerplate code.
- Refactored `recItems` generation to use `listOf` and grouped logic for Nigeria and Tanzania.

These changes improve code clarity, reduce redundancy, and enhance maintainability.

- ✨ feat(layout): simplify and modularize `activity_dates.xml` UI structure

- Reorganized layout by replacing inline components with reusable `<include>` elements for `card_planting` and `card_harvest`.
- Improved spacing and padding consistency using standardized dimensions.
- Removed outdated or redundant attributes, like `expandedTitleTextAppearance` and `contentInsetStartWithNavigation`.
- Added comments to clarify layout sections for better maintainability.
- Introduced a `Group` to manage `card_harvest` and `card_planting` visibility collectively.

These changes enhance code readability, reusability, and maintainability.

- ✨ feat(adapters): add `RecAdapter` for handling recommendation options

- Introduced `RecAdapter` to efficiently bind and display recommendation items in a RecyclerView.
- Utilized data binding for cleaner layout inflation and view handling.
- Added logic to dynamically update item status icons and colors based on completion state.

- ♻️ refactor(FertilizerRecActivity): streamline and optimize activity logic

- Refactored `FertilizerRecActivity` to extend `BaseRecommendationActivity` instead of `BaseActivity`.
- Removed redundant variables and methods, consolidating data initialization into `getRecommendationOptions()`.
- Improved RecyclerView setup by streamlining toolbar and adapter initialization.
- Simplified lifecycle handling by removing unused/duplicated methods like `setAdapter()`, enhancing clarity and maintainability.

- ♻️ refactor(activity): streamline `InterCropRecActivity` logic

- Replace `BaseActivity` with `BaseRecommendationActivity`
- Consolidate `recItems` generation into `getRecItems()`
- Simplify toolbar and initialization logic
- Optimize `useCase` assignment with a concise `when` expression

- ✨ **feat(RecOptionsAdapter): update item data with animation and improve efficiency**

- Updated constructor to accept item list directly for better initialization.
- Refactored `setData` to support updating a specific item with `notifyItemChanged`.

Enhances performance and avoids redundant UI updates.

- ✨ **feat(build): enhance versioning logic with branch-specific rules**

- Updated `computeVersionName` to handle `dev` branch with version adjustments.
- Simplified `computeBuildNumber` by removing beta-specific logic.
- Ensured `computeVersionCode` assigns a fixed code for `dev` branch.

Refactored and enhanced versioning strategy to support branch-based differentiation.

- ✨ feat(utils): update `convertToUnitWeightPrice` to support Double values for unitWeight

- Changed `unitWeight` parameter type from Int to Double in `convertToUnitWeightPrice` function
- Updated relevant test cases to use Double for `unitWeight` values

This enhances precision by allowing decimal unit weights.

- ✨ feat(utils, request): improve type safety & add JSON naming strategy

- Updated `convertToUnitWeightPrice` to use `Double` for `unitWeight` for better precision.
- Adjusted unit tests to reflect updated method signature.
- Added `JsonNaming` annotation with `SnakeCaseStrategy` to `UserInfo` class.
- Renamed fields in `UserInfo` to align with snake_case conversion.

Improves data handling consistency and maintains JSON API standards.

- ♻️ refactor(utils): update unitWeight() to return Double instead of Int

- Changed `unitWeight()` return type in `EnumUnitOfSale` from `Int` to `Double`.
- Updated all relevant overrides to reflect the return type change.

Rationale: Provides greater precision and flexibility for unit weights when needed.

- ♻️ refactor(BuildComputeData): streamline and simplify compute data building logic

- Removed unused constants and variables for better maintainability
- Replaced manual null checks with Kotlin's `let` for concise and safe handling
- Improved readability by reducing redundancy in method implementations
- Removed dependency on Sentry for exception handling within some methods

This refactor enhances code readability, reduces cognitive load, and aligns better with Kotlin best practices.

- ✨ feat(request): add JSON property annotations to `ComputeRequest`

- Introduced `@JsonProperty` annotations to all fields for explicit JSON mapping.
- Renamed/standardized field names to follow snake_case convention.
- Enhanced data structure to align with JSON property requirements.

Provides better integration and compatibility with external JSON data sources.

- ✨ feat(FertilizersActivity): add use case distinction for fertilizer selection

- Introduced `useCase` variable to handle specific use cases from intent
- Updated `setupToolbar` to replace deprecated `initToolbar` method
- Enhanced fertilizer selection query to filter by use case
- Simplified logic for fetching fertilizers and adapter updates

Improves flexibility for fertilizer selection based on varying use cases.

- ✨ feat(utils): enhance fertilizer computation logic and add new profile fields

- Refactored fertilizer selection logic to handle specific intercropping cases (potato/maize).
- Added `interCroppedCrop` attribute to track intercropped crop type.
- Included `emailAddress` and corrected `farmName` mapping in profile fields.

Improves flexibility in fertilizer computation and enriches profile data handling.

- ✨ feat(build): update Sentry Android SDK to 6.34.0

- Bump Sentry SDK version from 6.19.1 to 6.34.0.
- Comment out unused Sentry Gradle plugin.
- Streamline `build.gradle` and `app/build.gradle` dependencies.

Prepares the project for improved error tracking and stability.

- ♻️ refactor(BaseActivity): fix incorrect calls to currency method

- Updated currency-related properties to correctly use `currencyCode()` instead of `currency()`.

Improves code consistency and resolves potential inaccuracies in currency handling.

- ✨ feat(UserProfile): add `deviceToken` field and update indices

- Introduced `deviceToken` field to `UserProfile` entity.
- Updated unique index to use `device_token` instead of `user_name`.
- Set default value for `mobileCode` to an empty string.
- Removed `language` field.

Enhances user profile structure by adding device-specific identification.
BREAKING CHANGE: Removal of `language` field impacts existing database schema.

- 🐛 fix(MySurveyActivity): resolve null pointer issue with language fallback

- Fixed `language` assignment by using safe null coalescing operator (`?: "en"`)
- Ensures default value is used when `desiredLocale` is null

Improves app stability and prevents potential crashes.

- ✨ feat(CountryFragment): streamline country selection logic

- Refactored country selection flow with a simplified and efficient implementation.
- Replaced redundant variables and logic with concise mappings using `EnumCountry`.
- Improved error handling, user feedback, and dialog interactions.
- Removed unused UI elements and legacy code for better maintainability.

This update enhances usability and simplifies future modifications.

- ✨ feat(BaseActivity): improve intent handling with more concise variable usage

- Replaced `intent` with `it` for cleaner and safer usage in `openActivity` method.

- ♻️ refactor(usecases): replace `let` with null check in `InterCropRecActivity`

- Replaced `let` block with an explicit null check for `profileInfo`.
- Improved code readability and clarity.

- ✨ feat(utils): improve `DateHelper` for better immutability and error handling

- Made `format` immutable by replacing it with `DEFAULT_FORMAT` constant.
- Enhanced `formatToLocalDate` with null and exception handling.
- Added default values for parameters in `unixTimeStampToDate` and `olderThanCurrent`.
- Simplified and clarified date formatting methods with better defaults and comments.

Improves maintainability, reduces potential bugs, and ensures safer date handling.

- ✅ test(DateHelperTest): add test for `formatToLocalDate` method with custom date format

- Added unit test for formatting strings to local date using specified formats
- Ensures proper conversion and validation of date format consistency

- ♻️ refactor(utils): remove redundant `countryName` method in `EnumCountry`

- Eliminated the `countryName` method from all `EnumCountry` entries as it was redundant.
- Retained essential functionality with `countryCode` and other methods.

This simplifies the code and reduces unnecessary overrides for country-specific names.

- ✨ feat(DstRecommendationActivity): display single recommendation text

- Removed the RecyclerView and its adapter as recommendations are now displayed as a single block of text.
- Updated `initializeData` to set the recommendation text and type directly to TextViews.
- Adjusted UI visibility logic to show/hide a single recommendation layout instead of a RecyclerView.
- Improved error handling to display specific error messages from the API response.
- Removed unused variables and imports.

- ✨ feat(DstRecommendationActivity): improve error handling and display

- Implemented more specific error messages for HTTP errors (500, 502, 503) and network issues (UnknownHostException, ConnectException, SocketTimeoutException).
- Unified error display using an `errorContainer` instead of separate `errorLabel` and `errorImage`.
- Ensured error messages are displayed both in a Toast and on the screen.
- Triggered `loadingAndDisplayContent()` on activity creation for immediate data fetching.
- Captured API errors and exceptions with Sentry for better debugging.

- ♻️ refactor(BaseActivity): use `apply` scope function for dialog and layout params configuration

- Simplified dialog initialization using `apply` for `Dialog` and `WindowManager.LayoutParams`.
- Improved readability of `showNotificationDialog` and `showCustomWarningDialog` methods.
- Replaced `if (buttonTitle != null) { if (buttonTitle.isNotEmpty()) }` with `if (!buttonTitle.isNullOrEmpty())` for conciseness.

- 👷 ci(pr-automation): conditionally run auto-approve and add review message

- Added `if: github.actor != 'masgeek'` to the job to prevent auto-approval of PRs created by 'masgeek'.
- Added `review-message: "Auto approved automated PR"` to the `hmarr/auto-approve-action` step.

- ⬆️ ci: update next-release workflow

- Add `ci/fix-auto-approve` branch to trigger the workflow
- Change the reviewer for the pull request to "munywele-bot"

- Fix: remove `ci/fix-auto-approve` branch from next-release workflow triggers

- The `ci/fix-auto-approve` branch is no longer needed for triggering the next-release workflow.

- 🤖 ci(next-release): use commit message for pull request body

- Updated the `next-release.yml` workflow to use the latest commit message as the body and new string for automatic pull requests.
- Changed `old_string` to a more generic placeholder.

This ensures that the pull request description reflects the changes made in the triggering commit.

- Merge pull request #352 from IITA-AKILIMO/ci/fix-auto-approve

ci/fix auto approve

- 🤖 ci(android.yml): update build workflow and artifact handling

- Modified trigger condition for `build_artifacts` job to include `version-fix` branch.
- Added `VERSION_CODE` environment variable, retrieving its value from a remote PHP script.
- Changed artifact retrieval step name from "Retreive artifacts" to "Retrieve artifacts".
- Added a step to update the build version by calling a remote PHP script after successful internal app sharing.

- 🤖 ci(android.yml): fix typo in update build version URL

- Corrected a typo in the URL used to update the build version, changing `pudate` to `update`.

- 🤖 ci(android.yml): update version code retrieval and build version update steps

- Added `VERSION_CODE: ${{ secrets.GITHUB_RUN_ID }}` to the `env` section for the `build` job.
- Replaced the "Get version code from remote PHP script" step with "Get build version" in the `build_artifacts` job, which now uses the same logic as the `build` job to set the `VERSION_CODE` environment variable.
- Moved the "Update build version" step from the `build_artifacts` job to the end of the `upload_beta_artifacts` job. This step now runs after uploading release artifacts for beta branches.

- 🤖 ci(android.yml): add step to update build version

- A new step "Update build version" has been added to the `run_tests` job.
- This step executes a `curl` command to a remote PHP script, presumably to update the build version after tests are run.

- 🤖 ci(android.yml): remove "Update build version" step

- Deleted the step that updated the build version by calling an external script. This step was previously executed after the test report generation in the `run_tests` job.

- 🤖 ci(android.yml): update build_artifacts trigger

- Modified the trigger condition for the `build_artifacts` job.
- Replaced the `version-fix` branch with the `beta` branch in the trigger condition.
- The job will now run if the GitHub reference contains `beta` or `main`.

- 🤖 ci(next-release.yml): update workflow for next release

- Updated `runs-on` from `ubuntu-latest` to `ubuntu-24.04`.
- Updated `actions/checkout` from `v3` to `v4`.
- Set pull request `reviewer` to `github.actor`.
- Changed pull request `body` and `new_string` to use `github.event.head_commit.message`.

- 🤖 ci(merge-to-develop): update `action-pull-request` to v0.6.0

- The `action-pull-request` GitHub Action used in the "Create pull request to develop" step has been updated from version `v0.5.5` to `v0.6.0`.
- The `github_token` input remains configured to use `secrets.GITHUB_TOKEN`.

- Merge pull request #355 from IITA-AKILIMO/ci/version-fix

Merge ci/version-fix to develop

- 🤖 ci: remove actor check for auto-approve action

- The condition `if: github.actor != 'masgeek'` has been removed from the `Auto approve PR` step.
- This allows the auto-approve action to run regardless of the actor who initiated the pull request.

- Merge pull request #357 from IITA-AKILIMO/ci/version-fix

Merge ci/version-fix to develop

- Refactor: move companion object to top of `InfoFragment`

The companion object within the `InfoFragment` class has been moved from the bottom of the class definition to the top. This change improves code organization by placing static factory methods and constants at a more conventional location.

- Refactor: update app language handling in `BaseActivity`

- Removed `ViewPumpContextWrapper` and `dev.b3nedikt.app_locale` dependencies for language management.
- Integrated `LanguageManager` to set the application locale in `attachBaseContext`.
- The application language is now retrieved using `LanguageManager.getLanguage` and set using `LanguageManager.setLocale`.

- Refactor(fragment_bio_data.xml): update UI with Material Design components

- Replaced `TextView` and `TextInputEditText` with `TextInputLayout` and `TextInputEditText` for Material Design text fields.
- Grouped related fields into `MaterialCardView` for better visual organization.
- Adjusted layout parameters, margins, and padding for improved spacing and alignment.
- Updated spinner styles and added minimum dimensions for better usability.
- Removed "Remember my details" checkbox.
- Set `android:fillViewport="true"` for `NestedScrollView`.
- Changed `tools:context` to use a relative path.
- Updated title `TextView` style to `TextAppearance.MaterialComponents.Headline6`.

- Deps: update and remove dependencies

- Removed ORMLite dependencies (`ormlite-android` and `ormlite-core`).
- Removed Volley dependency (`com.android.volley`).
- Removed AppLocale, Reword, and ViewPump dependencies.
- Updated `androidx.test:runner` from version `1.6.1` to `1.6.2`.
- Cleaned up comments in the dependencies section.

- Refactor: remove unused methods from `Tools.kt`

- Removed `mapper` property and associated Jackson ObjectMapper initialization.
- Removed `parseNetworkError`, `prepareJsonObject` (both overloads), `serializeObjectToJson`, `iterateJsonObjects`, and `generateUUID` methods. These methods were not being used in the codebase.

- 🤖 ci(pr-automation.yml): prevent auto-approval for specific user

- Added a condition to the workflow to prevent auto-approval of pull requests if the actor is 'masgeek'.

- Refactor: update error handling and deprecate methods in DstRecommendationActivity

- Deprecated `initComponent()` and `initToolbar()` methods.
- Updated error message display to use `lblErrorMessage` and `lblErrorDetail` TextViews.
- Improved error messages for HTTP errors and other exceptions, providing more specific details.
- Ensured error details are logged to Sentry.

- Refactor: update card corner radius and padding in harvest and planting layouts

- Changed `cardCornerRadius` from `@dimen/dimen_12` to `@dimen/dimen_5`.
- Changed `padding` from `@dimen/spacing_medium` to `@dimen/spacing_middle`.

- Refactor: remove explicit `fontFamily` from  layouts

- 🤖 ci(android.yml): integrate SonarQube analysis

- Added caching steps for SonarQube and Gradle packages to speed up builds.
- Added a "Build and analyze" step that runs `./gradlew build sonar --info` using `SONAR_TOKEN` and `SONAR_HOST_URL` secrets.

- 🛠️ build: refactor Gradle configuration and update dependencies

- Moved plugin declarations to the `plugins` block for a more modern Gradle setup.
- Downgraded `kotlin_version` from `2.0.0` to `1.9.0` in the `buildscript` block.
- Updated Jacoco version from `0.8.10` to `0.8.11`.
- Added SonarQube and Detekt plugins to `buildscript` dependencies and the `plugins` block.
- Updated Kotlin Android plugin version to `2.0.21` in the `plugins` block.
- Updated Android Library plugin version to `8.5.2` in the `plugins` block.
- Added KSP plugin version `2.0.0-1.0.23` in the `plugins` block.
- Removed the `allprojects` block and its repository configurations.
- Commented out some existing dependency declarations in the `buildscript` block.

- ⚙️ **refactor(settings.gradle): centralize repository configuration**

- Moved repository declarations from `build.gradle` to `settings.gradle` using `dependencyResolutionManagement`.
- Added `pluginManagement` block to specify repositories for Gradle plugins.
- Included `google()`, `mavenCentral()`, `jitpack.io`, and a Sonatype snapshots repository.
- Added Mapbox Maven repository with authentication using `MAPBOX_DOWNLOADS_TOKEN` from `gradle.properties`.
- Removed `jcenter()` as it's deprecated.
- Set `repositoriesMode` to `FAIL_ON_PROJECT_REPOS` for stricter dependency resolution.
- Kept `include ':app'` and `rootProject.name='akilimo-mobile'` as is.

- 📝 **build(sonar): add sonar properties for code analysis**

- Added properties for SonarQube to specify source directories, encoding, test inclusions, and exclusions.
- This ensures accurate code analysis by including relevant files and excluding generated or irrelevant ones.

- 🤖 ci(android.yml): update test reporting action

- Replaced `asadmansr/android-test-report-action@v1.2.0` with `FlickerSoul/android-test-report-actions@v1.2`.
- Configured the new action to show skipped tests and include the operating system in the report header.

- 🤖 ci(android.yml): update concurrency group and add caching

- Changed the concurrency group from `ci-${{ github.ref }}` to `ci-${{ github.head_ref || github.ref_name }}` to better handle pull request and branch builds.
- Added a step to cache SonarQube packages to speed up analysis.
- Added a step to cache Gradle dependencies to improve build times.

- Ci: remove unused tag-runner.yml workflow

The `tag-runner.yml` GitHub Actions workflow has been removed as it appears to be unused.

- Refactor: remove unused imports

The following unused imports have been removed:
- `java.util.Locale` from `BaseActivity.kt` and `BaseStepFragment.kt`
- `com.akilimo.mobile.utils.Locales` from `Akilimo.kt`
- `android.text.TextUtils`, `android.util.Log`, and `android.util.Patterns` from `ValidationHelper.kt`
- All `androidx.room` sub-packages where specific classes are now imported directly (e.g., `Dao`, `Insert`, `Query`) in various DAO files.
- `java.util.*` replaced with specific import `java.util.Calendar` in `IDatePickerDismissListener.kt` and `Converters.kt`.

- 🐞 **fix(build): correct lint report path for SonarQube and enable XML output**

- Updated `sonar.androidLint.reportPaths` to point to `build/reports/lint-results.xml`.
- Configured `lintOptions` to enable XML report generation and specify the output file as `build/reports/lint-results.xml`.
- Set `htmlReport` to `true` within `lintOptions`.

- Clean: remove unused Mapbox token variable in `computeVersionCode`

The `mapbox` variable, initialized with `MAPBOX_DOWNLOADS_TOKEN`, was declared but not used within the `computeVersionCode` function in `app/build.gradle`. This change removes the unused variable and its associated print statement.

- 🔨 **build: update dependencies and lint configurations**

- Added `lint` block to configure linting behavior:
    - `ignoreWarnings = true`
    - `abortOnError = false`
    - `checkReleaseBuilds = false`
    - Disabled `MissingTranslation` check.
- Updated `androidx.activity:activity` dependency to `androidx.activity:activity-ktx:1.8.0`.

- 🤖 ci(android.yml): disable Android test report action

- The `FlickerSoul/android-test-report-actions` step, previously used for generating Android test reports, has been commented out.

- 🤖 ci(android.yml): ignore non-code changes for build trigger

- Updated the `push` trigger in the Android builder workflow.
- Added `paths-ignore` to exclude changes in documentation files (`.md`, `.txt`), configuration files (`.yml`, `.yaml`), scripts (`.sh`), the `docs/` directory, and issue templates from triggering the workflow.

- 🤖 ci(android.yml): expand path trigger for `build_artifacts` job

- Added `'.github/**'` to the `paths-ignore` list for the `build_artifacts` job.
This ensures that changes to GitHub-specific files (e.g., workflow configurations, issue templates) do not unnecessarily trigger the Android build workflow.

- 🔄 ci: refine `merge-to-develop` workflow trigger

- Added `paths-ignore` to exclude changes in documentation, configuration, and script files from triggering the workflow.
- Kept `branches-ignore` for the `main` branch.

- 🤖 ci(android.yml): optimize SonarCloud analysis

- Removed `build` task from the SonarCloud analysis step.
- The workflow now only runs `./gradlew sonar` for analysis.

- Merge pull request #360 from IITA-AKILIMO/ci/sonarqube

Merge ci/sonarqube to develop

- 🤖 ci: enable manual trigger and fetch full history for PR workflows

- Added `workflow_dispatch` trigger to `merge-to-develop.yml`, allowing manual execution.
- Configured `actions/checkout@v4` in both `merge-to-develop.yml` and `next-release.yml` to fetch the full Git history (`fetch-depth: 0`). This is often necessary for actions that analyze commit history or create pull requests.

- 🤖 ci: refine `merge-to-develop` workflow trigger

- Removed `.github/**` from paths-ignore to ensure workflow runs on changes within the `.github` directory itself, except for explicitly ignored paths like `.github/ISSUE_TEMPLATE/**`.

- 💄 **refactor(planting_harvest_date.xml): update UI to Material Design Components**

- Replaced `ImageView` with `ShapeableImageView` for the planting image, applying Material shape appearance.
- Replaced `AppCompatTextView` with `MaterialTextView` for title, subtitle, date hints, and selected dates, updating text appearances.
- Replaced `AppCompatButton` with `MaterialButton` for date picking buttons, applying Material button styles and updated corner radius.
- Adjusted layout constraints and margins for better alignment and spacing.
- Changed the planting image resource from `ic_calendar` to `ic_schedule`.
- Updated text color for selected dates to use `?attr/colorSecondary`.

- 🤖 ci(android.yml): include XML files in workflow trigger paths

- Added `**.xml` to the `paths-ignore` list for workflow triggers. This ensures that changes to XML files will also trigger the workflow.

- Refactor(AreaUnitFragment): update UI and remove unused code

- Updated radio button IDs and layout IDs for clarity.
- Replaced `ImageView` with `ShapeableImageView` and `TextView` with `MaterialTextView` for consistency with Material Design components.
- Adjusted image size for better visual appearance.
- Removed the "Remember details" checkbox and its associated logic as it was not being used.
- Removed unused `binding.chkRememberDetails` listener.

- Refactor(FieldSizeFragment): Migrate to ConstraintLayout and improve logic

- Replaced `RelativeLayout` with `androidx.constraintlayout.widget.ConstraintLayout` for the root layout in `fragment_field_size.xml`.
- Updated view IDs and types to Material Design components.
- Simplified variable declarations and initialization in `FieldSizeFragment.kt`.
- Improved data refreshing logic by directly accessing database entities.
- Streamlined radio button selection handling.
- Enhanced field label setting based on area unit.
- Refined custom dialog display and input handling for specifying exact area.
- Updated data saving logic to ensure `MandatoryInfo` exists before updating.
- Adjusted step verification to consider `dataValid` flag.

- ✨ Refactor Investment Preference Fragment

- Renamed layout and component IDs for clarity (e.g., `lyt_parent` to `layout_investment_preferences`, `image` to `img_risk_attitude`).
- Updated variable names in the Kotlin code for better readability (e.g., `investmentPreference` to `riskAttitudes`).
- Removed the "Remember details" checkbox and its associated logic.
- Standardized UI components, replacing `AppCompatTextView` with `MaterialTextView` and `ImageView` with `ShapeableImageView`.
- Adjusted layout constraints and margins for improved visual presentation.

- Refactor: rename IDs in `fragment_planting_harvest_date.xml`

Updated the IDs of various UI elements within the `fragment_planting_harvest_date.xml` layout file for better clarity and consistency.

- `lyt_parent` to `planting_scroll_container`
- `plantingImage` to `planting_image`
- `title` to `planting_title`
- `subtitle` to `planting_subtitle`
- `btnPickPlantingDate` to `planting_btn_pick_date`
- `plantingDateHint` to `planting_date_hint`
- `lblSelectedPlantingDate` to `planting_date_label`
- `btnPickHarvestDate` to `harvest_btn_pick_date`
- `harvestDateHint` to `harvest_date_hint`
- `lblSelectedHarvestDate` to `harvest_date_label`

Removed `app:cornerRadius="12dp"` from `planting_btn_pick_date` and `harvest_btn_pick_date` as it is now defined in the style.
Adjusted `marginTop` for `planting_subtitle` from `spacing_medium` to `spacing_large`.

- Chore(workflows): standardize job names

- Renamed job `run_tests` to `unit-tests` in `android.yml`.
- Renamed job `build` to `check-todo` in `todo-checker.yml`.
- Renamed job `create-pr` to `pr-to-develop` in `merge-to-develop.yml`.
- Renamed job `auto-approve` to `pr-auto-approve` in `pr-automation.yml`.
- Renamed job `action-pull-request` to `next-release-pr` in `next-release.yml`.

- 🤖 ci(android.yml): rename `unit-tests` job to `unit-test`

The CI workflow job responsible for running unit tests has been renamed from `unit-tests` to `unit-test`.

- 🤖 ci(android.yml): update `build_artifacts` job dependency

- Changed the `needs` condition for the `build_artifacts` job from `run_tests` to `unit-test`.

- 🤖 ci(android.yml): update unit test reporting and SonarQube integration

- Added steps to cache Gradle packages for unit tests.
- Incorporated `FlickerSoul/android-test-report-actions` and `asadmansr/android-test-report-action` for generating test reports after unit tests, ensuring they run even if tests fail.
- Created a new job `sonar-checks` that runs on the `develop` branch after `unit-test` job.
- The `sonar-checks` job now handles checking out code, setting up JDK, and making `gradlew` executable.
- Renamed the SonarQube analysis step from "Build and analyze" to "Run Sonar Analysis".
- Removed commented-out test reporting steps from the previous SonarQube analysis section as they are now handled in the `unit-test` job.

- 🤖 ci(android.yml): remove "Sonar Checks" name from workflow

The "Sonar Checks" job name has been removed from the `android.yml` GitHub Actions workflow. The job will now display its default name based on its ID (`sonar-checks`).

- Merge pull request #366 from IITA-AKILIMO/ci/rename

Merge ci/rename to develop

- 🤖 ci(android.yml): remove SonarQube analysis job

- Deleted the `sonar-checks` job and its associated steps from the GitHub Actions workflow.
- This job was previously responsible for performing SonarQube analysis on the `develop` branch.

- ➕ **ci(sonar-checks): add new workflow for SonarQube analysis**

- This workflow runs SonarQube analysis on pushes to the `develop` branch.
- It includes steps for checking out code, setting up JDK 21, caching SonarQube and Gradle packages, and running the Sonar analysis using Gradle.
- The workflow ignores changes to markdown, text, XML files, documentation, issue templates, and GitHub workflow files.
- It can also be triggered manually via `workflow_dispatch`.

- Merge pull request #367 from IITA-AKILIMO/ci/sonar

Merge ci/sonar to develop

- 🤖 ci(bump-and-tag.yml): refine workflow trigger conditions

- Added `paths-ignore` to prevent workflow execution for changes in documentation, configuration files, and GitHub-specific files.
- The workflow will now only trigger for relevant code changes on the `main` branch.


## [25.0.1] - 2025-02-19

## [25.0.0] - 2024-07-22

## [24.2.2] - 2024-06-22

## [24.2.1] - 2023-11-27

### Documentation

- Docs: updated documentatoin for releases 

Co-authored-by: Sammy Barasa <barsamms@gmail.com>


## [24.2.0] - 2023-09-14

## [24.1.0] - 2023-09-11

## [24.0.1] - 2023-08-22

## [24.0.0] - 2023-08-21

## [23.0.2] - 2023-08-21

### Bug Fixes

- Fix: added null check for country code

- Fix: update query filter

updated query filter for fertilizers to pick selected fertilizers only

- Fix: update IFertilizerDismissListener to allow nullable Fertilizer

- Fix: use currencyCode instead of currency to get currency symbol

- Fix: calculate total investment amount based on different area units


### Code Refactoring

- Refactor: removed accidental comment

- Refactor: removed eprecatd configuration in gradle build script

- Refactor: added replacment vector icon

- Refactor: revised result code for cancelled updates

- Refactor: moved inAppUpdate class to utils package

- Refactor: revised updateflow to use non deprecated method

- Refactor: reduced code smell for null checks

- Refactor: code cleanup

- Refactor: switched to TextUtils

- Refactor: removed dead code

- Refactor: deleted deprecated fieldinfo fragment class

- Refactor: optimized fragment view class

- Refactor: rewrote code to be more readable

- Refactor: added deprecated command

added compileSdkVersion instead os sdkVersion

- Refactor: disabled explicit jvm kotlin toolchain

- Refactor: rename FuelrodApiInterface to FuelrodService and update its implementation

- Refactor: update dialog fragment instantiation

The instantiation of `MaizePriceDialogFragment` and `OperationTypeDialogFragment` was updated to use the default constructor instead of passing a context. This change aligns with best practices for `DialogFragment` management by the `FragmentManager`, ensuring proper lifecycle integration and preventing potential issues like `IllegalStateExceptions` and window leaks as recommended by Android best practices.

- Refactor: Remove redundant `dialog` variable in `FertilizerPriceDialogFragment`

The `dialog` variable in `FertilizerPriceDialogFragment` was redundant. This commit removes the unused class variable and uses a local `dialog` variable instead. It also updates the `radioSelected` method to use the local `dialog` variable passed in.

- Refactor: update dialog fragments to use view binding and remove redundant code

This commit updates various dialog fragments to properly use view binding by replacing direct `findViewById` calls. It also removes redundant code such as unnecessary dialog object variables, making the code cleaner and more maintainable.
Here is a breakdown of the changes:

-   **FragmentCassavaPriceDialog**:
    -   Replaced the nullable binding `binding` with a non-nullable binding `_binding` to avoid potential null pointer exceptions.
    -   Updated dialog initialization to use the new `_binding`.
    -   Simplified the `radioSelected` function to directly use `dialog.findViewById` instead of relying on `binding.root`.

-   **FragmentOperationTypeDialog**:
    -   Updated the dialog initialization code to be more concise.
    -   Moved the `setContentView` call to be inside the dialog configuration block.
    -   Updated window management configuration to make it more concise.

-   **FragmentOperationCostDialog**:
    -   Replaced `dialog` object with a local one in `onCreateDialog`
    -   Replaced redundant dialog variables with `_binding` and added getter for it
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.
    -   Simplified the `radioSelected` function.

-   **FragmentSweetPotatoPriceDialog**:
    -   Replaced `dialog` object with a local one in `onCreateDialog`
    -   Replaced redundant dialog variables with `_binding` and added getter for it
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.
    -   Updated `radioSelected` function to use the dialog for `findViewById`

-   **FragmentRootYieldDialog**:
    -   Replaced redundant dialog object variable with a local one
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.

-   **FragmentMaizePerformanceDialog**:
    -   Replaced redundant dialog object variable with a local one
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.

-   **FragmentMaizePriceDialog**:
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.

-   **FragmentFertilizerPriceDialog**:
    -   Replaced redundant dialog object variable with a local one
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.

-   **FragmentSingleSelectDialog**:
    -   Replaced redundant dialog object variable with a local one
    -   Updated dialog configuration to be more consistent.
    -   Moved the `setContentView` call to be inside the dialog configuration block.

- Removed unnecessary imports
- Corrected window management configuration
- Improved code readability and conciseness

- Refactor: initialize sessionManager, mathHelper, and database in BaseDialogFragment

- Refactor: replace deprecated `kotlinx.android.parcel.Parcelize` with `kotlinx.parcelize.Parcelize`

- Refactor: use binding delegate pattern in FieldSizeFragment

The `FieldSizeFragment` was refactored to utilize the binding delegate pattern for improved code readability and to prevent potential memory leaks. Instead of directly accessing the binding instance via the `binding` variable, it now uses a backing property (`_binding`) and a delegate (`binding get()`) to manage the binding lifecycle. Additionally `refreshData` function is updated to private since it is only used inside the class. This ensures the binding is only accessed when the view is valid, reducing the risk of null pointer exceptions. The code now adheres to modern Kotlin practices for handling view bindings.

- Refactor: remove temporary redirection to RecommendationsActivity in SplashActivity

- Refactor: convert TheItemAnimation from Java to Kotlin

This commit refactors the `TheItemAnimation` class by converting it from Java to Kotlin. The functionality of the class remains the same, but the code has been updated to leverage Kotlin's syntax and features.

- Refactor: update RecommendationsActivity declaration in AndroidManifest

The declaration of `RecommendationsActivity` has been updated in the `AndroidManifest.xml` file, removing the `usecases` subdirectory from the class name.

- Refactor: update `tools:context` in `activity_recommendations_activity.xml` to reflect package change

- Refactor: Rename LocationInfo entity to UserLocation

This commit renames the `LocationInfo` entity to `UserLocation` and updates all references in the codebase to reflect this change. The change includes:

- Refactor: rename LocationInfoDao to UserLocationDao and update related classes

This commit renames `LocationInfoDao` to `UserLocationDao` and updates all references to reflect this change. It also renames the table to `user_location` and updates the `UserLocation` class to be a data class.


### Documentation

- Docs: updated documentatoin for releases

- Docs: added TODO comment


### Features

- Feat: updated gradle and kotlin version

updated gradle and kotlin versions to 7.xx and 1.8.xx respectively

BREAKING CHANGE: Updated gralde and kotlin versions

- Feat: added google updated checker

updated checker will allow for easier update notifications

- Feat: added country information text on selection

- Feat: updated translations

updated translation of some string values

- Feat: reducd code complexity for the switch case

- Feat: gradle update

BREAKING CHANGE: gradle and sdk update

- Feat: migrate RecommendationsActivity to Kotlin and use Retrofit for currency updates

This commit migrates the `RecommendationsActivity` from Java to Kotlin. The migration includes updating the code to utilize Kotlin's features and syntax, and it replaces the Volley library with Retrofit for handling currency list updates. Retrofit is now used to fetch and process the list of currencies.

- Feat: add AkilimoCurrency entity with Room integration

- Feat: remove currency entity

- Feat: update Currency entity to AkilimoCurrency and update queries

- Feat: update libraries for networking, logging, navigation and activities

- Feat: update Room database version to 2

This commit updates the Room database version from 1 to 2, reflecting changes in the database schema.

- Feat: add retrofit API service for currency, fertilizer and fertilizer price endpoints

- Feat: update AndroidManifest.xml

- Updated permissions: Moved `ACCESS_NETWORK_STATE` permission declaration.
- Updated application attributes: Removed `dataExtractionRules` and `networkSecurityConfig`.
- Updated meta-data: Formatted `meta-data` elements for better readability.
- Updated uses-library: Commented out `uses-library` for `org.apache.http.legacy`.

- Feat: migrate FertilizersActivity from Java to Kotlin and use Retrofit for API calls

- Feat: add RetrofitManager and RetroFitFactory for API communication

This commit introduces `RetrofitManager` and `RetroFitFactory` to manage Retrofit instances for API communication. `RetroFitFactory` creates Retrofit instances with OkHttp client and Jackson converter, while `RetrofitManager` initializes and provides access to specific Retrofit instances.

- Feat: upgrade database version to 3 and update database name

The database version has been upgraded to 3. The database name has also been updated to "AKILIMO_22_APR_2025".

- Feat: update table name and column in CassavaPriceDao

The table name has been changed from `cassava_price` to `cassava_prices`. The column name has been changed from `country` to `country_code`.

- Feat: update cassava price entity with new fields and data types

The cassava price entity has been updated to include new fields such as `id`, `countryCode`, `minLocalPrice`, `maxLocalPrice`, `minUsd`, `maxUsd`, `minAllowedPrice`, `maxAllowedPrice`, `active`, and `averagePrice`. The data types of some existing fields have also been changed.

- Feat: update fertilizer entity to match API response

The `Fertilizer` entity has been updated to align with the structure of the API response, including changes to data types and field names. New fields have been added to accommodate additional fertilizer information, and the table name has been changed from `fertilizer` to `fertilizers`.

- Feat: remove fertilizer type from list

- Feat: update FertilizerPrice entity and add FertilizerPriceResponse data class

The commit updates the `FertilizerPrice` entity to include more fields and use appropriate data types. It also introduces a new data class, `FertilizerPriceResponse`, to handle the response containing a list of `FertilizerPrice` objects.

- Feat: update FertilizerPrice entity and add FertilizerPriceResponse data class

The commit updates the `FertilizerPrice` entity to include more fields and use appropriate data types. It also introduces a new data class, `FertilizerPriceResponse`, to handle the response containing a list of `FertilizerPrice` objects.

- Feat: update cassava price query to use country code

- Feat: update fertilizer table name and column names in FertilizerDao

The commit updates the table name from "fertilizer" to "fertilizers" in the FertilizerDao class. It also updates the column names to use snake_case for consistency.

- Feat: update session manager to use fuelrod endpoint

- Feat: initialize Retrofit in SplashActivity and add ApiTestActivity

This commit initializes Retrofit with the endpoint URLs stored in SessionManager within SplashActivity. Additionally, it adds an ApiTestActivity, which is currently commented out but can be used for testing API calls during development. The default intent is set to RecommendationsActivity.

- Feat: deprecate RestService and update endpoint retrieval

The RestService class has been deprecated. The method for retrieving the API endpoint has been updated to use `getAkilimoEndpoint()` instead of `getApiEndPoint()`.

- Feat: use AkilimoCurrency entity instead of Currency entity in TractorAccessActivity and WeedControlCostsActivity

- Feat: allow cleartext traffic for specific domains

This change updates the network security configuration to allow cleartext traffic for the following domains and their subdomains:

- munywele.co.ke
- akilimo.org
- tsobu.co.ke
- stag-emerging-dodo.ngrok-free.app

- Feat: use AkilimoCurrency entity instead of Currency

This commit replaces the usage of the `Currency` entity with `AkilimoCurrency` in the `MaizeMarketActivity` and `ManualTillageCostActivity`. This change ensures that the correct currency information is used throughout the application.

- Feat: update recommendations activity to use retrofit

- Feat: load api endpoint from remote config

- Feat: add investment amounts API endpoint

- Feat: remove investmentAmountDto database table

- Feat: update profile information in the database

The commit updates the profile information in the database by setting the username based on the profile names. It retrieves the device token from the session manager and updates the profile information using the `update` method of the `profileInfoDao`.

- Feat: update compute request to include user full names and max investment

The compute request now includes the user's full names, retrieved from the user profile information. The maximum investment amount is also retrieved from the database and included in the compute request.

- Feat: migrate InvestmentAmountActivity from Java to Kotlin

This commit migrates the InvestmentAmountActivity from Java to Kotlin, including necessary refactoring and adjustments for Kotlin syntax.

- Feat: remove unused InvestmentAmountDto files

- Feat: update LocationFragment to use names() method from profileInfo

- Feat: add names method to ProfileInfo to return formatted full name

- Feat: use String.format for summary title in SummaryFragment

- Feat: remove investmentAmountDtoDao().deleteAll() from SplashActivity

The removed code snippet `investmentAmountDtoDao().deleteAll()` was likely used to clear data related to investment amounts. However, the specific purpose and impact of its removal are unclear without further context.

- Feat: update investment amount entity

The commit updates the InvestmentAmount entity to include additional fields such as country code, minimum and maximum investment amounts, area unit, field size, price activity status, sorting order, and timestamps for creation and updates. It also introduces a new data class, InvestmentAmountResponse, to handle responses containing a list of InvestmentAmount entities.

- Feat: add methods to InvestmentAmountDao for finding by ID and batch insertion

The changes introduce two new methods to the `InvestmentAmountDao` in Kotlin: `findOneByInvestmentId` to retrieve an investment amount based on its ID, and `insertAll` to insert a list of investment amounts into the database.

- Feat: remove volley request classes

This commit removes the `CustomVolleyRequest` and `RestService` classes, which were used for making network requests with the Volley library. These classes are no longer needed as the application has transitioned to using Retrofit for network operations.

- Feat: implement sweet potato market price screen logic

- Feat: Implement user review submission via API

This commit implements the functionality to submit user reviews via an API call. It replaces the previous Volley implementation with a Retrofit-based approach for handling network requests.  The changes include:

- Removal of Volley dependency and integration of Retrofit for API calls.
- Modification of the `submitUserReview` function to use Retrofit to send survey data to the backend.
- Implementation of success and failure callbacks to handle API responses.
- Display of a toast message upon successful feedback submission.
- Error handling and reporting to Sentry in case of API call failures.

- Feat: change survey request properties to snake case

- Feat: remove unused rest service class

- Feat: improve intercrop fertilizer recommendation logic

- Feat: handle API request errors for fertilizer prices

- Feat: show loading progress when requesting recommendations

- Feat: allow for empty cost items in operation costs

- Feat: add API endpoint to submit user reviews

- Feat: improve MySurveyActivity layout

Updated the layout for `activity_my_survey.xml` to enhance user experience. Changes include:
- Centered the layout content
- Added `textColor` attribute to the `TextView` elements for better readability
- Adjusted padding and margins for better spacing
- Modified `RadioGroup` elements for a more consistent look and feel.

- Feat: update cassava market activity to handle market survey data

- Feat: add entities, API endpoints, and database migrations for price data

This commit introduces new entities for maize, potato, and cassava prices, along with corresponding API endpoints to fetch this data. It also includes database schema updates and migrations to accommodate the new price entities.

- Feat: convert CassavaPriceDialogFragment from Java to Kotlin

This commit migrates the `CassavaPriceDialogFragment` from Java to Kotlin, addressing nullability issues and leveraging Kotlin's concise syntax.

- Feat: migrate IntercropFertilizersActivity from Java to Kotlin and use Retrofit for API calls

- Feat(refactor):major refactoring   BREAKING_CHANGES: ui rewrites

- Feat: migrate MaizePerformanceDialogFragment from Java to Kotlin

This commit migrates the `MaizePerformanceDialogFragment` from Java to Kotlin. The functionality remains the same, but the code is now written in Kotlin, offering improved conciseness and null safety.

- Feat: refactor operation costs dialog fragment for better handling and reusability

This commit refactors the `OperationCostsDialogFragment` to improve its handling and reusability. The changes include:

- Removing the context parameter from the constructor.
- Adding a companion object to hold constant values.
- Updating the `onCreateDialog` method to use `requireContext()` and handle arguments.
- Improving radio button creation within the dialog.
- Adjusting how the dialog is called from `TractorAccessActivity`, `ManualTillageCostActivity` and `IntercropFertilizersActivity`.
- Ensuring the dialog instance is created via `getOperationCostsDialogFragment` in `TractorAccessActivity`.
- Adding null safety checks for mathHelper in `OperationCostsDialogFragment`.
- Using `toDrawable()` to set the background color in `OperationCostsDialogFragment`.

These changes streamline the dialog's lifecycle and make it more adaptable to different contexts.

- Feat: remove context from base dialog fragment

- Feat: refactor cassava price dialog fragment and update arguments

- Feat: remove deprecated IntercropFertilizerPriceDialogFragment and update CassavaPriceDialogFragment initialization

- Removes the deprecated `IntercropFertilizerPriceDialogFragment` as it is no longer needed.
- Updates `CassavaPriceDialogFragment` to initialize context in `onCreateDialog` instead of an init block to ensure context is available when the dialog is created.

- Feat: add FertilizerPriceDialogFragment for managing fertilizer prices

This commit introduces a new dialog fragment, `FertilizerPriceDialogFragment`, which allows users to manage fertilizer prices. The dialog displays a list of available price ranges for a selected fertilizer and allows users to either select a predefined price range or enter an exact price.

- Feat: remove unused FertilizerPriceDialogFragment

This commit removes the unused `FertilizerPriceDialogFragment`. This dialog fragment was likely intended for displaying and handling fertilizer price information, but it is no longer used in the application. Removing it helps to clean up the codebase and reduce unnecessary complexity.

- Feat: use empty constructor for FertilizerPriceDialogFragment

- Feat: add Maize performance dialog fragment

- Feat: migrate OperationTypeDialogFragment from Java to Kotlin

This commit migrates the `OperationTypeDialogFragment` from Java to Kotlin. The dialog allows users to select the operation type (mechanical or manual) for ploughing or ridging.

- Feat: enable error reporting to Sentry in FertilizerRecActivity

- Feat: update MaizePerformanceDialogFragment initialization in MaizePerformanceActivity

- Feat: migrate RootYieldDialogFragment from Java to Kotlin

This commit migrates the RootYieldDialogFragment from Java to Kotlin, including updating the layout and associated logic.

- Feat: enable cancelling OperationTypeDialogFragment dialog by tapping outside

- Feat: migrate RootYieldActivity from Java to Kotlin

The RootYieldActivity has been migrated from Java to Kotlin. The migration includes converting the class, its methods, variables, and UI interactions to use Kotlin syntax and features.
This commit removes the `RootYieldActivity.java` file and introduces `RootYieldActivity.kt`.

- Feat: update field yield dialog dismissal listener signature

- Feat: deprecate unused fields in BaseActivity

The commit message summarizes the changes made in the diffs. The diffs show that two fields, `context` and `queue`, in the `BaseActivity` class have been marked as deprecated using the `@Deprecated` annotation. This indicates that these fields are no longer recommended for use and may be removed in future versions of the code.

- Feat: refactor FieldYield entity with Room annotations and Parcelize

This commit refactors the `FieldYield` entity to use Room annotations for better database integration.
- The entity is now a `data class`
- `field_yield` table name changed to `field_yields`
- Added `@ColumnInfo` for all the fields
- `@Parcelize` was replaced by `kotlinx.parcelize.Parcelize`
- Changed fields to use `var` instead of `val` to allow for data modification.
- Remove the `@Transient` annotation as they are not needed anymore.

- Feat: rename table field_yield to field_yields

- Feat: migrate `SingleSelectDialogFragment` from Java to Kotlin

The `SingleSelectDialogFragment` has been migrated from Java to Kotlin. This change involves the removal of the Java file (`SingleSelectDialogFragment.java`) and the creation of a new Kotlin file (`SingleSelectDialogFragment.kt`). The Kotlin version includes refactoring to use data binding.
This migration leverages Kotlin's features to enhance code readability and maintainability.

- Feat: migrate MaizePriceDialogFragment to Kotlin

The `MaizePriceDialogFragment` has been migrated from Java to Kotlin, leveraging Kotlin's features to enhance code readability and maintainability. The functionality of the dialog remains consistent, but the codebase is now written in Kotlin.

- Feat: migrate `DateDialogPickerFragment` from Java to Kotlin

The `DateDialogPickerFragment` has been migrated from Java to Kotlin. The old Java file was deleted and the new Kotlin class was added. The logic remains similar but is now expressed in Kotlin syntax.

- Feat: migrate `MathHelper` class to Kotlin

This commit migrates the `MathHelper` class from Java to Kotlin. The new Kotlin implementation maintains the same core functionality as the original Java version, including currency conversion, area unit conversion, and numerical rounding.

- Feat: migrate `WeedControlCostsActivity` from Java to Kotlin

This commit migrates the `WeedControlCostsActivity` from Java to Kotlin. The migration includes:

-   Replacing the Java class `WeedControlCostsActivity` with its Kotlin equivalent.
-   Updating all related references to use the new Kotlin class.
-   Ensuring all logic and functionality remains consistent with the previous Java implementation.
-   Adapting to Kotlin syntax and features while preserving core functionality.
-   Deleting the old Java `WeedControlCostsActivity`.

- Feat: convert TractorAccessActivity from Java to Kotlin

The `TractorAccessActivity` class has been converted from Java to Kotlin. This involved rewriting the entire class to leverage Kotlin's features. The functionality remains the same. This includes:
- Implementation of the `onCreate`, `initToolbar`, and `initComponent` lifecycle methods.
- Handling of user interactions with radio buttons and checkboxes.
- Data validation and persistence.
- Displaying the fullscreen dialog.
- Handling dialog dismiss events.
- Class and variable type changes.

- Feat: refactor `WeedControlCostsActivity` to improve code readability and maintainability

The changes in this commit focus on refactoring the `WeedControlCostsActivity` for better code quality.

-   Replaced the `binding` with a backing property to improve encapsulation and safety.
-   Removed unused imports.
-   Updated the `setOnCheckedChangeListener` to avoid passing unnecessary variables to the lambda.
-   Fixed the deprecated `onBackPressed` to properly override the method.
-   Replaced `View` parameters with `Unit` as appropriate in the click listener lambdas.
-   Moved the context declaration to be local to `onCreate` and `initComponent` method where it's actually needed.
-   Corrected the use of `isEmptyOrWhitespace` to use `isNullOrEmpty`.
-   Updated the `Toast.makeText` call to use `this@WeedControlCostsActivity`.

- Feat: round the converted field size to 2 decimal places instead of 1

- Feat: replace field size fragment with Kotlin version

The field size fragment has been rewritten in Kotlin, replacing the existing Java version. This change should provide improvements in code maintainability and potential performance enhancements.

- Feat: enhance currency conversions, formatting, and area calculations

This commit introduces significant improvements to the `MathHelper` utility class, enhancing its capabilities in currency conversions, number formatting, and area calculations.

**Key changes:**

-   **Currency Handling:**
    -   Currency conversions now support NGN, TZS, GHS, and USD.
    -   Exchange rates are configurable via a session or by default.
    -   Conversion supports single values and ranges.
    -   Robust error handling during conversion.
-   **Number Formatting:**
    -   Added methods for formatting numbers with or without currency symbols.
    -   Rounding methods enhanced for precision.
-   **Area Calculations:**
    -   Improved area unit conversions (acre, ha, sqm, are).
-   **Investment Calculations:**
    -   Enhanced investment amount computations using local currency, field size, and currency.
-   **Code Modernization:**
    -   Refactored code to improve readability and maintainability.
    -   Consolidated constants for units, currencies, and rounding.
    -   Introduced comprehensive documentation.
-   **Error Handling:**
    -   Improved error handling using `try-catch` blocks.
- **Other Changes:**
   - Added `convertToUnitWeightPrice` function to determine the unit weight price
   - Added `removeLeadingZero` function that will remove leading zeros
   - Added `computeInvestmentForSpecifiedAreaUnit` function for calculating investment based on the specified area
-   - Fixed bug in `convertToUSD` method to handle rounding better
+   - `convertToUSD` method now handles rounding better
+   - Fixed bug in `roundToNDecimalPlaces` to handle `decimalPlaces` value.
+   - Updated to handle empty inputs in `convertToDouble`
+   - Introduced a companion object with constants
   - Added `updateExchangeRates` function
   - Updated the constructors.
   - Updated the names of variables for clarity.
   - Added default rounding values.
   - Introduced the currency exchange rates map.

- Feat: update getFertilizers API to use path parameter for country code

- Feat: improve price selection logic in CassavaPriceDialogFragment

This commit refactors the `CassavaPriceDialogFragment` to improve the logic for selecting cassava prices. The following changes were made:

-   Removed unused variables (`maxPrice`, `minPrice`, `dialog`, `minAmountUSD`)
-   Updated code to use `bundle.apply` for retrieving data from the bundle, which makes code cleaner and shorter.
-   Fixed a potential `NullPointerException` issue when getting the currency symbol.
-   Updated the price radio button generation to use `averagePrice` from `cassavaPriceList` to display accurate price options.
-   Updated `labelText` method to use average price instead of min/max local price, improving accuracy in displayed price labels.
-   Replaced multiple `TODO()` calls in `labelText` with a default case.
-   Added improved error handling for cases where the user inputs invalid prices in edittext.

- Feat: migrate `SessionManager` to Kotlin

The `SessionManager` class has been migrated from Java to Kotlin, improving code readability and maintainability. The new implementation leverages Kotlin's features for conciseness and null safety. The methods and their functionality remain the same.

- Feat: migrate BaseStepFragment to Kotlin

The `BaseStepFragment` class has been migrated from Java to Kotlin, improving code consistency and maintainability. The new `BaseStepFragment` is a Kotlin class.

- Feat: add String extension to handle null or blank strings

This commit introduces a new String extension function `orIfBlank` in `StringExtensions.kt`. This extension provides a concise way to handle strings that might be null or blank. If the string is null or blank, it returns a default value; otherwise, it returns the original string.

- Feat: refactor currency conversion in MathHelper

- Removed the constructor that was loading exchange rates from the session
- Removed the `updateExchangeRates` function
- Made the `convertCurrency` function private
- Updated `convertCurrency` to be more efficient by returning the input as is if it is already in the target currency.

- Feat: migrate area unit fragment to Kotlin

The `AreaUnitFragment` has been migrated from Java to Kotlin. The new Kotlin file `AreaUnitFragment.kt` replaces the previous Java file `AreaUnitFragment.java`, implementing the same functionality with Kotlin syntax and features.

- Feat: refactor BaseStepFragment for improved dependency management and code clarity

This commit refactors the `BaseStepFragment` class to improve dependency management, code clarity, and error handling.

**Key Changes:**

-   **Lazy Initialization:**
    -   Replaced direct initializations with lazy initialization for `database`, `sessionManager`, and `mathHelper`.
-   This approach defers object creation until first use, enhancing performance.
-   **Simplified Dialog:**
    -   Simplified the `showCustomWarningDialog` method to use `Dialog.apply` for cleaner code.
    -   Introduced `setCancelable` and `setCanceledOnTouchOutside` for enhanced control over dialog behavior.
    - Improved and corrected the set of Layout parameters for the dialog.
-   **Location Info Formatting:**
    -   Replaced `loadLocationInfo` with `formatLocationInfo` to handle null cases gracefully.
    -   Improved formatting logic for location data.
-   **Database Operations:**
    -   Introduced `performDatabaseOperation` to execute database tasks in a safe coroutine context.
-   **Locale Handling:**
    -   Added `getCurrentLocale` helper function for retrieving the current locale.
-   **Error Handling:**
    -   Improved error handling in the warning dialog and database operation functions.
    - Added a basic implementation for the `onError` function.
-   **Code Cleanup:**
    -   Removed unused member variables and imports.
    -   Added more detailed comments for better readability.
    - Improved the constructor and simplified the class.
-   **Logging:**
    -   Added comprehensive logging with Timber for better debugging.
    -   Removed the logging tag, since timber takes care of that.
- Removed the Volley dependencies and request queue.
- Removed the deprecated field.
- Remove the use of a deprecated method.

These changes enhance the fragment's robustness and ease of use.


### Miscellaneous Tasks

- Ci: revised revision tools and github actions sequence

- Ci: added android sdk setup step

- Ci: testing android build

- Ci: disabled android setup step

- Ci: downgraded build tools

- Ci: added pull request automation

- Ci: added next release github action

this will automatically create a release pull request

- Merge pull request #316 from IITA-AKILIMO/ci/next-release

ci: added next release github action

- Build(deps): bump ejs from 3.1.9 to 3.1.10 

Bumps [ejs](https://github.com/mde/ejs) from 3.1.9 to 3.1.10.
- [Release notes](https://github.com/mde/ejs/releases)
- [Commits](https://github.com/mde/ejs/compare/v3.1.9...v3.1.10)

---
updated-dependencies:
- dependency-name: ejs
  dependency-type: indirect
...

Signed-off-by: dependabot[bot] <support@github.com>
Co-authored-by: dependabot[bot] <49699333+dependabot[bot]@users.noreply.github.com>

- Ci: added tag fetcher

- Ci: changed workflow name

- Ci: changed workflow name

- Ci: updated android ci/cd workflow filw

- Merge pull request #328 from IITA-AKILIMO/ci/tag-fetching

ci/tag fetching

- Ci: updated actions file

- Ci: revised build number in CI

- Build(deps): bump actions/download-artifact in /.github/workflows

Bumps [actions/download-artifact](https://github.com/actions/download-artifact) from 2 to 4.1.7.
- [Release notes](https://github.com/actions/download-artifact/releases)
- [Commits](https://github.com/actions/download-artifact/compare/v2...v4.1.7)

---
updated-dependencies:
- dependency-name: actions/download-artifact
  dependency-type: direct:production
...

Signed-off-by: dependabot[bot] <support@github.com>

- Chore: remove deprecated pull request workflow

- Chore: add VSCode settings for interactive Java build configuration

- Chore: disable interactive build configuration in VSCode settings

- Chore: remove experimental android extensions configuration

- Chore: update dependencies and repository configurations in build.gradle

- Chore: update base URL in FuelrodApiInterface to new endpoint

- Chore: update JSON property names in RemoteConfig to use snake_case

- Chore: rename BASE_URL parameter to apiBaseUrl in FuelrodApiInterface

- Chore: enhance error handling in HomeStepperActivity and comment out unused fragment additions

- Chore: update API endpoint and clear default API keys in SessionManager

- Chore: uncomment area unit and investment preference fragment additions in HomeStepperActivity

- Chore: integrate Sentry for error tracking in HomeStepperActivity

- Build(deps): bump cross-spawn from 6.0.5 to 6.0.6

Bumps [cross-spawn](https://github.com/moxystudio/node-cross-spawn) from 6.0.5 to 6.0.6.
- [Changelog](https://github.com/moxystudio/node-cross-spawn/blob/v6.0.6/CHANGELOG.md)
- [Commits](https://github.com/moxystudio/node-cross-spawn/compare/v6.0.5...v6.0.6)

---
updated-dependencies:
- dependency-name: cross-spawn
  dependency-type: indirect
...

Signed-off-by: dependabot[bot] <support@github.com>

- Merge pull request #334 from IITA-AKILIMO/fix/api-endpoint

refactor: disabled explicit jvm kotlin toolchain

- Chore: update GitHub Actions workflow to support manual triggers and upgrade upload-artifact action

- Chore: update bump-and-tag workflow for improved versioning and tagging process

- Chore: update PR automation workflow to use Ubuntu 24.04 and enable concurrency

- Chore: update TODO workflow to use Ubuntu 24.04 and enable concurrency

- Chore: remove unused BUILD_NUMBER variable from Android workflow

- Chore: remove unused test class CurrencyCodeTest

- Chore: add .kotlin to .gitignore

This change adds the `.kotlin` directory to the `.gitignore` file to prevent it from being tracked by Git.

- Chore: migrate from java to kotlin

Migrate files from java to kotlin for better consistency

- ♻️ refactor(dao): rename and update ProfileInfoDao to UserProfileDao

- Renamed `ProfileInfoDao` to `UserProfileDao` for improved clarity and consistency.
- Updated table references from `profile_info` to `user_profiles`.
- Renamed associated methods and parameters to align with the updated naming convention.

No functional changes, purely structural updates for better code readability and organization.

- ✨ **feat(BioDataFragment): migrate from Java to Kotlin**

- Converted `BioDataFragment` implementation from Java to Kotlin for improved code readability and maintainability.
- Replaced `ProfileInfo` with `UserProfile` for consistency.
- Introduced Kotlin-specific features like lazy initialization and null safety.
- Streamlined data binding and fragment lifecycle handling with Kotlin enhancements.

- ♻️ refactor(database): rename `ProfileInfo` to `UserProfile` and update associated DAO

- Renamed `ProfileInfo` entity to `UserProfile` for better clarity.
- Updated `profileInfoDao` to `userProfileDao` to reflect the entity rename.

No functional changes introduced; this is purely a refactor for improved naming consistency.

- ✨ refactor(DstOptionsFragment): migrate from Java to Kotlin

- Converted `DstOptionsFragment` implementation from Java to Kotlin for improved readability and maintainability.
- Preserved all existing functionality, including RecyclerView setup and navigation logic.
- Leveraged Kotlin features for concise and clearer code structure.

- ✨ **feat(fragments): migrate `InvestmentPrefFragment` from Java to Kotlin**

- Rewrote `InvestmentPrefFragment` in Kotlin to improve code readability and maintainability.
- Simplified data handling with Kotlin's concise syntax and null safety features.
- Maintained existing functionality, including investment preference selection and updates.

No changes to existing features or behavior.

- 🐛 fix(BaseStepFragment): handle null `locationInfo` more explicitly in `formatLocationInfo`

- Refactored `formatLocationInfo` to use explicit null-check for `locationInfo`.
- Simplified logic flow, returning an empty string when `locationInfo` is null.

- ♻️ refactor(PrivacyStatementFragment): migrate to Kotlin and improve code structure

- Converted `PrivacyStatementFragment` from Java to Kotlin.
- Applied modern Kotlin features for cleaner and more efficient code.
- Improved `WebView` configuration using a Kotlin-friendly syntax.
- Enhanced readability and maintainability of the fragment.

- ✨ **feat(SummaryFragment): migrate to Kotlin**

- Reimplemented `SummaryFragment` in Kotlin from Java.
- Ensured feature parity with the previous Java implementation.
- Improved readability and maintainability with Kotlin language features.

BREAKING CHANGE: Removed `SummaryFragment.java` and replaced it with `SummaryFragment.kt`. Users relying on Java-specific implementations or references will need to update accordingly.

- ♻️ refactor(fragments): migrate `WelcomeFragment` from Java to Kotlin

- Completely rewrote the `WelcomeFragment` class in Kotlin.
- Improved code readability and structure by leveraging Kotlin's concise syntax.
- Ensured functional parity with the original Java implementation.

- ✨ refactor(response): migrate `RecommendationResp` from Java to Kotlin

- Replaced `RecommendationResp` Java class with `RecommendationResponse` Kotlin data class.
- Simplified boilerplate code with Kotlin's concise syntax.
- Maintained `Parcelable` implementation for seamless Android compatibility.

- 🐛 fix(ManualTillageCostActivity): resolve context usage issues in string handling and Toast messages

- Ensure proper `context` usage with explicit class reference in Toasts and string resources
- Fix potential nullability issue by enforcing non-null currency symbol with `!!` operator
- Add explicit context declaration for better clarity in `initComponent`
- Replace ambiguous `context` with clear `this@ManualTillageCostActivity` reference

No breaking changes introduced.

- ♻️ refactor(MapBoxActivity): improve intent handling and remove unused `SessionManager`

- Removed unused `SessionManager` import and instance to streamline code.
- Replaced explicit `Intent` property setting with `apply` block for cleaner intent construction.
- Adjusted `Mapbox.getInstance` call to use explicit `this@MapBoxActivity` for clarity.

- ✨ feat(entities): make UserProfile fields nullable for improved flexibility

- Updated `gender` and `akilimoInterest` fields to be nullable, replacing default values.
- Enhanced support for unspecified or optional user inputs.

- 🔥 chore(models): remove unused model classes

- Deleted `FirebaseTopic`, `PriceModel`, and `ProducePrice` classes as they are no longer in use.
- Cleaned up unused imports and associated annotations.

This reduces code clutter and improves maintainability by removing obsolete resources.

- ♻️ refactor(models): simplify `RecommendationOptions` constructor

- Replaced multiple constructors with a primary constructor for cleaner initialization.
- Removed the deprecated constructor as `statusImage` is no longer required to be passed explicitly.

- ♻️ refactor(utils): migrate `CurrencyCode` from Java to Kotlin

- Rewrote `CurrencyCode` class in Kotlin for better interoperability and conciseness.
- Changed the Java class to a Kotlin `object` to streamline singleton usage.

This improves code readability, aligns with modern development practices, and enhances type safety.

- ✨ feat(widget): migrate `SpacingItemDecoration` to Kotlin

- Converted `SpacingItemDecoration` class from Java to Kotlin.
- Improved readability and leveraged Kotlin syntax for cleaner code.

- ♻️ refactor(adapters): migrate adapter classes from Java to Kotlin

- Refactored `FertilizerGridAdapter`, `FieldYieldAdapter`, `MaizePerformanceAdapter`, `MySpinnerAdapter`, `MyStepperAdapter`, and `RecommendationAdapter` from Java to Kotlin
- Improved code structure and readability using Kotlin's concise syntax
- Maintained existing functionality while ensuring compatibility

This increases code maintainability and takes advantage of Kotlin's modern language features.

- ♻️ refactor(dao): rename `MaizePerformanceDao` to `CropPerformanceDao`

- Replaced `MaizePerformanceDao` with `CropPerformanceDao` for broader applicability.
- Updated references from `maize_performance` to `crop_performance` in DAO queries and entity associations.

This change improves code clarity and aligns naming with the generalized functionality.

- ✨ feat(RootYieldActivity): enhance item click handling and dialog management

- Refactored `setOnItemClickListener` to use an explicit interface implementation for clarity.
- Simplified dialog arguments setup using Kotlin's `apply` scope function.
- Introduced `showDialogFragmentSafely` utility to streamline dialog showing and ensure safe fragment transactions.
- Removed unused `Sentry` reference to clean up unnecessary dependencies.

- 🚀 feat(database): bump database version and allow main thread queries

- Updated database version from 1 to 2.
- Enabled `.allowMainThreadQueries()` to facilitate operations (note: migrate to coroutines later).

BREAKING CHANGE: Database schema change requires destructive migration. Existing local data will be lost.

- 💄 style(AndroidManifest): improve organization and formatting

- Grouped and reordered permissions, meta-data, services, and activities for better readability.
- Added comments to clarify sections and provide context (e.g., permissions, services, splash activity).
- Minor formatting adjustments (e.g., spacing, consistency in `tools:replace`).

These changes enhance code maintainability without altering functionality.

- 👷 ci(workflows): update next-release workflow for improved automation

- Set `new_string` to a static message for release preparation
- Disable `get_diff` to streamline the process

- 📚 docs(github): add CODEOWNERS file to define repository ownership

- Specify `@masgeek` as the default owner for all files
- Improves clarity and assigns responsibility for changes within the repository


### Styling

- Style: annotated unimplemented functions

- Style: revise line endings


### Testing

- Test: update area conversion assertions in MathHelperTest

The assertions for area conversions in `MathHelperTest` have been updated to reflect more accurate values. Specifically, `convertAcreToHa`, `convertAcreToSQM`, and `convertAcreToAre` test assertions have been revised.


## [23.0.1] - 2023-08-03

### Bug Fixes

- Fix: added default value for unit price on error

- Fix: added default maize price on number error


### Miscellaneous Tasks

- Build(deps): bump semver from 5.7.1 to 5.7.2

Bumps [semver](https://github.com/npm/node-semver) from 5.7.1 to 5.7.2.
- [Release notes](https://github.com/npm/node-semver/releases)
- [Changelog](https://github.com/npm/node-semver/blob/v5.7.2/CHANGELOG.md)
- [Commits](https://github.com/npm/node-semver/compare/v5.7.1...v5.7.2)

---
updated-dependencies:
- dependency-name: semver
  dependency-type: indirect
...

Signed-off-by: dependabot[bot] <support@github.com>


## [23.0.0] - 2023-07-05

### Bug Fixes

- Fix: correted incorrect function name

- Fix: corrected incorrect currency endpoint

- Fix: added jackson ignore property


### Code Refactoring

- Refactor: revised wording for important information page

- Refactor: removed uncessesary variables

moved the varibales to be referenced directly via the binding. chain

- Refactor: removed uneeded variables

- Refactor: removed step index check

this will need clarification in future releases

- Refactor: updated database name

- Refactor: removed uneeded code block that was causing exceptions

- Refactor: switched to v1 for recommendations


### Documentation

- Docs: updated relaase notes

- Docs: updated release notes


### Features

- Feat: removed obsolete crashlytics logging

BREAKING CHANGE: replaced with sentry or new firebase logging

- Feat: added sntry logging libraries

- Feat: replaced TODO comments for crash logging


### Miscellaneous Tasks

- Doc/release notes 

* Update build.gradle

Removed NDL version

* build: updated NDK version\

* ci: set LATEST_TAG file to be non secret

* docs: updated release notes

- Build: updated gradle dependencies

- Ci: added new github rlease action

this new action automatically bumps and tags the release


### Styling

- Style: corrected typo


## [22.0.0] - 2023-05-18

### Bug Fixes

- Fix: updated email validation logic

- Fix: added proper context for getting string resource

- Fix: fixed ivalid export declaration

- Fix: added fix for selecting radiobutton for investment preference

radio button was not being selected based on saved value index because the id was incorrect switched it to use the item index value as the button id for ease of preselection

- Fix: renamed invalid preference key

- Fix: revised reference to removed layout element


### Code Refactoring

- Refactor: changed wording to revised version

- Refactor: revised data validation and default selection

revised data validation and default data selection for the biodata fragment page

- Refactor: revised country selection picker

revised country selection picker to show country flag and exclude phone code

- Refactor: updated wording

updateded wording and subsequent translations for the welcome string text

also updated the global font styles in the BaseTheme

- Refactor: removed extraneous padding for text views and layout

removed extra padding to improve textual displays

- Refactor: revised font styling and padding

- Refactor: removed all instances of fontfamily declaration

removed instances of fontfamily declaration in layout files, will now use the global style in the styles resource

- Refactor: updated string translations

- Refactor: re-ordered sumary page items

place technology preference summry last in the display

- Refactor: optimized conditions for string checks in order to assign boolean flags

- Refactor: revised field location heading title

- Refactor: revised english instruction text for location selection

- Refactor: revised stepper adapter

- Refactor: switched famr name summary

switched farm name summry to user farmName variable instead of fieldDescription

- Refactor: renamed RiskAttFragment to InvestmentPrefFragment

renamed RiskAttFragment to InvestmentPrefFragment including the xml layout file and binding references

BREAKING CHANGE:

- Refactor: renamed prefernece Never to rarely

- Refactor: renamed text for investment preference fragment

- Refactor: added translations for tz and rwanda

- Refactor: removed bckground color in root layout

- Refactor: revised field size fragment

updated logic to check for proper values and show context accurate text and labels

- Refactor: refactore layout container for checkboxes

- Refactor: renamed fragment to match view name

- Refactor: revised layout of first two view in wizard

- Refactor: removed the image tint color


### Features

- Feat: updated string translations

- Feat: updated summary text processing

- Feat: revised texts and layouts

revised text style and font type for dialog and updated string to new wording

- Feat: updated country validation

ensure geolocation of coordinates matches the specified country from the locationfragment

- Feat: updated location evaluation

updated location country code comparison to not be case sensitive

- Feat: added reverse geocode logic

added rvers geocode logic to verify county name and code as well asnd check for country data array size to prevent index exceptions

- Feat: redesigned fam location screen

redesigned farm location screen to accomodate field name provision

- Feat: revised country location verification and validation

esnured that the selected country matches the expected country after reverse geolocation

- Feat: added verification of country location

added verification of country location and revisd display of farm name

- Feat: added info on farm name

added info on farm name that ins specified by the user and added logic to udpate it on the database

- Feat: revised ui layout for investment preferences fragment

- Feat: added new translations

added new translations for swahili and kinyarwanda

- Feat: refactore logic for investment preference

refactored functionality for investment preference and removed aler dialog radio group option

BREAKING CHANGE: investment profile refactoring

- Feat: revised planting date fragment layout

- Feat: revised areunit fragment

revised area unit fragment to contain field size info

BREAKING CHANGE:

- Feat: added data deletion condition for profile info and mandatory infor tables

- Feat: added skip logic for areaunit fragment in the step wizard

- Feat: added option to skipp investment preference

screen will be skipped on the next app restart

- Feat: redesigned layout for tillage operations


### Miscellaneous Tasks

- Merge pull request #216 from IITA-AKILIMO/ci/notes-eneration

Ci/notes eneration

- Ci: disabled pr createor for beta releases


### Styling

- Style: revised line spacing in information fragment text

- Style: updated text color style

- Style: reverted back to empty text placeholder

- Style: revised font family layout

- Style: removed custom font color

\

- Style: revised country selection layout


### Testing

- Test: revised email regex test

revised email regex pattern and added new tests to ensure that the validation is correct


## [21.5.0] - 2023-04-12

### Code Refactoring

- Refactor: enhanced functions

enhanced functions and removed uneeded variables

- Refactor: switched to using TextUtils.empty()

used TextUtils.empty() to check for string validity

- Refactor: rearranged wizard screens for riskatt

rearranged wizard screen for risk att and removed redundant commented out code

- Refactor: rewoded english question for farm location

- Refactor: re-enabled steppers


### Documentation

- Docs: updated documentation

updated changelog, release notes and histry file


### Features

- Feat: added deletAll() method to all dao objects

added elete all to allow item specific deletion for tables

- Feat: changed mapbox style

changed mapbox style to satellite_street for easy identificaton of areas

Mapbox old version is still used, cosider migrating in the future

- Feat: set new map zoom factor

set map zoom factor to 17


### Miscellaneous Tasks

- Ci: added bump and tag build step

- Ci: change concurrency id for bunp and tag

- Merge pull request #215 from IITA-AKILIMO/develop

ci: change concurrency id for bunp and tag


## [21.4.4] - 2023-04-04

### Features

- Feat: added new ui revisions


## [21.4.3] - 2022-10-04

## [21.4.2] - 2022-08-03

### Code Refactoring

- Refactor: improved webview loading


### Features

- Feat: updated remote config logic


## [21.4.1] - 2022-08-03

### Bug Fixes

- Fix: corrected state where variables values were swapped for summary view

- Fix: removed invalid line in function

removed accidnetla comment in code and change test results agent for github actions

- Fix: updated in app terms link


### Miscellaneous Tasks

- Ci: set LATEST_TAG file to be non secret


### Testing

- Test: added teste for computing ivestment amount


## [21.4.0] - 2022-07-29

### Bug Fixes

- Fix: invalid min and maximum price

- Fix: placeholder order correction

added language specific placeholder for fertilizer investment amount display


### Code Refactoring

- Refactor: removed debug context code

- Refactor: added translations to kiswahili


### Features

- Feat: added new layout

new layout style for biodata page

- Feat: ui revision

revision of biodata UI


## [21.3.0] - 2022-04-05

### Features

- Feat: added burundi

added burundi to list of DST countries


## [21.2.4] - 2022-02-02

### Bug Fixes

- Fix: fixed intercrop fertilizer prices

added proper filter for loading intercrop fertilizers prices

- Fix: fixed seleted price

fixed selected prie evaluation


## [21.2.3] - 2022-02-01

### Bug Fixes

- Fix: updated endpoint

updated endpoint for getting intercrop fertilizers


## [21.2.2] - 2021-11-25

### Bug Fixes

- Fix: added proper conversion for save valud for max investment

- Fix: added proper evaluation for step skipping for views


### Miscellaneous Tasks

- Ci: updated concurrency group to use github ref


## [21.2.1] - 2021-11-25

### Bug Fixes

- Fix: added proper conversion for save valud for max investment

- Fix: added proper evaluation for step skipping for views


### Miscellaneous Tasks

- Ci: updated concurrency group to use github ref


## [21.2.0] - 2021-11-24

### Bug Fixes

- Fix: added dynamic fertilizerpriecs

- Fix: fixed invalid index range for radio button tags

- Fix: corrected shared pref ref key for GHS rate

- Fix: disbaled rwanda data points

- Fix: added current practices skipping if country is ghana


### Code Refactoring

- Refactor: corrected sort order or records


### Features

- Feat: added step skipper

added step skipper evaluation for ghana as a country

- Feat: added dev endpoint to test new payload fetching

- Feat: added dynamic investment amount

added dynamic investment amount from soruce

BREAKING CHANGE:


### Miscellaneous Tasks

- Ci: resticted actions to be for specific branches

- Ci: updated repo

- Ci: added on pull request action for non main branches

- Ci: revised on pull request conditions

- Ci: github actions

- Ci: added branch name testing

- Ci: updated test steps

- Ci: testing

- Ci: testing some more

- Ci: adde branch evaluation

- Ci: fixed invalid closing line in line 16

- Ci: added concurrency bits

- Ci: added concurrency group

- Ci: updated stuff

- Ci: concurrency step

- Ci: concurrency testing

- Ci: updated consurrency step section

- Ci: disabled mock building


### Styling

- Style: removed extra blank lines


## [21.1.0] - 2021-11-23

### Bug Fixes

- Fix: added evaluation for ARE area units


### Documentation

- Docs: updated whats new release notes


### Miscellaneous Tasks

- Ci: added beta release channels

- Ci: updated build step java version


## [21.0.2] - 2021-11-18

## [21.0.1] - 2021-11-18

### Bug Fixes

- Fix: area unit conversion

added proper handling and conversion of are unit type

- Fix: wrong area unit checked

added fix for checking of wrong are unit in ase of RWANDA

- Fix: added custom dialog fragment to prevent crashes on inflation


### Code Refactoring

- Refactor: revised kotlin version string

changed to use globally declared value

- Refactor: reenabled debugging views bypass

- Refactor: corrected annotations and removed extra slashes in trnslations

- Refactor: added checker for starch factory count

- Refactor: updated in app database name


### Documentation

- Docs: updated release notes


### Features

- Feat: added checks for country to determine use case and area units

- Feat: added kinyarwanda translations


### Miscellaneous Tasks

- Ci: added env variable

added env variable for package name

- Ci: added workflow

added new todo checker

- Merge pull request #150 from IITA-AKILIMO/ci/package

ci: added env variable

- Ci: todo actions

added actions to open issues when todo comments are detected

- Ci: added release status in google uploader

- Merge pull request #153 from IITA-AKILIMO/ci/releases

ci: added release status in google uploader


## [21.0.0] - 2021-11-09

### Bug Fixes

- Fix: currency object processing

added null check for currency object, removed extra slash in currency endpoint

- Fix: added proper planting windows check

addeed price reseters if factory is selected

- Fix: fertilizer list clenup logic

cleanup inactive fertilizers from tables

BREAKING CHANGE:

- Fix: added proper room database annotation

- Fix: added proper case switching fo swahili words

- Fix: added proper translation of swahili string and positioning

added code to fetch transdlated versions of the strings in swahili language

BREAKING CHANGE:

- Fix: fixed ui tranlation strings for kiswahili

- Fix: fixed null locate issue casuing interfaces to crash

- Fix: merge conflict

fix merge conflict in android.yml actions file

- Fix: updated repo names

- Fix: translations

updated swahili translation to proper tense and diction

- Fix: crashing maps

crashing map selection in android 11 due to new security checks


### Code Refactoring

- Refactor: enabled correct entry activity

- Refactor: downgraded kotlin versions

- Refactor: updated build steps in android.yml actions file

- Refactor: git hooks

updated git hooks for better workflow

- Refactor: removed extraneous semicolon

removed extraneous semicolon

- Refactor: package renaming

renaming of package after dev account change

BREAKING CHANGE: T

- Refactor: removed bpkp folder


### Documentation

- Docs: added changelog genration

proper changelog generation using golang tools

- Docs: updated whats new notes

- Docs: updated changelog

- Docs: updated changelog file

- Docs: updated changelog file

- Docs: added release notes for swahili version

- Docs(change-logs): updated change log file

- Docs: updated changelog

- Docs: README update

updated links in reame file

- Docs: updated release notes

updates release notes file to have latest release notes


### Features

- Feat: dependencies updates

- Feat: added ui to handle harvest window prices for month 1&2

added ui to handle harvest window prices for month 1 and month 2 windows

BREAKING CHANGE:

- Feat: translation

added translation for kinyarwanda

- Feat: Rwanda country selection

added rwanda country selection, ui translation and in app vlaidation

BREAKING CHANGE:

- Feat: harmonized packages

harmonized packages and removed ads library

- Feat: ghana support

added data processing support for ghana


### Miscellaneous Tasks

- Added version specific image resources and syles

- Updated dependencies

- Added bg with opacity logo

- Added pricing information

- Updates to dependcies

- Added circleci config

- Added circleci config

- Added circleci config

- Disabled circleci temporarily

- Updated depenencies

- Merge pull request #84 from masgeek/chore/cicd

Chore/cicd

- Ci: removed extra comments

- Ci: addee cz quality checker

Added CZ quality checker

- Ci: updated actions flow and branch with develop changes

updated github actions with some changes from develop

- Ci: build condition step revision

- Merge pull request #91 from masgeek/fix/cicd

ci: build condition step revision

- Ci: removed git quality check steps

remove git quality checks step

- Ci: added step for beta branch build

beta branch build workflow

- Ci: disabled on pull request build

- Ci: added auto pr action to github CI

- Ci: added title to beta release workflow

- Ci: disble Pr draft flag in pr automater

- Merge pull request #98 from masgeek/ci/pr-flag

ci: disble Pr draft flag in pr automater

- Pulling refs/heads/develop into beta 

* Removed incorrect step conditions

* Code styling updates

* Removed incorrect step conditions

* Added market wizard step fragment

* docs: updated whats new notes

* feat: dependencies updates

* ci: disabled on pull request build

* feat: added ui to handle harvest window prices for month 1&2

added ui to handle harvest window prices for month 1 and month 2 windows

BREAKING CHANGE:

* fix: added proper planting windows check

addeed price reseters if factory is selected

* ci: added auto pr action to github CI

* ci: added title to beta release workflow

* fix: fertilizer list clenup logic

cleanup inactive fertilizers from tables

BREAKING CHANGE:

* refactor: enabled correct entry activity

* ci: disble Pr draft flag in pr automater

Co-authored-by: Tsobu Developer <dev@tsobu.co.ke>
Co-authored-by: Sammy Barasa <barsamms@gmail.com>

- Version 18.6.0 

* Removed incorrect step conditions

* Code styling updates

* Removed incorrect step conditions

* Added market wizard step fragment

* docs: updated whats new notes

* feat: dependencies updates

* ci: disabled on pull request build

* feat: added ui to handle harvest window prices for month 1&2

added ui to handle harvest window prices for month 1 and month 2 windows

BREAKING CHANGE:

* fix: added proper planting windows check

addeed price reseters if factory is selected

* ci: added auto pr action to github CI

* ci: added title to beta release workflow

* fix: fertilizer list clenup logic

cleanup inactive fertilizers from tables

BREAKING CHANGE:

* refactor: enabled correct entry activity

* ci: disble Pr draft flag in pr automater

* docs: updated changelog

Co-authored-by: Tsobu Developer <dev@tsobu.co.ke>
Co-authored-by: Sammy Barasa <barsamms@gmail.com>

- Ci: enhanced build step to exclude tests

- Merge pull request #110 from masgeek/feature/nextrelease

ci: enhanced build step to exclude tests

- Ci: reenable github actions by renaming

- Ci: removed bundlerelease step in unit test step

- Build: updated dependencies version

- Ci: updated java version

- Merge pull request #122 from IITA-AKILIMO/cicd/builds

ci: updated java version

- Ci: updated env values

added line to include mapbox download token

- Build(updated-dependencies): updated dependencies and build references

- Ci: disabled goog play publisher step


### Revert

- Reverted to no arrow card layout for fertilizer rec view


### Styling

- Style: code style formatting


<!-- generated by git-cliff -->
