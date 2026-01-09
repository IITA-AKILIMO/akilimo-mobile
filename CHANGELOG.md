# Changelog

All notable changes to AKILIMO will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Documentation

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog


## [29.0.2] - 2026-01-09

### Documentation

- Update changelog 

- Update changelog

- Update changelog

- Update changelog

- Update changelog

- Update changelog


### Miscellaneous Tasks

- **workflow**: Update git push command in release


## [29.0.1] - 2025-12-30

### CI

- Automate changelog generation in release workflow


### Miscellaneous Tasks

- **workflow**: Fix changelog commit message format

- Improve changelog generation workflow


## [29.0.0] - 2025-12-30

### Code Refactoring

- **location**: Migrate to mapbox maps sdk v10

- **permissions**: Modernize permission handling logic

- **permissions**: Abstract location permission checks

- **maps**: Migrate LocationPickerActivity to Mapbox SDK v11

- **location**: Modernize location fetching logic


### Features

- **deps**: Update mapbox sdk and add new dependencies [**BREAKING**]

- **res**: Add location pin icon

- **deps**: Integrate swipe-to-refresh and location services

- **location**: Add location helper utility

- **permissions**: Add permission helper class


### Miscellaneous Tasks

- Trigger release workflow on push to main


### Revert

- **release**: Set next version to 28.0.0


## [28.1.0] - 2025-12-28

### Bug Fixes

- **tests**: Correct EnumCountry mapping for TZ test


### Code Refactoring

- **tillage**: Use EnumOperation enum directly

- **MandatoryInfo**: Remove unused radio index fields

- **navigation**: Update intent in FrActivity

- **recommendation**: Rename DstRecommendationActivity to GetRecommendationActivity

- Remove unused SPH activity layout

- **recommendation**: Implement use case screen

- **ui**: Animate selection state in adapters

- **ui**: Remove text color animation from adapters

- **theme**: Update gradient overlay color

- **recommendations**: Tweak recommendation card image layout

- **recommendation**: Update intercropping with sweet potato use cases

- **ui**: Adjust window insets handling in BaseActivity

- **recommendation**: Invert icon visibility logic

- **recommendations**: Clean up and enable recommendation options

- **ui**: Re-enable stepper fragments and integrate `SimpleStepperListener`

- **ui**: Use string resources for exit dialog text in `HomeStepperActivity`

- **ui**: Simplify `sizeUnitLabel` retrieval in `WeedControlCostsActivity`

- **db**: Re-order imports and cleanup in `AppDatabase`


### Features

- **security**: Update network security configuration

- **SplashActivity**: Initialize Retrofit with context

- **security**: Add network security configuration

- Migration to git history

- **recommendations**: Add more advice options

- **recommendations**: Enable more advice options for IC Maize

- **build**: Define base URLs in default config and remove from release/debug

- **build**: Automate `versionName` and `versionCode` generation


### Fix

- Correct repeatOnLifecycle state for viewModel observation


### Miscellaneous Tasks

- **Akilimo**: Remove comment from AndroidThreeTen initialization

- Update focus colors

- **strings**: Update string resource keys for sweet potato

- **enums**: Update string resource key for sweet potato market outlet

- **manifest**: Add SweetPotatoMarketActivity to AndroidManifest

- **enums**: Comment out weed management advice

- **ci**: Temporarily disable actor check in PR automation workflow

- **ui**: Add `SimpleStepperListener` base implementation for stepper events

- **network**: Add `LocalDateAdapter` for JSON (de)serialization with Moshi

- **utils**: Add `NumberHelper` with null-safe Double extensions

- **utils**: Add `orUnavailable` extension for null-safe default strings

- **network**: Add `LocalDateAdapter` to Moshi instance in `ApiClient`

- **network**: Add POST endpoint for computing recommendations in `AkilimoApi`

- **enum**: Add `produce` function to `EnumProduceType` for mapping produce types

- **enum**: Update `EnumUnitOfSale` weights to `Double` type for precision adjustments

- **model**: Remove default values in `UserInfo` properties and add `riskAttitude` field

