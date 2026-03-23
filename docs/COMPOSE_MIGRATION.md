# AKILIMO Mobile — Jetpack Compose Migration Guide

This document is the authoritative plan for migrating the entire AKILIMO Android app from
View-based XML + ViewBinding to Jetpack Compose. It covers architecture decisions, build
configuration, design-system bridging, phased migration order, and a screen-by-screen
conversion map.

---

## Table of Contents

1. [Migration Philosophy](#1-migration-philosophy)
2. [Key Decisions](#2-key-decisions)
3. [Build Configuration](#3-build-configuration)
4. [Design System Bridge](#4-design-system-bridge)
5. [Target Architecture](#5-target-architecture)
6. [Navigation](#6-navigation)
7. [Phased Migration Plan](#7-phased-migration-plan)
8. [Onboarding Wizard — Pattern Reference](#8-onboarding-wizard--pattern-reference)
9. [Screen-by-Screen Conversion Map](#9-screen-by-screen-conversion-map)
10. [Adapter → LazyLayout Equivalents](#10-adapter--lazylayout-equivalents)
11. [Testing Strategy](#11-testing-strategy)
12. [Common Pitfalls](#12-common-pitfalls)
13. [Library Removals at Completion](#13-library-removals-at-completion)

---

## 1. Migration Philosophy

**Full migration — no permanent hybrid UI.** Every Fragment and Activity is replaced with a
Jetpack Compose equivalent. No `ComposeView` bridges or `AndroidView` wrappers remain after
Phase 5 (except where a third-party View-only SDK, e.g. Mapbox, has no Compose alternative).

**Onboarding-first.** The onboarding wizard is implemented first and serves as the
architectural pattern reference for all subsequent screens. Getting the wizard right —
state management, validation, navigation, theming — means every screen that follows is
mechanical work.

**Single-Activity from day one.** `MainActivity` replaces `HomeStepperActivity` as the sole
entry point. A single `NavHost` replaces all `startActivity()` / `Intent` calls from the
first commit of Phase 1.

---

## 2. Key Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Migration scope | Full — all Fragments and Activities replaced | No long-term hybrid debt |
| Navigation | Pure Compose `NavHost` with `@Serializable` routes | Type-safe, no NavHostFragment, testable |
| Hybrid strategy | None — direct replacement per phase | ComposeView bridges introduce double maintenance |
| State container | ViewModel + `StateFlow<UiState>` | Consistent with existing ViewModels; survives config change |
| One-shot effects | `Channel<Effect>` collected in `LaunchedEffect` | Prevents re-delivery on recomposition |
| DI | Hilt (already integrated) — `hiltViewModel()` in composables | No change to injection setup |
| Starting screen | Onboarding wizard | Most complex flow; sets patterns for all others |
| Theme | `AkilimoTheme` wrapping `MaterialTheme` (Material 3) | Port existing XML tokens directly |

---

## 3. Build Configuration

All dependencies are managed through `gradle/libs.versions.toml`. Do not add raw Maven
coordinate strings to `build.gradle.kts` — always go through the version catalog.

### 3.1 Additions required in `gradle/libs.versions.toml`

The Compose BOM, `activity-compose`, and core UI/Material3 library aliases are
**already present** in the catalog. The following are missing and must be added before
Phase 0 begins.

```toml
[versions]
# Add these — currently missing:
navigationCompose      = "2.9.0"
hiltNavigationCompose  = "1.2.0"

[libraries]
# Add after the existing compose entries:
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose",  version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose   = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose",    version.ref = "lifecycleRuntimeKtx" }
androidx-navigation-compose          = { group = "androidx.navigation", name = "navigation-compose",          version.ref = "navigationCompose" }
hilt-navigation-compose              = { group = "androidx.hilt",       name = "hilt-navigation-compose",     version.ref = "hiltNavigationCompose" }

[plugins]
# Uncomment this line (currently commented out):
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

### 3.2 `app/build.gradle.kts` — plugin block

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)   // ← add (Phase 0)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}
```

### 3.3 `android {}` block

```kotlin
android {
    buildFeatures {
        compose     = true   // ← change from false
        viewBinding = true   // keep until Phase 5 cleanup
        buildConfig = true
    }
    // No composeOptions.kotlinCompilerExtensionVersion needed —
    // kotlin.plugin.compose manages it automatically.
}
```

### 3.4 `dependencies {}` block

```kotlin
dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

Keep `viewBinding = true` and all existing ViewBinding dependencies until Phase 5.
Every un-migrated screen still uses them.

---

## 4. Design System Bridge

The app already has a well-defined Material 3 colour palette in `values/colors.xml` and
`values/themes.xml`. These are ported directly into Compose so both the old and new screens
are visually identical during the transition.

### 4.1 `ui/theme/AkilimoColors.kt`

```kotlin
package com.akilimo.mobile.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val AkilimoPrimaryGreen        = Color(0xFF3D7600)
val AkilimoPrimaryContainer    = Color(0xFFBAEE82)
val AkilimoOnPrimary           = Color(0xFFFFFFFF)
val AkilimoOnPrimaryContainer  = Color(0xFF0D2100)
val AkilimoSecondaryOlive      = Color(0xFF586200)
val AkilimoSecondaryContainer  = Color(0xFFDCE87A)
val AkilimoOnSecondary         = Color(0xFFFFFFFF)
val AkilimoOnSecondaryContainer = Color(0xFF181D00)
val AkilimoTertiaryRed         = Color(0xFFB5162A)
val AkilimoTertiaryContainer   = Color(0xFFFFDADC)
val AkilimoOnTertiary          = Color(0xFFFFFFFF)
val AkilimoOnTertiaryContainer = Color(0xFF40000D)
val AkilimoBackground          = Color(0xFFF5FCE9)
val AkilimoSurface             = Color(0xFFF5FCE9)
val AkilimoSurfaceVariant      = Color(0xFFDCE6C8)
val AkilimoOnBackground        = Color(0xFF181D12)
val AkilimoOnSurface           = Color(0xFF181D12)
val AkilimoOnSurfaceVariant    = Color(0xFF424940)
val AkilimoOutline             = Color(0xFF72796A)
val AkilimoError               = Color(0xFFBA1A1A)
val AkilimoErrorContainer      = Color(0xFFFFDAD6)

// Dark variants
val AkilimoDarkPrimary               = Color(0xFF9DD768)
val AkilimoDarkOnPrimary             = Color(0xFF1C3A00)
val AkilimoDarkPrimaryContainer      = Color(0xFF2C5900)
val AkilimoDarkOnPrimaryContainer    = Color(0xFFBAEE82)
val AkilimoDarkSecondary             = Color(0xFFBFCA6A)
val AkilimoDarkOnSecondary           = Color(0xFF2D3300)
val AkilimoDarkSecondaryContainer    = Color(0xFF434A00)
val AkilimoDarkOnSecondaryContainer  = Color(0xFFDCE87A)
val AkilimoDarkTertiary              = Color(0xFFFFB3B4)
val AkilimoDarkOnTertiary            = Color(0xFF68000F)
val AkilimoDarkTertiaryContainer     = Color(0xFF92001E)
val AkilimoDarkOnTertiaryContainer   = Color(0xFFFFDADC)
val AkilimoDarkBackground            = Color(0xFF10140C)
val AkilimoDarkSurface               = Color(0xFF10140C)
val AkilimoDarkSurfaceVariant        = Color(0xFF424940)
val AkilimoDarkOnBackground          = Color(0xFFE1E8D4)
val AkilimoDarkOnSurface             = Color(0xFFE1E8D4)
val AkilimoDarkOnSurfaceVariant      = Color(0xFFC1C9B2)
val AkilimoDarkError                 = Color(0xFFFFB4AB)
val AkilimoDarkErrorContainer        = Color(0xFF93000A)

val LightColorScheme = lightColorScheme(
    primary              = AkilimoPrimaryGreen,
    onPrimary            = AkilimoOnPrimary,
    primaryContainer     = AkilimoPrimaryContainer,
    onPrimaryContainer   = AkilimoOnPrimaryContainer,
    secondary            = AkilimoSecondaryOlive,
    onSecondary          = AkilimoOnSecondary,
    secondaryContainer   = AkilimoSecondaryContainer,
    onSecondaryContainer = AkilimoOnSecondaryContainer,
    tertiary             = AkilimoTertiaryRed,
    onTertiary           = AkilimoOnTertiary,
    tertiaryContainer    = AkilimoTertiaryContainer,
    onTertiaryContainer  = AkilimoOnTertiaryContainer,
    background           = AkilimoBackground,
    onBackground         = AkilimoOnBackground,
    surface              = AkilimoSurface,
    onSurface            = AkilimoOnSurface,
    surfaceVariant       = AkilimoSurfaceVariant,
    onSurfaceVariant     = AkilimoOnSurfaceVariant,
    outline              = AkilimoOutline,
    error                = AkilimoError,
    errorContainer       = AkilimoErrorContainer,
)

val DarkColorScheme = darkColorScheme(
    primary              = AkilimoDarkPrimary,
    onPrimary            = AkilimoDarkOnPrimary,
    primaryContainer     = AkilimoDarkPrimaryContainer,
    onPrimaryContainer   = AkilimoDarkOnPrimaryContainer,
    secondary            = AkilimoDarkSecondary,
    onSecondary          = AkilimoDarkOnSecondary,
    secondaryContainer   = AkilimoDarkSecondaryContainer,
    onSecondaryContainer = AkilimoDarkOnSecondaryContainer,
    tertiary             = AkilimoDarkTertiary,
    onTertiary           = AkilimoDarkOnTertiary,
    tertiaryContainer    = AkilimoDarkTertiaryContainer,
    onTertiaryContainer  = AkilimoDarkOnTertiaryContainer,
    background           = AkilimoDarkBackground,
    onBackground         = AkilimoDarkOnBackground,
    surface              = AkilimoDarkSurface,
    onSurface            = AkilimoDarkOnSurface,
    surfaceVariant       = AkilimoDarkSurfaceVariant,
    onSurfaceVariant     = AkilimoDarkOnSurfaceVariant,
    error                = AkilimoDarkError,
    errorContainer       = AkilimoDarkErrorContainer,
)
```

### 4.2 `ui/theme/AkilimoTypography.kt`

```kotlin
val GoogleSans = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_medium,  FontWeight.Medium),
    Font(R.font.google_sans_bold,    FontWeight.Bold),
    Font(R.font.google_sans_italic,  FontWeight.Normal, FontStyle.Italic),
)

val AkilimoTypography = Typography(
    displayLarge   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal,  fontSize = 57.sp, lineHeight = 64.sp),
    headlineMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal,  fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium,  fontSize = 22.sp, lineHeight = 28.sp),
    bodyLarge      = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium,  fontSize = 11.sp, lineHeight = 16.sp),
)
```

### 4.3 `ui/theme/AkilimoShapes.kt`

```kotlin
val AkilimoShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // chips
    small      = RoundedCornerShape(8.dp),   // text fields
    medium     = RoundedCornerShape(12.dp),  // cards
    large      = RoundedCornerShape(24.dp),  // bottom sheets
    extraLarge = RoundedCornerShape(50),     // pill buttons
)
```

### 4.4 `ui/theme/AkilimoTheme.kt`

```kotlin
@Composable
fun AkilimoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography   = AkilimoTypography,
        shapes       = AkilimoShapes,
        content      = content,
    )
}
```

**Rule:** Never hardcode colours in composables. Always reference `MaterialTheme.colorScheme.*`.

---

## 5. Target Architecture

### 5.1 Layer diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                     PRESENTATION (Compose)                        │
│  MainActivity (@AndroidEntryPoint)                               │
│    └─ AkilimoNavHost (NavHost with @Serializable routes)         │
│         ├─ OnboardingScreen   → WelcomeStep, BioDataStep, …     │
│         ├─ RecommendationsScreen → FrScreen, BppScreen, …       │
│         └─ SettingsScreen                                         │
│  Per screen: @Composable fun + @HiltViewModel                    │
│  State contract: UiState / Event / Effect (see §5.2)             │
└──────────────────────────┬───────────────────────────────────────┘
                            │ StateFlow<UiState> / Flow<Effect>
┌──────────────────────────▼───────────────────────────────────────┐
│                     VIEWMODEL LAYER (Hilt)                        │
│  @HiltViewModel — one per screen or logical group                 │
│  _uiState: MutableStateFlow<UiState>                             │
│  _effect:  Channel<Effect> (one-shot side effects)               │
│  onEvent(Event) — single entry point for all user actions        │
└──────────────────────────┬───────────────────────────────────────┘
                            │ suspend / Flow (unchanged)
┌──────────────────────────▼───────────────────────────────────────┐
│                  REPOSITORY LAYER (unchanged)                     │
│  All 15+ repos remain; constructor-injected via Hilt             │
└──────────────────────────────────────────────────────────────────┘
```

### 5.2 Per-screen contract (UiState / Event / Effect)

Every screen defines three types:

```kotlin
// What the UI renders — must be a data class, always has defaults
data class FooUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val nameError: String? = null,
)

// What the user does — sealed interface, one object per action
sealed interface FooEvent {
    data class NameChanged(val value: String) : FooEvent
    data object SaveClicked : FooEvent
}

// One-shot side effects (navigate, show snackbar) — NOT in StateFlow
// Delivered via Channel so they survive exactly once
sealed interface FooEffect {
    data object NavigateBack : FooEffect
    data class ShowError(val message: String) : FooEffect
}
```

### 5.3 ViewModel shape

```kotlin
@HiltViewModel
class FooViewModel @Inject constructor(
    private val repo: FooRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow(FooUiState())
    val uiState: StateFlow<FooUiState> = _uiState.asStateFlow()

    // Channel — delivers each effect exactly once regardless of recompositions
    private val _effect = Channel<FooEffect>(Channel.BUFFERED)
    val effect: Flow<FooEffect> = _effect.receiveAsFlow()

    fun onEvent(event: FooEvent) {
        when (event) {
            is FooEvent.NameChanged -> _uiState.update { it.copy(name = event.value) }
            FooEvent.SaveClicked    -> save()
        }
    }

    private fun save() = viewModelScope.launch {
        // validate, persist
        _effect.send(FooEffect.NavigateBack)
    }
}
```

### 5.4 Screen shape

```kotlin
@Composable
fun FooScreen(
    onNavigateBack: () -> Unit,
    viewModel: FooViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Effects are collected once — LaunchedEffect(Unit) prevents re-collection on recomposition
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FooEffect.NavigateBack        -> onNavigateBack()
                is FooEffect.ShowError        -> { /* show snackbar */ }
            }
        }
    }

    FooContent(state = state, onEvent = viewModel::onEvent)
}

// Stateless content composable — easy to preview and test
@Composable
private fun FooContent(state: FooUiState, onEvent: (FooEvent) -> Unit) {
    // UI layout
}

@Preview
@Composable
private fun FooContentPreview() {
    AkilimoTheme { FooContent(state = FooUiState(), onEvent = {}) }
}
```

**Why separate `FooScreen` and `FooContent`:**
- `FooScreen` owns ViewModel and effect collection — not previewable
- `FooContent` is stateless and always previewable with arbitrary state

---

## 6. Navigation

### 6.1 Route definition

Use `@Serializable` objects and data classes (Kotlin Serialization + Navigation 2.8+):

```kotlin
// navigation/Route.kt
sealed interface Route {
    @Serializable data object Onboarding       : Route
    @Serializable data object Recommendations  : Route
    @Serializable data object Bpp              : Route
    @Serializable data object Fr               : Route
    @Serializable data object Sph              : Route
    @Serializable data object IcMaize          : Route
    @Serializable data object IcSweetPotato    : Route
    @Serializable data class  ManualTillageCost(val userId: Int) : Route
    @Serializable data class  TractorAccess(val userId: Int)     : Route
    @Serializable data class  WeedControlCosts(val userId: Int)  : Route
    @Serializable data object Dates            : Route
    @Serializable data object CassavaMarket    : Route
    @Serializable data object CassavaYield     : Route
    @Serializable data object InvestmentAmount : Route
    @Serializable data object Fertilizers      : Route
    @Serializable data object Settings         : Route
    @Serializable data object LocationPicker   : Route
}
```

### 6.2 NavHost

```kotlin
// navigation/AkilimoNavHost.kt
@Composable
fun AkilimoNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController, startDestination = Route.Onboarding) {

        composable<Route.Onboarding> {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Route.Recommendations) {
                        popUpTo<Route.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<Route.Recommendations> {
            RecommendationsScreen(
                onNavigate = { navController.navigate(it) },
                onBack = navController::popBackStack
            )
        }

        composable<Route.ManualTillageCost> { back ->
            val route = back.toRoute<Route.ManualTillageCost>()
            ManualTillageCostScreen(userId = route.userId, onBack = navController::popBackStack)
        }

        // ... all other destinations
    }
}
```

### 6.3 Navigation rules

- Pass only **IDs and primitives** in routes — never full entity objects
- Load entity data in the destination ViewModel using the ID from the route
- Use `popUpTo<Route.Onboarding> { inclusive = true }` only when intentionally clearing the back stack (e.g. after completing onboarding)
- Call `navController.navigate()` only from screen composables or `LaunchedEffect` — never from inside a ViewModel
- `navigateSingleTop = true` for tab-style top-level destinations

---

## 7. Phased Migration Plan

### Phase 0 — Enable Compose ✅ Complete

No user-visible changes.

| Task | Status |
|---|---|
| Uncomment `kotlin-compose` plugin in `libs.versions.toml` | ✅ |
| Add missing catalog entries (§3.1) | ✅ |
| Set `compose = true` and add Compose dependencies in `app/build.gradle.kts` | ✅ |
| Create `AkilimoTheme`, `AkilimoColors`, `AkilimoTypography`, `AkilimoShapes` | ✅ |
| Verify build passes — no screens changed yet | ✅ |

Branch: `feature/compose-foundation`

---

### Phase 1 — Navigation Shell ✅ Complete

`MainActivity` is the active Compose entry point. `AkilimoNavHost` wires all Phase 3 screens.

| Task | Status |
|---|---|
| Create `MainActivity.kt` with `setContent { AkilimoTheme { AkilimoNavHost() } }` | ✅ |
| Create `navigation/Route.kt` with all routes declared upfront | ✅ |
| Create `navigation/AkilimoNavHost.kt` | ✅ |
| Update `AndroidManifest.xml` — `MainActivity` as launcher, keep all legacy Activities | ✅ |
| Build and verify app launches | ✅ |

Branch: `feature/compose-foundation` (same branch as Phase 0)

---

### Phase 2 — Onboarding Wizard (3–5 days)

**Pattern-setting phase.** The architecture established here is the template for every
subsequent screen. See §8 for full implementation detail.

| Task |
|---|
| Create reusable components: `AkilimoTextField`, `AkilimoDropdown`, `WizardBottomBar`, `ExitConfirmDialog` |
| Adapt `OnboardingViewModel` — full UiState covering all steps, Event, Effect |
| Implement `OnboardingScreen` container with `AnimatedContent` step transitions |
| Implement each step composable: `WelcomeStep`, `DisclaimerStep`, `TermsStep`, `BioDataStep`, `CountryStep`, `LocationStep`, `AreaUnitStep`, `PlantingDateStep`, `TillageStep`, `InvestmentPrefStep`, `SummaryStep` |
| Wire `OnboardingScreen` into `AkilimoNavHost` |
| Delete `HomeStepperActivity`, `WizardAdapter`, all wizard Fragment classes, `BaseStepFragment` |
| Remove wizard XML layouts |

Branch: `feature/compose-onboarding`

---

### Phase 3 — Recommendations & Use-Case Screens ✅ Complete

All use-case and recommendations screens are live in Compose. Shared components (`BackTopAppBar`, `SaveBottomBar`, `ScrollableFormColumn`, `NavExtensions.completeTask`) extracted to `ui/components/compose/` (see `docs/COMPOSE_COMPONENT_REFACTOR.md`).

| Screen | Status |
|---|---|
| `RecommendationsScreen`, `UseCaseScreen` | ✅ |
| `ManualTillageCostScreen`, `TractorAccessScreen`, `WeedControlCostsScreen` | ✅ |
| `CassavaMarketScreen`, `CassavaYieldScreen`, `DatesScreen`, `InvestmentAmountScreen` | ✅ |
| `FertilizerScreen` (DEFAULT / CIM / CIS flows) | ✅ |
| `MaizeMarketScreen`, `MaizePerformanceScreen`, `SweetPotatoMarketScreen` | ✅ |
| `GetRecommendationScreen` | ✅ |

Branch: `feature/compose-recommendations`

---

### Phase 4 — Settings & Misc ✅ Complete

| Task | Status |
|---|---|
| `UserSettingsScreen` — settings form | ✅ |
| `LocationPickerScreen` — `AndroidView` wrapper for Mapbox | ✅ |

Branch: `feature/compose-settings`

---

### Phase 5 — Final Cleanup (1–2 days)

| Task |
|---|
| `viewBinding = false` in `build.gradle.kts` |
| Delete all `app/src/main/res/layout/*.xml` files |
| Delete `base/BaseFragment.kt`, `base/BaseStepFragment.kt` |
| Simplify `base/BaseActivity.kt` to a thin `@AndroidEntryPoint` host (only lifecycle/permission helpers remain) |
| Remove View-only libraries: `AppLocale`/`Reword`/`ViewPump`, `hbb20:ccp`, `StepperLayout` (already replaced in Phase 2) |
| Remove `androidx.navigation:navigation-fragment` and `navigation-ui-ktx` (replaced by `navigation-compose`) |
| Remove all XML navigation graphs (`nav_graph.xml`, `nav_recommendations.xml`) |
| Final build verification — no ViewBinding generated classes, no XML layouts |

Branch: `feature/compose-cleanup`

---

### Branch Strategy

```
develop
  └── feature/compose-foundation       ← Phase 0 + 1: Compose enabled, theme, NavHost
        └── feature/compose-onboarding ← Phase 2: wizard (merge before starting Phase 3)
              └── feature/compose-recs ← Phase 3: recommendations
                    └── feature/compose-settings  ← Phase 4: settings
                          └── feature/compose-cleanup ← Phase 5: delete all legacy
```

Merge each branch to `develop` before starting the next phase. This keeps each PR
reviewable in isolation and the app always in a buildable state.

---

## 8. Onboarding Wizard — Pattern Reference

This section defines the canonical implementation that all other screens follow.

### 8.1 UiState

The wizard ViewModel owns ALL step data in a single flat state object. Sub-states for each
step are nested data classes:

```kotlin
data class OnboardingUiState(
    val currentStep: Int = 0,
    val steps: List<OnboardingStep> = emptyList(),  // driven by appSettings flags
    val bioData: BioDataState = BioDataState(),
    val country: CountryState = CountryState(),
    val location: LocationState = LocationState(),
    val plantingDate: PlantingDateState = PlantingDateState(),
    val tillageOps: TillageOpsState = TillageOpsState(),
    val investmentPref: InvestmentPrefState = InvestmentPrefState(),
    val isLoading: Boolean = false,
    val stepError: String? = null,
)

data class BioDataState(
    val firstName: String = "",
    val lastName: String = "",
    val farmSize: String = "",
    val firstNameError: String? = null,
    val farmSizeError: String? = null,
)
// ... similar for CountryState, LocationState, etc.
```

### 8.2 Conditional steps (replaces `buildStepList()`)

Drive the step list from DataStore flows so it reacts to state changes:

```kotlin
val steps: StateFlow<List<OnboardingStep>> = combine(
    appSettings.disclaimerRead,
    appSettings.termsAccepted,
    appSettings.rememberAreaUnit,
) { disclaimerRead, termsAccepted, rememberAreaUnit ->
    buildList {
        add(OnboardingStep.WELCOME)
        if (!disclaimerRead)   add(OnboardingStep.DISCLAIMER)
        if (!termsAccepted)    add(OnboardingStep.TERMS)
        add(OnboardingStep.BIO_DATA)
        add(OnboardingStep.COUNTRY)
        add(OnboardingStep.LOCATION)
        if (!rememberAreaUnit) add(OnboardingStep.AREA_UNIT)
        add(OnboardingStep.PLANTING_DATE)
        add(OnboardingStep.TILLAGE)
        add(OnboardingStep.INVESTMENT_PREF)
        add(OnboardingStep.SUMMARY)
    }
}.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

### 8.3 Validation (replaces `verifyStep()`)

```kotlin
private fun validateCurrentStep(): String? {
    val state = _uiState.value
    return when (state.steps.getOrNull(state.currentStep)) {
        OnboardingStep.BIO_DATA -> validateBioData(state.bioData)
        OnboardingStep.COUNTRY  -> if (state.country.selected == null) "Select a country" else null
        else                    -> null
    }
}

private fun validateBioData(s: BioDataState): String? = when {
    s.firstName.isBlank() -> "First name is required"
    s.farmSize.toDoubleOrNull() == null -> "Enter a valid farm size"
    else -> null
}
```

### 8.4 Screen container

```kotlin
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingEffect.Finish -> onFinish()
            }
        }
    }

    // Handle system back: go to previous step or show exit dialog
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = true) {
        if (state.currentStep > 0) viewModel.onEvent(OnboardingEvent.Back)
        else showExitDialog = true
    }

    if (showExitDialog) {
        ExitConfirmDialog(
            onConfirm = { exitProcess(0) },
            onDismiss = { showExitDialog = false }
        )
    }

    Scaffold(
        bottomBar = {
            WizardBottomBar(
                currentStep  = state.currentStep,
                stepCount    = state.steps.size,
                onBack       = { viewModel.onEvent(OnboardingEvent.Back) },
                onNext       = { viewModel.onEvent(OnboardingEvent.Next) },
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally { if (forward) it else -it } + fadeIn()) togetherWith
                (slideOutHorizontally { if (forward) -it else it } + fadeOut())
            },
            modifier = Modifier.padding(padding),
            label = "WizardStep"
        ) { stepIndex ->
            when (state.steps.getOrNull(stepIndex)) {
                OnboardingStep.WELCOME       -> WelcomeStep()
                OnboardingStep.DISCLAIMER    -> DisclaimerStep(onEvent = viewModel::onEvent)
                OnboardingStep.TERMS         -> TermsStep(onEvent = viewModel::onEvent)
                OnboardingStep.BIO_DATA      -> BioDataStep(state.bioData, viewModel::onEvent)
                OnboardingStep.COUNTRY       -> CountryStep(state.country, viewModel::onEvent)
                OnboardingStep.LOCATION      -> LocationStep(state.location, viewModel::onEvent)
                OnboardingStep.AREA_UNIT     -> AreaUnitStep(state.areaUnit, viewModel::onEvent)
                OnboardingStep.PLANTING_DATE -> PlantingDateStep(state.plantingDate, viewModel::onEvent)
                OnboardingStep.TILLAGE       -> TillageStep(state.tillageOps, viewModel::onEvent)
                OnboardingStep.INVESTMENT_PREF -> InvestmentPrefStep(state.investmentPref, viewModel::onEvent)
                OnboardingStep.SUMMARY       -> SummaryStep(state, viewModel::onEvent)
                else                         -> {}
            }
        }
    }
}
```

### 8.5 Individual step

Steps are **stateless composables** — they receive state and dispatch events. They never
call `hiltViewModel()` or hold local mutable state (use `rememberSaveable` only for
transient UI state like keyboard focus, not for form values):

```kotlin
@Composable
fun BioDataStep(
    state: BioDataState,
    onEvent: (OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AkilimoTextField(
            value       = state.firstName,
            onValueChange = { onEvent(OnboardingEvent.BioDataChanged(BioDataField.FIRST_NAME, it)) },
            label       = stringResource(R.string.lbl_first_name),
            error       = state.firstNameError,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        AkilimoTextField(
            value       = state.farmSize,
            onValueChange = { onEvent(OnboardingEvent.BioDataChanged(BioDataField.FARM_SIZE, it)) },
            label       = stringResource(R.string.lbl_farm_size),
            error       = state.farmSizeError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction    = ImeAction.Done
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BioDataStepPreview() {
    AkilimoTheme {
        BioDataStep(
            state   = BioDataState(firstNameError = "Required"),
            onEvent = {}
        )
    }
}
```

---

## 9. Screen-by-Screen Conversion Map

| Current class | Compose screen | Phase |
|---|---|---|
| `HomeStepperActivity` + 11 wizard Fragments | `OnboardingScreen` + `*Step` composables | 2 |
| `RecommendationsFragment` | `RecommendationsScreen` | 3 |
| `FrFragment` + `FrActivity` | `FrScreen` | 3 |
| `BppFragment` + `BppActivity` | `BppScreen` | 3 |
| `SphFragment` + `SphActivity` | `SphScreen` | 3 |
| `IcMaizeFragment` + `IcMaizeActivity` | `IcMaizeScreen` | 3 |
| `IcSweetPotatoFragment` + `IcSweetPotatoActivity` | `IcSweetPotatoScreen` | 3 |
| `ManualTillageCostFragment` + `ManualTillageCostActivity` | `ManualTillageCostScreen` | 3 |
| `TractorAccessFragment` + `TractorAccessActivity` | `TractorAccessScreen` | 3 |
| `WeedControlCostsFragment` + `WeedControlCostsActivity` | `WeedControlCostsScreen` | 3 |
| `DatesFragment` + `DatesActivity` | `DatesScreen` | 3 |
| `CassavaMarketFragment` + `CassavaMarketActivity` | `CassavaMarketScreen` | 3 |
| `CassavaYieldFragment` + `CassavaYieldActivity` | `CassavaYieldScreen` | 3 |
| `InvestmentAmountFragment` + `InvestmentAmountActivity` | `InvestmentAmountScreen` | 3 |
| `FertilizersFragment` + `FertilizersActivity` | `FertilizersScreen` | 3 |
| `InterCropFertilizersFragment` + activity | `InterCropFertilizersScreen` | 3 |
| `SweetPotatoInterCropFertilizersFragment` + activity | `SweetPotatoInterCropFertilizersScreen` | 3 |
| `GetRecommendationFragment` + `GetRecommendationActivity` | `GetRecommendationScreen` | 3 |
| `MaizeMarketActivity` | `MaizeMarketScreen` | 3 |
| `MaizePerformanceActivity` | `MaizePerformanceScreen` | 3 |
| `SweetPotatoMarketActivity` | `SweetPotatoMarketScreen` | 3 |
| `WeedManagementActivity` | `WeedManagementScreen` | 3 |
| `UserSettingsActivity` | `UserSettingsScreen` | 4 |
| `LocationPickerActivity` | `LocationPickerScreen` (AndroidView for Mapbox) | 4 |
| `MainActivity` (splash) | integrated into `AkilimoNavHost` start logic | 1 |

---

## 10. Adapter → LazyLayout Equivalents

| Adapter | Compose equivalent |
|---|---|
| `CassavaYieldAdapter` (2-col grid, tap select) | `LazyVerticalGrid(Fixed(2))` + `CassavaYieldCard` |
| `MaizePerformanceAdapter` (2-col grid, tap select) | `LazyVerticalGrid(Fixed(2))` + `MaizePerformanceCard` |
| `FertilizerAdapter` (toggle + checkbox list) | `LazyColumn` + `FertilizerItem` |
| `CassavaMarketPriceAdapter` (single-select list) | `LazyColumn` + `MarketPriceItem` |
| `InvestmentAmountAdapter` (grid) | `LazyVerticalGrid` + `InvestmentAmountCard` |
| `RecommendationAdapter` (expandable list) | `LazyColumn` + `RecommendationCard(expanded, onToggle)` |
| `BaseSpinnerAdapter` / `ValueOptionAdapter` (dropdown) | `ExposedDropdownMenuBox` + `DropdownMenuItem` |

**Selection state for image-grid screens:**

```kotlin
// In ViewModel
val items: StateFlow<List<CassavaYield>> = combine(repo.items, _selectedId) { list, id ->
    list.map { it.copy(isSelected = it.id == id) }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

// In composable
Card(
    onClick = { onEvent(CassavaYieldEvent.Select(item.id)) },
    border = if (item.isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
)
```

---

## 11. Testing Strategy

### Unit tests (ViewModels)

Test `UiState` transitions and Effect emission using `kotlinx-coroutines-test` and Turbine:

```kotlin
@Test
fun `next on last step emits Finish effect`() = runTest {
    val vm = OnboardingViewModel(/* fakes */)
    vm.effect.test {
        vm.onEvent(OnboardingEvent.Next) // at last step
        assertEquals(OnboardingEffect.Finish, awaitItem())
    }
}
```

### Composable tests

`@Preview` for every screen and component. Use Paparazzi for screenshot regression without
a device.

### Instrumented / integration tests

`ComposeTestRule` + Hilt test runner per screen:

```kotlin
@HiltAndroidTest
class BioDataStepTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun emptyFirstName_showsError() {
        composeRule.onNodeWithText("Next").performClick()
        composeRule.onNodeWithText("Required").assertIsDisplayed()
    }
}
```

---

## 12. Common Pitfalls

| Pitfall | Correct approach |
|---|---|
| `remember` for form values | Use `rememberSaveable` — `remember` is lost on rotation |
| Navigation state in `UiState` (e.g. `navigateNext: Boolean`) | Use `Channel<Effect>` — StateFlow replays on recomposition, re-triggering navigation |
| One ViewModel per wizard step | One shared ViewModel for the whole wizard scoped to `OnboardingScreen` |
| `LaunchedEffect(state.someValue)` for effects | `LaunchedEffect(Unit)` collecting from `viewModel.effect` — stable key, no re-triggers |
| Calling `hiltViewModel()` in a step composable | Pass state and callbacks as parameters; only call `hiltViewModel()` in screen-level composables |
| Blocking the main thread | All suspend/IO work in ViewModel `viewModelScope.launch` — never in composables |
| Skipping `@Preview` | Every content composable must have a preview — they catch bugs at design time |
| Deep object graphs in routes | Pass only primitive IDs in routes; load full objects in the destination ViewModel |

---

## 13. Library Removals at Completion

Removed during Phase 5 cleanup:

| Library | Replacement | Notes |
|---|---|---|
| `androidx.navigation:navigation-fragment` + `navigation-ui-ktx` | `navigation-compose` | Already replaced in Phase 1 |
| `dev.b3nedikt.app_locale` + `reword` + `viewpump` | `AppCompatDelegate.setApplicationLocales()` + Compose `stringResource` | AppLocale string replacement no longer needed |
| `com.jakewharton.processphoenix` | — | ✅ Already removed |
| `hbb20:ccp` (Country Code Picker) | `ExposedDropdownMenuBox` | Replace in Phase 2 (CountryStep) |
| `androidx.viewbinding` | — | Remove in Phase 5 |
| `com.stepstone.stepper` | `AnimatedContent` wizard | ✅ Already replaced by ViewPager2 (interim); remove in Phase 2 |

**Do not remove:** `retrofit`, `okhttp`, `room`, `hilt`, `moshi`, `firebase`, `mapbox`,
`timber`, `sentry`, `workmanager` — all survive the Compose migration unchanged.