- **utils**: Refactor `RecommendationBuilder` to improve readability and modularize logic

- **ui**: Integrate `RecommendationBuilder` and `AkilimoApi` in `GetRecommendationActivity` for API-based recommendations

- **model**: Refactor `ComputeRequest` structure and field types for improved consistency and clarity

- **ui**: Add `EnumUseCase` handling in various activities for improved use case specificity

- **ui**: Update `GetRecommendationActivity` to set toolbar title and enhance recommendation handling logic

- **ui**: Pass `EnumUseCase` as Parcelable to `GetRecommendationActivity` for enhanced intent handling

- **utils**: Update `RecommendationBuilder` to set intercrop recommendations based on `EnumUseCase`

- **ui**: Refactor coroutine scope usage in `GetRecommendationActivity` for streamlined thread handling and error management

- **ui**: Update card background and elevation styles in `fragment_area_unit.xml` for layout consistency

- **ui**: Update card elevation and background styles in `fragment_bio_data.xml` for visual consistency

- **utils**: Update `RecommendationBuilder` logic to conditionally set recommendations based on `EnumUseCase`

- **ui**: Simplify `Toast` usage in `GetRecommendationActivity` by removing fully qualified references

- **ui**: Update card background styles across layouts to use `?attr/colorSurfaceVariant` for consistency

- **release**: Bump version to `30.0.0` in `nextrelease.txt`

- **config**: Update git-cliff configuration for AKILIMO


### Refactor

- Rename `EnumOperationType` to `EnumOperationMethod`

- Rename email validation function and add empty check

- Remove unused function and add TODO for encryption


### ✨feat

- **inherit**: Remove deprecated properties and update annotations [**BREAKING**]


### 💄fix

- **flow**: Always show area unit and investment preference fragments


## [26.3.0] - 2025-06-05

### Code Refactoring

- Move `initToolbar` deprecation notice

- **FieldSizeFragment**: Simplify radio button selection logic

- Replace `MyBaseActivity` with `BaseActivity`


### Features

- **icon**: Add back arrow icon

- Introduce `MyBaseActivity` as a base for activities

- **strings**: Add location selected prompt

- Introduce `BindBaseActivity` for ViewBinding


### Miscellaneous Tasks

- **activity_investment_amount.xml**: Update layout and component IDs

- Improve layout structure and naming in `activity_recommendations_activity.xml`

- Update GPSTracker and location retrieval logic

- **fieldSize**: Correct data validation logic

- Remove deprecated `initToolbar` and `initComponent` methods from `MyBaseActivity`


### Refactor

- **RecommendationsActivity**: Modernize toolbar and RecyclerView setup

- **InvestmentAmountActivity**: Extend MyBaseActivity and remove deprecated methods

- **GPSTracker**: Simplify location retrieval and improve clarity

- Move recommendation items initialization to a separate function

- Remove redundant UserProfile field

- Update `BaseRecommendationActivity` inheritance and remove deprecated methods

- Use `setupToolbar` extension in DatesActivity

- Simplify initialization in `CassavaMarketActivity`

- Make `validate` method in `BaseActivity` optional

- InvestmentAmountActivity inherits from BaseActivity

- Modernize layout in `content_cassava_market_outlet.xml`

- Improve code structure and readability in `CassavaMarketActivity`


## [26.0.0] - 2025-05-27

### Bug Fixes

- **dev**: Comment out direct navigation to RecommendationsActivity

- **WelcomeFragment**: Remove unused HomeStepperActivity import

- **app**: Remove unused libraries and update initialization


### Code Refactoring

- Move operation cost response to entities and update endpoint to use country code

- Update field names in compute request object

- Replace deprecated `kotlinx.android.parcel.Parcelize` with `kotlinx.parcelize.Parcelize`

- Rename UseCases to UseCase and update related code

- Update version code computation in `app/build.gradle`

- **db**: Update database name and reset version

- **.github/workflows**: Simplify `merge-to-develop.yml` trigger


### Docs

- Update comment for `applocale` dependency


### Features

- **sentry**: Ppdate AndroidManifest.xml

- Update OperationCost entity to use Long for ID and add JsonProperty annotation

- Rename and refactor UseCaseDao to align with data layer principles

- Refactor field operation cost handling and update use cases

- Add dimen_58 to dimens.xml

- Add FertilizerRecommendationActivity and update AndroidManifest

- **locale**: Introduce language selection and persistence

- **ui**: Add `dimen_36` to dimens.xml

- **strings**: Add `lbl_remember_details` string resource

- **BioDataFragment**: Use custom spinner adapter and simplify UI logic

- **icon**: Update calendar icon

- **permissions**: Add POST_NOTIFICATIONS permission

- **security**: Disable cleartext traffic in AndroidManifest

- **backup**: Disable auto backup

- **sonar**: Configure connected mode


### Fix

- Initialize `displayAreaUnit` to empty string in `MandatoryInfo`


### Miscellaneous Tasks

- **TillageOperationFragment**: Streamline dialog management and simplify operation type handling

- Remove `ci/fix-auto-approve` branch from next-release workflow triggers

- Move companion object to top of `InfoFragment`

- Update app language handling in `BaseActivity`

- **fragment_bio_data.xml**: Update UI with Material Design components

- Update and remove dependencies

- Remove unused methods from `Tools.kt`

- Update error handling and deprecate methods in DstRecommendationActivity

- Update card corner radius and padding in harvest and planting layouts

- Remove explicit `fontFamily` from  layouts

- Remove unused tag-runner.yml workflow

- Remove unused imports

- Remove unused Mapbox token variable in `computeVersionCode`

- **AreaUnitFragment**: Update UI and remove unused code

- **FieldSizeFragment**: Migrate to ConstraintLayout and improve logic

- Rename IDs in `fragment_planting_harvest_date.xml`

- **workflows**: Standardize job names


### Refactor

- Simplify error view visibility in `DstRecommendationActivity`

- Rename `title` to `importantInfoTitle` in `fragment_info.xml`

- Add detekt baseline file

- Remove unused `getLocalizedName` method from `EnumOperationType`

- Migrate `fragment_welcome.xml` to Material Components

- Convert `Recommendations` class to data class `Recommendation`

- Update variable names and improve date validation in PlantingDateFragment

- Use `Recommendation` data class and simplify code

- Rename `Recommendations` data class to `Recommendation`

- **ui**: Update country fragment with Material Design components

- Update view IDs in `BioDataFragment` and its layout

- Update DstOptionsFragment and layout


### Deps

- Update Android Gradle Plugin from 8.5.1 to 8.5.2


### ✨feat

- **localization**: Integrate LanguageManager for consistent language handling


## [24.2.1] - 2023-11-27

### Documentation

- Updated documentatoin for releases 


## [23.0.2] - 2023-08-21

### Bug Fixes

- Added null check for country code

- Update query filter

- Update IFertilizerDismissListener to allow nullable Fertilizer

- Use currencyCode instead of currency to get currency symbol

- Calculate total investment amount based on different area units


### Code Refactoring

- Removed accidental comment

- Removed eprecatd configuration in gradle build script

- Added replacment vector icon

- Revised result code for cancelled updates

- Moved inAppUpdate class to utils package

- Revised updateflow to use non deprecated method

- Reduced code smell for null checks

- Code cleanup

- Switched to TextUtils

- Removed dead code

- Deleted deprecated fieldinfo fragment class

- Optimized fragment view class

- Rewrote code to be more readable

- Added deprecated command

- Disabled explicit jvm kotlin toolchain

- Rename FuelrodApiInterface to FuelrodService and update its implementation

- Update dialog fragment instantiation

- Remove redundant `dialog` variable in `FertilizerPriceDialogFragment`

- Update dialog fragments to use view binding and remove redundant code

- Initialize sessionManager, mathHelper, and database in BaseDialogFragment

- Replace deprecated `kotlinx.android.parcel.Parcelize` with `kotlinx.parcelize.Parcelize`

- Use binding delegate pattern in FieldSizeFragment

- Remove temporary redirection to RecommendationsActivity in SplashActivity

- Convert TheItemAnimation from Java to Kotlin

- Update RecommendationsActivity declaration in AndroidManifest

- Update `tools:context` in `activity_recommendations_activity.xml` to reflect package change

- Rename LocationInfo entity to UserLocation

- Rename LocationInfoDao to UserLocationDao and update related classes


### Documentation

- Updated documentatoin for releases

- Added TODO comment


### Features

- Updated gradle and kotlin version [**BREAKING**]

- Added google updated checker

- Added country information text on selection

- Updated translations

- Reducd code complexity for the switch case

- Gradle update [**BREAKING**]

- Migrate RecommendationsActivity to Kotlin and use Retrofit for currency updates

- Add AkilimoCurrency entity with Room integration

- Remove currency entity

- Update Currency entity to AkilimoCurrency and update queries

- Update libraries for networking, logging, navigation and activities

- Update Room database version to 2

- Add retrofit API service for currency, fertilizer and fertilizer price endpoints

- Update AndroidManifest.xml

- Migrate FertilizersActivity from Java to Kotlin and use Retrofit for API calls

- Add RetrofitManager and RetroFitFactory for API communication

- Upgrade database version to 3 and update database name

- Update table name and column in CassavaPriceDao

- Update cassava price entity with new fields and data types

- Update fertilizer entity to match API response

- Remove fertilizer type from list

- Update FertilizerPrice entity and add FertilizerPriceResponse data class

- Update FertilizerPrice entity and add FertilizerPriceResponse data class

- Update cassava price query to use country code

- Update fertilizer table name and column names in FertilizerDao

- Update session manager to use fuelrod endpoint

- Initialize Retrofit in SplashActivity and add ApiTestActivity

- Deprecate RestService and update endpoint retrieval

- Use AkilimoCurrency entity instead of Currency entity in TractorAccessActivity and WeedControlCostsActivity

- Allow cleartext traffic for specific domains

- Use AkilimoCurrency entity instead of Currency

- Update recommendations activity to use retrofit

- Load api endpoint from remote config

- Add investment amounts API endpoint

- Remove investmentAmountDto database table

- Update profile information in the database

- Update compute request to include user full names and max investment

- Migrate InvestmentAmountActivity from Java to Kotlin

- Remove unused InvestmentAmountDto files

- Update LocationFragment to use names() method from profileInfo

- Add names method to ProfileInfo to return formatted full name

- Use String.format for summary title in SummaryFragment

- Remove investmentAmountDtoDao().deleteAll() from SplashActivity

- Update investment amount entity

- Add methods to InvestmentAmountDao for finding by ID and batch insertion

- Remove volley request classes

- Implement sweet potato market price screen logic

- Implement user review submission via API

- Change survey request properties to snake case

- Remove unused rest service class

- Improve intercrop fertilizer recommendation logic

- Handle API request errors for fertilizer prices

- Show loading progress when requesting recommendations

- Allow for empty cost items in operation costs

- Add API endpoint to submit user reviews

- Improve MySurveyActivity layout

- Update cassava market activity to handle market survey data

- Add entities, API endpoints, and database migrations for price data

- Convert CassavaPriceDialogFragment from Java to Kotlin

- Migrate IntercropFertilizersActivity from Java to Kotlin and use Retrofit for API calls

- **refactor**: Major refactoring   BREAKING_CHANGES: ui rewrites

- Migrate MaizePerformanceDialogFragment from Java to Kotlin

- Refactor operation costs dialog fragment for better handling and reusability

- Remove context from base dialog fragment

- Refactor cassava price dialog fragment and update arguments

- Remove deprecated IntercropFertilizerPriceDialogFragment and update CassavaPriceDialogFragment initialization

- Add FertilizerPriceDialogFragment for managing fertilizer prices

- Remove unused FertilizerPriceDialogFragment

- Use empty constructor for FertilizerPriceDialogFragment

- Add Maize performance dialog fragment

- Migrate OperationTypeDialogFragment from Java to Kotlin

- Enable error reporting to Sentry in FertilizerRecActivity

- Update MaizePerformanceDialogFragment initialization in MaizePerformanceActivity

- Migrate RootYieldDialogFragment from Java to Kotlin

- Enable cancelling OperationTypeDialogFragment dialog by tapping outside

- Migrate RootYieldActivity from Java to Kotlin

- Update field yield dialog dismissal listener signature

- Deprecate unused fields in BaseActivity

- Refactor FieldYield entity with Room annotations and Parcelize

- Rename table field_yield to field_yields

- Migrate `SingleSelectDialogFragment` from Java to Kotlin

- Migrate MaizePriceDialogFragment to Kotlin

- Migrate `DateDialogPickerFragment` from Java to Kotlin

- Migrate `MathHelper` class to Kotlin

- Migrate `WeedControlCostsActivity` from Java to Kotlin

- Convert TractorAccessActivity from Java to Kotlin

- Refactor `WeedControlCostsActivity` to improve code readability and maintainability

- Round the converted field size to 2 decimal places instead of 1

- Replace field size fragment with Kotlin version

- Enhance currency conversions, formatting, and area calculations

- Update getFertilizers API to use path parameter for country code

- Improve price selection logic in CassavaPriceDialogFragment

- Migrate `SessionManager` to Kotlin

- Migrate BaseStepFragment to Kotlin

- Add String extension to handle null or blank strings

- Refactor currency conversion in MathHelper

- Migrate area unit fragment to Kotlin

- Refactor BaseStepFragment for improved dependency management and code clarity


### Miscellaneous Tasks

- Revised revision tools and github actions sequence

- Added android sdk setup step

- Testing android build

- Disabled android setup step

- Downgraded build tools

- Added pull request automation

- Added next release github action

- **deps**: Bump ejs from 3.1.9 to 3.1.10 

- Added tag fetcher

- Changed workflow name

- Changed workflow name

- Updated android ci/cd workflow filw

- Updated actions file

- Revised build number in CI

- **deps**: Bump actions/download-artifact in /.github/workflows

- Remove deprecated pull request workflow

- Add VSCode settings for interactive Java build configuration

- Disable interactive build configuration in VSCode settings

- Remove experimental android extensions configuration

- Update dependencies and repository configurations in build.gradle

- Update base URL in FuelrodApiInterface to new endpoint

- Update JSON property names in RemoteConfig to use snake_case

- Rename BASE_URL parameter to apiBaseUrl in FuelrodApiInterface

- Enhance error handling in HomeStepperActivity and comment out unused fragment additions

- Update API endpoint and clear default API keys in SessionManager

- Uncomment area unit and investment preference fragment additions in HomeStepperActivity

- Integrate Sentry for error tracking in HomeStepperActivity

- **deps**: Bump cross-spawn from 6.0.5 to 6.0.6

- Update GitHub Actions workflow to support manual triggers and upgrade upload-artifact action

- Update bump-and-tag workflow for improved versioning and tagging process

- Update PR automation workflow to use Ubuntu 24.04 and enable concurrency

- Update TODO workflow to use Ubuntu 24.04 and enable concurrency

- Remove unused BUILD_NUMBER variable from Android workflow

- Remove unused test class CurrencyCodeTest

- Add .kotlin to .gitignore

- Migrate from java to kotlin


### Styling

- Annotated unimplemented functions

- Revise line endings


### Testing

- Update area conversion assertions in MathHelperTest


### Build

- Updated target sdk and min sdk version

- Upgraded gradle version

- Updated to JDK 21

- Updated gradle config

- Updated jvm toolchains

- Update branch syntax in next release workflow


## [23.0.1] - 2023-08-03

### Bug Fixes

- Added default value for unit price on error

- Added default maize price on number error


### Miscellaneous Tasks

- **deps**: Bump semver from 5.7.1 to 5.7.2


## [23.0.0] - 2023-07-05

### Bug Fixes

- Correted incorrect function name

- Corrected incorrect currency endpoint

- Added jackson ignore property


### Code Refactoring

- Revised wording for important information page

- Removed uncessesary variables

- Removed uneeded variables

- Removed step index check

- Updated database name

- Removed uneeded code block that was causing exceptions

- Switched to v1 for recommendations


### Documentation

- Updated relaase notes

- Updated release notes


### Features

- Removed obsolete crashlytics logging [**BREAKING**]

- Added sntry logging libraries

- Replaced TODO comments for crash logging


### Miscellaneous Tasks

- Updated gradle dependencies

- Added new github rlease action


### Styling

- Corrected typo


### Build

- Updated gradle version


## [22.0.0] - 2023-05-18

### Bug Fixes

- Updated email validation logic

- Added proper context for getting string resource

- Fixed ivalid export declaration

- Added fix for selecting radiobutton for investment preference

- Renamed invalid preference key

- Revised reference to removed layout element


### Code Refactoring

- Changed wording to revised version

- Revised data validation and default selection

- Revised country selection picker

- Updated wording

- Removed extraneous padding for text views and layout

- Revised font styling and padding

- Removed all instances of fontfamily declaration

- Updated string translations

- Re-ordered sumary page items

- Optimized conditions for string checks in order to assign boolean flags

- Revised field location heading title

- Revised english instruction text for location selection

- Revised stepper adapter

- Switched famr name summary

- Renamed prefernece Never to rarely

- Renamed text for investment preference fragment

- Added translations for tz and rwanda

- Removed bckground color in root layout

- Revised field size fragment

- Refactore layout container for checkboxes

- Renamed fragment to match view name

- Revised layout of first two view in wizard

- Removed the image tint color


### Features

- Updated string translations

- Updated summary text processing

- Revised texts and layouts

- Updated country validation

- Updated location evaluation

- Added reverse geocode logic

- Redesigned fam location screen

- Revised country location verification and validation

- Added verification of country location

- Added info on farm name

- Revised ui layout for investment preferences fragment

- Added new translations

- Refactore logic for investment preference [**BREAKING**]

- Revised planting date fragment layout

- Added data deletion condition for profile info and mandatory infor tables

- Added skip logic for areaunit fragment in the step wizard

- Added option to skipp investment preference

- Redesigned layout for tillage operations


### Miscellaneous Tasks

- Disabled pr createor for beta releases


### Security

- Updated android manifest


### Styling

- Revised line spacing in information fragment text

- Updated text color style

- Reverted back to empty text placeholder

- Revised font family layout

- Removed custom font color

- Revised country selection layout


### Testing

- Revised email regex test


### Build

- Updated release notes generation

- Updated npm packages

- Removed uneeded chnagelog dependency

- Updated to gradle 7.2 [**BREAKING**]

- Added retrofit libraries and upgrades others for non breaking changes


## [21.5.0] - 2023-04-12

### Code Refactoring

- Enhanced functions

- Switched to using TextUtils.empty()

- Rearranged wizard screens for riskatt

- Rewoded english question for farm location

- Re-enabled steppers


### Documentation

- Updated documentation


### Features

- Added deletAll() method to all dao objects

- Changed mapbox style

- Set new map zoom factor


### Miscellaneous Tasks

- Added bump and tag build step

- Change concurrency id for bunp and tag


### Build

- Added npm packages for building and creating release notes


## [21.4.4] - 2023-04-04

### Features

- Added new ui revisions


## [21.4.2] - 2022-08-03

### Code Refactoring

- Improved webview loading


### Features

- Updated remote config logic


## [21.4.1] - 2022-08-03

### Bug Fixes

- Corrected state where variables values were swapped for summary view

- Removed invalid line in function

- Updated in app terms link


### Miscellaneous Tasks

- Set LATEST_TAG file to be non secret


### Testing

- Added teste for computing ivestment amount


### Build

- Updated NDK version\

- Enabled jacoco coverage reporting


## [21.4.0] - 2022-07-29

### Bug Fixes

- Invalid min and maximum price

- Placeholder order correction


### Code Refactoring

- Removed debug context code

- Added translations to kiswahili


### Features

- Added new layout

- Ui revision


## [21.3.0] - 2022-04-05

### Features

- Added burundi


## [21.2.4] - 2022-02-02

### Bug Fixes

- Fixed intercrop fertilizer prices

- Fixed seleted price


## [21.2.3] - 2022-02-01

### Bug Fixes

- Updated endpoint


## [21.2.2] - 2021-11-25

### Bug Fixes

- Added proper conversion for save valud for max investment

- Added proper evaluation for step skipping for views


### Miscellaneous Tasks

- Updated concurrency group to use github ref


## [21.2.1] - 2021-11-25

### Bug Fixes

- Added proper conversion for save valud for max investment

- Added proper evaluation for step skipping for views


### Miscellaneous Tasks

- Updated concurrency group to use github ref


## [21.2.0] - 2021-11-24

### Bug Fixes

- Added dynamic fertilizerpriecs

- Fixed invalid index range for radio button tags

- Corrected shared pref ref key for GHS rate

- Disbaled rwanda data points

- Added current practices skipping if country is ghana


### Code Refactoring

- Corrected sort order or records


### Features

- Added step skipper

- Added dev endpoint to test new payload fetching


### Miscellaneous Tasks

- Resticted actions to be for specific branches

- Updated repo

- Added on pull request action for non main branches

- Revised on pull request conditions

- Github actions

- Added branch name testing

- Updated test steps

- Testing

- Testing some more

- Adde branch evaluation

- Fixed invalid closing line in line 16

- Added concurrency bits

- Added concurrency group

- Updated stuff

- Concurrency step

- Concurrency testing

- Updated consurrency step section

- Disabled mock building


### Styling

- Removed extra blank lines


### Build

- Updated library version


## [21.1.0] - 2021-11-23

### Bug Fixes

- Added evaluation for ARE area units


### Documentation

- Updated whats new release notes


### Miscellaneous Tasks

- Added beta release channels

- Updated build step java version


## [21.0.1] - 2021-11-18

### Bug Fixes

- Area unit conversion

- Wrong area unit checked

- Added custom dialog fragment to prevent crashes on inflation


### Code Refactoring

- Revised kotlin version string

- Reenabled debugging views bypass

- Corrected annotations and removed extra slashes in trnslations

- Added checker for starch factory count

- Updated in app database name


### Documentation

- Updated release notes


### Features

- Added checks for country to determine use case and area units

- Added kinyarwanda translations


### Miscellaneous Tasks

- Added env variable

- Added workflow

- Todo actions

- Added release status in google uploader


### Build

- Updated git workflow file

- Updated buils tep proces


## [21.0.0] - 2021-11-09

### Bug Fixes

- Currency object processing

- Added proper planting windows check

- Added proper room database annotation

- Added proper case switching fo swahili words

- Fixed ui tranlation strings for kiswahili

- Fixed null locate issue casuing interfaces to crash

- Merge conflict

- Updated repo names

- Translations

- Crashing maps


### Code Refactoring

- Enabled correct entry activity

- Downgraded kotlin versions

- Updated build steps in android.yml actions file

- Git hooks

- Removed extraneous semicolon

- Package renaming [**BREAKING**]

- Removed bpkp folder


### Documentation

- Added changelog genration

- Updated whats new notes

- Updated changelog

- Updated changelog file

- Updated changelog file

- Added release notes for swahili version

- **change-logs**: Updated change log file

- Updated changelog

- README update

- Updated release notes


### Features

- Dependencies updates

- Translation

- Harmonized packages

- Ghana support


### Miscellaneous Tasks

- Removed extra comments

- Addee cz quality checker

- Updated actions flow and branch with develop changes

- Build condition step revision

- Removed git quality check steps

- Added step for beta branch build

- Disabled on pull request build

- Added auto pr action to github CI

- Added title to beta release workflow

- Disble Pr draft flag in pr automater

- Enhanced build step to exclude tests

- Reenable github actions by renaming

- Removed bundlerelease step in unit test step

- Updated dependencies version

- Updated java version

- Updated env values

- **updated-dependencies**: Updated dependencies and build references

- Disabled goog play publisher step


### Styling

- Code style formatting


### Build

- Removed AndRatingBar

- Library updates

- Updated mapbox tokens


<!-- generated by git-cliff -->
