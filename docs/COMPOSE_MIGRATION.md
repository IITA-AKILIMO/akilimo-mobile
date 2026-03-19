# AKILIMO Mobile — Jetpack Compose Migration Guide

This document is the authoritative plan for migrating the entire AKILIMO Android app from
View-based XML + ViewBinding to Jetpack Compose. It defines prerequisites, a phased screen
migration order, interoperability patterns, design-system bridging, and a screen-by-screen
conversion map.

---

## Table of Contents

1. [Migration Philosophy](#1-migration-philosophy)
2. [Prerequisites](#2-prerequisites)
3. [Build Configuration](#3-build-configuration)
4. [Design System Bridge](#4-design-system-bridge)
5. [Architecture in the Compose World](#5-architecture-in-the-compose-world)
6. [Interoperability Patterns](#6-interoperability-patterns)
7. [Phased Migration Plan](#7-phased-migration-plan)
8. [Screen-by-Screen Conversion Map](#8-screen-by-screen-conversion-map)
9. [Adapter → LazyLayout Equivalents](#9-adapter--lazylayout-equivalents)
10. [Stepper Replacement Strategy](#10-stepper-replacement-strategy)
11. [Testing Strategy](#11-testing-strategy)
12. [Library Removals at Completion](#12-library-removals-at-completion)

---

## 1. Migration Philosophy

**Strangler-fig migration**: never rewrite from scratch. Replace one screen at a time, keeping
the app releasable at every commit. Each screen migrates as a self-contained unit:
`Activity` → `ComposeActivity` (still extending `BaseActivity`) → eventually a `NavGraph`
destination.

**Rule of thumb for migration order:**
1. No ViewModel, no state (display-only) screens first.
2. Screens that already have a ViewModel when we add them.
3. Complex stateful flows (stepper) last — these need NavGraph and SavedStateHandle ready.

---

## 2. Prerequisites

These must be completed **before** any screen migration begins. They are captured in the
ROADMAP as items 3–14.

| # | Prerequisite | Why Compose needs it | ROADMAP ref |
|---|---|---|---|
| P1 | Remove `allowMainThreadQueries()` | Compose LaunchedEffect + StateFlow collectors will surface ANRs that were previously hidden | #3 |
| P2 | Move API keys to `BuildConfig` | R8 minification (P3) strips class names; keys embedded in strings survive but need proper BuildConfig pattern first | #4 |
| P3 | Enable R8 minification | Compose compiler generates significant bytecode; minification is required for release APK size | #5 |
| P4 | Introduce ViewModels | Compose `collectAsStateWithLifecycle()` needs a ViewModel as the state holder; all screen state moves there | #7 |
| P5 | Proper Room migrations | Schema changes during migration must not wipe user data | #8 |
| P6 | **Introduce Hilt** ✅ Done | `hiltViewModel()` in composables; `@HiltAndroidApp`, `@AndroidEntryPoint` on host activities | #9 |
| P7 | Consolidate language + all settings to DataStore ✅ Done | `rememberUpdatedState` needs a single reactive source; dual SharedPrefs causes recomposition race | #10 |
| P8 | Jetpack NavGraph | `NavHost` + `composable { }` destinations replace `startActivity()` calls | #13 |

P6 (Hilt) is complete. Do not skip P8 (NavGraph). Compose screens without NavGraph require manual
`startActivity()` calls from composables, which creates navigation debt.

---

## 3. Build Configuration

All dependencies are managed through `gradle/libs.versions.toml`. Do not add raw Maven
coordinate strings to `build.gradle.kts` — always go through the version catalog.

### 3.1 Additions required in `gradle/libs.versions.toml`

The Compose BOM, `activity-compose`, and all core UI/Material3 compose library aliases are
**already present** in the catalog. The following entries are missing and must be added before
Phase 1 begins.

#### `[versions]` — current state and what still needs adding

Hilt is already in the catalog at version `"2.57.1"`. The Compose BOM is already present at `"2025.10.00"`.

```toml
# Still missing — add these:
navigationCompose      = "2.8.5"   # navigation-compose (separate from view-based navigation = "2.7.5")
hiltNavigationCompose  = "1.2.0"
```

#### `[libraries]` — add after the existing `androidx-compose-material3` line

```toml
# Compose extras (BOM manages versions for ui/material3 entries; explicit for the rest)
androidx-compose-material3-window    = { group = "androidx.compose.material3",   name = "material3-window-size-class" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle",            name = "lifecycle-viewmodel-compose",  version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-runtime-compose   = { group = "androidx.lifecycle",            name = "lifecycle-runtime-compose",    version.ref = "lifecycleRuntimeKtx" }
# navigation-compose and hilt-navigation-compose are NOT yet in the catalog — add before Phase 1:
androidx-navigation-compose          = { group = "androidx.navigation",           name = "navigation-compose",           version.ref = "navigationCompose" }
hilt-navigation-compose              = { group = "androidx.hilt",                 name = "hilt-navigation-compose",      version.ref = "hiltNavigationCompose" }
```

> Note: `hilt-android` and `hilt-compiler` are already in the catalog (used by the current `kapt`-based Hilt integration).

#### `[plugins]` — `kotlin-compose` still needs to be uncommented; `hilt` is already present

```toml
# kotlin-compose is still commented out in the catalog — uncomment before Phase 1:
#kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
# change to:
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

# hilt plugin is already in the catalog as:
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
# and is applied in app/build.gradle.kts as:
# alias(libs.plugins.hilt)
```

#### `[bundles]` — optional convenience bundle (add alongside `androidx-navigation`)

```toml
compose-core = [
    "androidx-compose-ui",
    "androidx-compose-ui-graphics",
    "androidx-compose-ui-tooling-preview",
    "androidx-compose-material3",
    "androidx-activity-compose",
    "androidx-lifecycle-viewmodel-compose",
    "androidx-lifecycle-runtime-compose",
]
```

---

### 3.2 `app/build.gradle.kts` — plugin block

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)   // ← add when catalog entry is uncommented (Phase 1)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)             // ← already active (was libs.plugins.hilt.android in old docs; correct alias is libs.plugins.hilt)
    // … existing plugins unchanged
}
```

---

### 3.3 `android {}` block — enable Compose

```kotlin
android {
    buildFeatures {
        compose      = true
        viewBinding  = true   // keep until Phase 5 migration complete
        buildConfig  = true
    }
    // No composeOptions.kotlinCompilerExtensionVersion needed —
    // the kotlin.plugin.compose plugin (3.1 Build Configuration) manages it automatically.
}
```

---

### 3.4 `dependencies {}` block — using catalog aliases

```kotlin
dependencies {
    // ── Compose BOM (already in catalog, already used for activity-compose) ──
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // ── Core Compose UI (versions managed by BOM) ────────────────────────────
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window)      // ← new catalog entry

    // ── Activity + Lifecycle integration (already in catalog) ────────────────
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)   // ← new catalog entry
    implementation(libs.androidx.lifecycle.runtime.compose)     // ← new catalog entry

    // ── Navigation Compose ────────────────────────────────────────────────────
    implementation(libs.androidx.navigation.compose)            // ← new catalog entry

    // ── Hilt (already integrated — prerequisite #9 ✅ Done) ──────────────────
    implementation(libs.hilt.android)                           // already in build.gradle.kts
    kapt(libs.hilt.compiler)                                    // uses kapt (not ksp) for Hilt compiler
    implementation(libs.hilt.navigation.compose)                // ← new catalog entry (add before Phase 1)

    // ── Debug / test ─────────────────────────────────────────────────────────
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

Entries marked **← new catalog entry** require the `[libraries]` additions from §3.1 first.
All others are already present in `gradle/libs.versions.toml`.

---

### 3.5 Keep `viewBinding = true` until Phase 5

Do **not** disable ViewBinding mid-migration. Every un-migrated activity still uses it.
Remove it — along with all remaining XML layout references — in the final cleanup commit
of Phase 5.

---

## 4. Design System Bridge

The app already has a well-defined Material3 colour palette (`values/colors.xml`,
`values/themes.xml`). Bridge it directly into Compose so the visual identity is identical on
both the old and new screens during the transition.

### 4.1 Create `ui/theme/AkilimoColors.kt`

```kotlin
package com.akilimo.mobile.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Brand palette (mirrors values/colors.xml) ──────────────────────
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
val AkilimoOutlineVariant      = Color(0xFFC1C9B2)
val AkilimoError               = Color(0xFFBA1A1A)
val AkilimoErrorContainer      = Color(0xFFFFDAD6)

// ── Dark variants ───────────────────────────────────────────────────
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
    outlineVariant       = AkilimoOutlineVariant,
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

### 4.2 Create `ui/theme/AkilimoTypography.kt`

```kotlin
package com.akilimo.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.akilimo.mobile.R

val GoogleSans = FontFamily(
    Font(R.font.google_sans_regular, FontWeight.Normal),
    Font(R.font.google_sans_medium,  FontWeight.Medium),
    Font(R.font.google_sans_bold,    FontWeight.Bold),
    Font(R.font.google_sans_italic,  FontWeight.Normal, FontStyle.Italic),
)

val AkilimoTypography = Typography(
    displayLarge  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
```

### 4.3 Create `ui/theme/AkilimoShapes.kt`

```kotlin
package com.akilimo.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AkilimoShapes = Shapes(
    // extraSmall — chips, small badges
    extraSmall = RoundedCornerShape(4.dp),
    // small — text fields
    small      = RoundedCornerShape(8.dp),
    // medium — cards (12dp matches ShapeAppearance.Akilimo.Card)
    medium     = RoundedCornerShape(12.dp),
    // large — bottom sheets (24dp matches ShapeAppearance.Akilimo.Large)
    large      = RoundedCornerShape(24.dp),
    // extraLarge — pill buttons (50% = full rounding)
    extraLarge = RoundedCornerShape(50),
)
```

### 4.4 Create `ui/theme/AkilimoTheme.kt`

```kotlin
package com.akilimo.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

---

## 5. Architecture in the Compose World

### 5.1 Target state per layer

```
┌─────────────────────────────────────────────────────────────────────┐
│                       PRESENTATION (Compose)                         │
│  NavHost (AppNavGraph.kt)                                            │
│    ├─ OnboardingGraph  →  WelcomeScreen, BioDataScreen, …           │
│    ├─ RecommendationGraph → RecommendationUseCaseScreen, …          │
│    └─ SettingsGraph    →  UserSettingsScreen                         │
│  Each screen: @Composable fun + @HiltViewModel                       │
│  State: UiState data class + StateFlow in ViewModel                  │
└──────────────────────────┬──────────────────────────────────────────┘
                            │ StateFlow / SharedFlow
┌──────────────────────────▼──────────────────────────────────────────┐
│                          VIEWMODEL LAYER                             │
│  @HiltViewModel — one per screen or logical group                    │
│  Holds: UiState (data), UiEvent (one-shot), UserInput (form fields)  │
│  Exposes: StateFlow<ScreenState>, SharedFlow<NavigationEvent>        │
└──────────────────────────┬──────────────────────────────────────────┘
                            │ suspend / Flow
┌──────────────────────────▼──────────────────────────────────────────┐
│                       REPOSITORY LAYER (unchanged)                   │
│  All repos remain — Hilt provides them via constructor injection     │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 ViewModel state pattern (use consistently)

```kotlin
// ── UiState ───────────────────────────────────────────────────────
data class UserSettingsUiState(
    val isLoading: Boolean = false,
    val firstName: String = "",
    val languageCode: String = "en-US",
    val darkMode: Boolean = false,
    // … other fields
)

// ── ViewModel ─────────────────────────────────────────────────────
@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepo,
    private val appSettings: AppSettingsDataStore,   // SessionManager is deleted; inject DataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSettingsUiState())
    val uiState: StateFlow<UserSettingsUiState> = _uiState.asStateFlow()

    init { loadPreferences() }

    private fun loadPreferences() {
        viewModelScope.launch {
            val prefs = prefsRepo.getOrDefault()
            _uiState.update { it.copy(firstName = prefs.firstName ?: "", …) }
        }
    }

    fun onFirstNameChange(value: String) =
        _uiState.update { it.copy(firstName = value) }

    fun save() { /* validate → persist → emit navigation event */ }
}

// ── Screen ────────────────────────────────────────────────────────
@Composable
fun UserSettingsScreen(
    viewModel: UserSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // … compose UI
}
```

### 5.3 Navigation events (one-shot)

```kotlin
// In ViewModel
private val _navEvents = MutableSharedFlow<NavEvent>()
val navEvents = _navEvents.asSharedFlow()

sealed interface NavEvent {
    data object NavigateBack : NavEvent
    data class NavigateTo(val route: String) : NavEvent
}

// In Screen
LaunchedEffect(Unit) {
    viewModel.navEvents.collect { event ->
        when (event) {
            NavEvent.NavigateBack -> onNavigateBack()
            is NavEvent.NavigateTo -> navController.navigate(event.route)
        }
    }
}
```

---

## 6. Interoperability Patterns

During the transition, View-based and Compose-based code must coexist.

### 6.1 Embed a Compose screen inside an existing Activity (bridge host)

Use this pattern for Phase 2 screens before NavGraph is ready.

```kotlin
// In a migrated activity that still extends BaseActivity
class UserSettingsActivity : BaseActivity<ActivityUserSettingsBinding>() {

    override fun inflateBinding() = ActivityUserSettingsBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        // Replace the entire content with a ComposeView
        binding.composeContainer.setContent {
            AkilimoTheme {
                UserSettingsScreen(onNavigateBack = { finish() })
            }
        }
    }
}
```

`activity_user_settings.xml` reduces to a single `ComposeView`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.compose.ui.platform.ComposeView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/compose_container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### 6.2 Embed a View inside Compose (for complex Views not yet migrated)

```kotlin
@Composable
fun MapboxMapView(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context -> MapboxMapView(context) },
        modifier = modifier,
        update = { view -> /* update map state */ }
    )
}
```

Use this for `LocationPickerActivity`'s Mapbox map during Phase 3.

### 6.3 CompositionLocal bridge — Hilt is now active, use @HiltViewModel

> **Hilt is fully integrated (prerequisite #9 ✅ Done).** The CompositionLocal bridging
> pattern described here is no longer needed for `AppSettingsDataStore` or any repository.
> All dependencies must be provided through `@HiltViewModel` constructor injection.
>
> Do **not** create `staticCompositionLocalOf<AppSettingsDataStore>` or similar —
> use `@Inject constructor(private val appSettings: AppSettingsDataStore)` in the ViewModel.

If a rare third-party View-only dependency has no Hilt binding, the `CompositionLocal`
technique remains available as a last resort for that specific case only. Remove it
once a proper Hilt module is in place.

---

## 7. Phased Migration Plan

### Phase 0 — Foundation (prerequisite, aligns with ROADMAP #3–14)

Complete before writing any `@Composable` production code.

| Task | Owner note |
|------|------------|
| ✅ Remove `allowMainThreadQueries()` | AppDatabase.kt — done |
| ✅ Move API keys to BuildConfig | build.gradle.kts + local.properties — done |
| ⬜ Enable R8 minification | build.gradle.kts release config |
| 🔄 Add ViewModels to all activities/fragments | WelcomeViewModel + UserSettingsViewModel done; all others remain |
| ⬜ Proper Room migrations | AppDatabase.kt Migration objects |
| ✅ Integrate Hilt | @HiltAndroidApp on AkilimoApp; @AndroidEntryPoint on 25+ Activities/Fragments; @HiltViewModel on ViewModels; version 2.57.1, kapt — done |
| ✅ DataStore for ALL 17 settings keys | AppSettingsDataStore replaces deleted SessionManager.kt; SharedPreferencesMigration from "new-akilimo-config" — done |
| ⬜ Jetpack NavGraph (View-based first) | replace startActivity() calls |

---

### Phase 1 — Compose Infrastructure

Deliver the theme, no user-visible changes.

| Task | Files to create |
|------|----------------|
| Enable Compose in build.gradle.kts | `app/build.gradle.kts` |
| Create `AkilimoTheme` + color/type/shape files | `ui/theme/AkilimoColors.kt`, `AkilimoTypography.kt`, `AkilimoShapes.kt`, `AkilimoTheme.kt` |
| Create common composable atoms | `ui/components/compose/AkilimoButton.kt`, `AkilimoCard.kt`, `AkilimoTextField.kt`, `NetworkBanner.kt` |
| Create `ComposeBaseActivity` extending `BaseActivity` | `base/ComposeBaseActivity.kt` |
| Add screenshot / composable preview tests | `ui/theme/AkilimoThemePreview.kt` |

---

### Phase 2 — Leaf Screens (no deep state, isolated)

Each screen: create `*Screen.kt` composable + `*ViewModel` → wire via `ComposeView` bridge in
existing activity → delete XML layout → delete ViewBinding reference.

| Screen | Current class | New composable | ViewModel |
|--------|--------------|----------------|-----------|
| User Settings | `UserSettingsActivity.kt` | `UserSettingsScreen.kt` | `UserSettingsViewModel.kt` |
| Get Recommendation | `GetRecommendationActivity.kt` | `GetRecommendationScreen.kt` | `GetRecommendationViewModel.kt` |
| Recommendations display | `RecommendationsActivity.kt` | `RecommendationsScreen.kt` | `RecommendationsViewModel.kt` |
| Main / splash | `MainActivity.kt` | `SplashScreen.kt` | — |

---

### Phase 3 — Domain Screens (RecyclerViews → LazyLayouts)

Each adapter-backed RecyclerView becomes a `LazyColumn` or `LazyVerticalGrid`.
See §9 for the full adapter equivalents table.

| Screen | Current class | New composable | Key change |
|--------|--------------|----------------|------------|
| Cassava Yield | `CassavaYieldActivity.kt` | `CassavaYieldScreen.kt` | `LazyVerticalGrid` + `CassavaYieldCard` |
| Maize Performance | `MaizePerformanceActivity.kt` | `MaizePerformanceScreen.kt` | `LazyVerticalGrid` + `MaizePerformanceCard` |
| Cassava Market | `CassavaMarketActivity.kt` | `CassavaMarketScreen.kt` | `LazyColumn` |
| Maize Market | `MaizeMarketActivity.kt` (verify name) | `MaizeMarketScreen.kt` | `LazyColumn` |
| Sweet Potato Market | `IcSweetPotatoActivity.kt` | `SweetPotatoMarketScreen.kt` | `LazyColumn` |
| Fertilizers | `FertilizersActivity.kt` | `FertilizersScreen.kt` | `LazyVerticalGrid` toggle |
| Investment Amount | `InvestmentAmountActivity.kt` | `InvestmentAmountScreen.kt` | `LazyVerticalGrid` |
| Weed Management | `WeedManagementActivity.kt` | `WeedManagementScreen.kt` | `LazyColumn` |
| Weed Control Costs | `WeedControlCostsActivity.kt` | `WeedControlCostsScreen.kt` | Form fields |
| Manual Tillage Cost | `ManualTillageCostActivity.kt` | `ManualTillageCostScreen.kt` | Form fields |
| Tractor Access | `TractorAccessActivity.kt` | `TractorAccessScreen.kt` | Toggle + form |
| Recommendation Use Case | `RecommendationUseCaseActivity.kt` | `RecommendationUseCaseScreen.kt` | `LazyColumn` |
| Location Picker | `LocationPickerActivity.kt` | `LocationPickerScreen.kt` | `AndroidView` (Mapbox) |
| Alternative Planting Schedule | `BppActivity.kt` / `SphActivity.kt` | `PlantingScheduleScreen.kt` | Display only |
| IC Maize | `IcMaizeActivity.kt` | `IcMaizeScreen.kt` | Form + list |

---

### Phase 4 — Onboarding Stepper Replacement

The `StepperLayout` library (com.stepstone.stepper) has no Compose equivalent.
Replace with a custom `OnboardingPager` built on `HorizontalPager`.

**Target structure:**

```kotlin
// AppNavGraph.kt
composable("onboarding") {
    OnboardingFlow(onComplete = { navController.navigate("recommendations") })
}

// OnboardingFlow.kt
@Composable
fun OnboardingFlow(onComplete: () -> Unit) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { state.steps.size })

    Scaffold(
        bottomBar = {
            OnboardingNavBar(
                currentStep = pagerState.currentPage,
                totalSteps = state.steps.size,
                canProceed = state.currentStepValid,
                onBack = { /* scroll pager back */ },
                onNext = { viewModel.validateAndAdvance(pagerState) },
            )
        }
    ) { padding ->
        HorizontalPager(state = pagerState, userScrollEnabled = false) { page ->
            when (state.steps[page]) {
                OnboardingStep.Welcome       -> WelcomeStepContent(viewModel)
                OnboardingStep.BioData       -> BioDataStepContent(viewModel)
                OnboardingStep.Country       -> CountryStepContent(viewModel)
                OnboardingStep.Location      -> LocationStepContent(viewModel)
                OnboardingStep.AreaUnit      -> AreaUnitStepContent(viewModel)
                OnboardingStep.PlantingDate  -> PlantingDateStepContent(viewModel)
                OnboardingStep.Tillage       -> TillageStepContent(viewModel)
                OnboardingStep.InvestmentPref -> InvestmentPrefStepContent(viewModel)
                OnboardingStep.Summary       -> SummaryStepContent(viewModel, onComplete)
            }
        }
    }
}
```

**Fragment-to-composable mapping for the stepper:**

| Fragment | Step composable | `prefillFromEntity()` equivalent |
|----------|----------------|----------------------------------|
| `WelcomeFragment` | `WelcomeStepContent` | `OnboardingViewModel.init { }` loads locale from repo |
| `BioDataFragment` | `BioDataStepContent` | `OnboardingViewModel.loadBioData()` |
| `CountryFragment` | `CountryStepContent` | `OnboardingViewModel.loadCountry()` |
| `LocationFragment` | `LocationStepContent` | `OnboardingViewModel.loadLocation()` |
| `AreaUnitFragment` | `AreaUnitStepContent` | `OnboardingViewModel.loadAreaUnit()` |
| `PlantingDateFragment` | `PlantingDateStepContent` | `OnboardingViewModel.loadDates()` |
| `TillageOperationFragment` | `TillageStepContent` | `OnboardingViewModel.loadTillage()` |
| `InvestmentPrefFragment` | `InvestmentPrefStepContent` | `OnboardingViewModel.loadInvestmentPref()` |
| `DisclaimerFragment` | `DisclaimerStepContent` | shown once; gated by `appSettings.disclaimerRead` |
| `TermsFragment` | `TermsStepContent` | shown once; gated by `appSettings.termsAccepted` |
| `SummaryFragment` | `SummaryStepContent` | `OnboardingViewModel.buildSummary()` |

---

### Phase 5 — NavGraph Migration and Final Cleanup

Consolidate all Compose destinations into a single `NavHost`. Remove all
`startActivity()` / `Intent` navigation, delete all XML layout files, disable
`viewBinding`.

```kotlin
// AppNavGraph.kt — full nav graph
@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = "onboarding") {
        composable("onboarding") { OnboardingFlow(onComplete = { navController.navigate("recommendations") }) }
        composable("recommendations") { RecommendationsScreen(onNavigate = { navController.navigate(it) }) }
        composable("settings") { UserSettingsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("cassava_yield") { CassavaYieldScreen(onNavigateBack = { navController.popBackStack() }) }
        composable("fertilizers") { FertilizersScreen(onNavigateBack = { navController.popBackStack() }) }
        // … all other destinations
    }
}
```

**Final cleanup checklist:**
- [ ] `viewBinding = false` in `build.gradle.kts`
- [ ] Delete all `app/src/main/res/layout/*.xml` files
- [ ] Delete `base/BaseFragment.kt`, `base/BaseStepFragment.kt`
- [ ] Remove `StepperLayout`, `ViewPump`, `Reword`, `AppLocale` (replace with Compose locale + DataStore)
- [x] ~~Remove `ProcessPhoenix`~~ — already removed; locale changes use `AppCompatDelegate.setApplicationLocales()`
- [ ] Remove `CountryCodePicker` view library — replace with Compose version
- [ ] Keep `BaseActivity` only as a thin Hilt host (`@AndroidEntryPoint` + edge-to-edge setup)

---

## 8. Screen-by-Screen Conversion Map

| Current Activity | XML Layout | Compose Screen | Phase |
|---|---|---|---|
| `MainActivity` | `activity_main.xml` | `SplashScreen.kt` | 2 |
| `HomeStepperActivity` | `activity_home_stepper.xml` | `OnboardingFlow.kt` | 4 |
| `UserSettingsActivity` | `activity_user_settings.xml` | `UserSettingsScreen.kt` | 2 |
| `RecommendationsActivity` | `activity_recommendations.xml` | `RecommendationsScreen.kt` | 2 |
| `GetRecommendationActivity` | `activity_get_recommendation.xml` | `GetRecommendationScreen.kt` | 2 |
| `RecommendationUseCaseActivity` | `activity_recommendation_use_case.xml` | `RecommendationUseCaseScreen.kt` | 3 |
| `CassavaYieldActivity` | `activity_cassava_yield.xml` | `CassavaYieldScreen.kt` | 3 |
| `CassavaMarketActivity` | `activity_cassava_market.xml` | `CassavaMarketScreen.kt` | 3 |
| `MaizePerformanceActivity` | `activity_maize_performance.xml` | `MaizePerformanceScreen.kt` | 3 |
| `MaizeMarketActivity` | `activity_maize_market.xml` | `MaizeMarketScreen.kt` | 3 |
| `IcSweetPotatoActivity` | `activity_ic_sweet_potato.xml` | `SweetPotatoMarketScreen.kt` | 3 |
| `FertilizersActivity` | `activity_fertilizers.xml` | `FertilizersScreen.kt` | 3 |
| `InvestmentAmountActivity` | `activity_investment_amount.xml` | `InvestmentAmountScreen.kt` | 3 |
| `WeedManagementActivity` | `activity_weed_management.xml` | `WeedManagementScreen.kt` | 3 |
| `WeedControlCostsActivity` | `activity_weed_control_costs.xml` | `WeedControlCostsScreen.kt` | 3 |
| `ManualTillageCostActivity` | `activity_manual_tillage_cost.xml` | `ManualTillageCostScreen.kt` | 3 |
| `TractorAccessActivity` | `activity_tractor_access.xml` | `TractorAccessScreen.kt` | 3 |
| `LocationPickerActivity` | `activity_location_picker.xml` | `LocationPickerScreen.kt` | 3 |
| `BppActivity` | `activity_alternative_planting_schedule.xml` | `PlantingScheduleScreen.kt` | 3 |
| `SphActivity` | _(shares layout)_ | _(merged into PlantingScheduleScreen)_ | 3 |
| `IcMaizeActivity` | — | `IcMaizeScreen.kt` | 3 |

Fragment-to-composable mapping covered in §10.

---

## 9. Adapter → LazyLayout Equivalents

| Adapter | Pattern | Compose equivalent |
|---------|---------|-------------------|
| `CassavaYieldAdapter` | `RecyclerView` 2-col grid, image tap select | `LazyVerticalGrid(columns = Fixed(2))` + `CassavaYieldCard(item, onSelect)` |
| `MaizePerformanceAdapter` | `RecyclerView` 2-col grid, image tap select | `LazyVerticalGrid(columns = Fixed(2))` + `MaizePerformanceCard(item, onSelect)` |
| `FertilizerAdapter` | `RecyclerView` toggle + checkbox list | `LazyColumn` + `FertilizerItem(item, onToggle)` |
| `CassavaMarketPriceAdapter` | `RecyclerView` single-select list | `LazyColumn` + `MarketPriceItem(item, selected, onSelect)` |
| `CassavaUnitAdapter` | `RecyclerView` single-select list | `LazyColumn` + `CassavaUnitItem` |
| `StarchFactoryAdapter` | `RecyclerView` single-select | `LazyColumn` + `StarchFactoryItem` |
| `InvestmentAmountAdapter` | `RecyclerView` grid | `LazyVerticalGrid` + `InvestmentAmountCard` |
| `RecommendationAdapter` | `RecyclerView` expandable | `LazyColumn` + `RecommendationCard(expanded, onToggle)` |
| `GenericSelectableAdapter` | `RecyclerView` single-select | `LazyColumn` + `SelectableItem` |
| `BaseSpinnerAdapter` / `ValueOptionAdapter` | `AutoCompleteTextView` dropdown | `ExposedDropdownMenuBox` + `DropdownMenuItem` |

**Selection state pattern for image-grid screens:**

```kotlin
// In ViewModel
private val _selectedId = MutableStateFlow<Int?>(null)

fun select(id: Int) {
    _selectedId.value = id
    // persist via repo
}

val items: StateFlow<List<CassavaYield>> = /* from repo */
    .combine(_selectedId) { list, selectedId ->
        list.map { it.copy(isSelected = it.id == selectedId) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

// In composable
@Composable
fun CassavaYieldCard(item: CassavaYield, onSelect: (Int) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Card(
        onClick = { onSelect(item.id) },
        border = if (item.isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        // image + overlay + checkmark
    }
}
```

---

## 10. Stepper Replacement Strategy

> **Partial migration already done.** `HomeStepperActivity` has been migrated from the
> `StepperLayout` library (com.stepstone.stepper) to **ViewPager2 + a custom `WizardAdapter`**
> with a `WizardStep` interface, `BaseStepFragment`, and a `pendingOnSelected` pattern.
> Phase 4 now replaces the ViewPager2-based wizard with a `HorizontalPager` Compose
> implementation. The `com.stepstone.stepper` dependency may still be present in
> `app/build.gradle.kts` but it is no longer driving the UI.

The existing `WizardAdapter` (ViewPager2) drives `HomeStepperActivity` after the View-layer
migration. It has no Compose equivalent and must be fully replaced in Phase 4.

### Replacing `verifyStep()` validation

```kotlin
// In OnboardingViewModel
data class StepValidation(val isValid: Boolean, val errorMessage: String? = null)

private val _stepValidations = MutableStateFlow(mapOf<OnboardingStep, StepValidation>())

fun validateCurrentStep(step: OnboardingStep): Boolean {
    val result = when (step) {
        OnboardingStep.BioData -> validateBioData(uiState.value)
        OnboardingStep.Country -> validateCountry(uiState.value)
        // …
        else -> StepValidation(isValid = true)
    }
    _stepValidations.update { it + (step to result) }
    return result.isValid
}
```

### Replacing conditional steps

`HomeStepperActivity.buildStepList()` conditionally includes Disclaimer and Terms steps.
In Compose, drive this through ViewModel state:

```kotlin
val steps: StateFlow<List<OnboardingStep>> = combine(
    appSettings.disclaimerRead,   // AppSettingsDataStore Flow<Boolean>
    appSettings.termsAccepted,    // AppSettingsDataStore Flow<Boolean>
) { disclaimerRead, termsAccepted ->
    buildList {
        add(OnboardingStep.Welcome)
        if (!disclaimerRead) add(OnboardingStep.Disclaimer)
        if (!termsAccepted)  add(OnboardingStep.Terms)
        add(OnboardingStep.BioData)
        // …
    }
}.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
```

---

## 11. Testing Strategy

### Unit tests (ViewModels)
- Test `UiState` transitions: given initial state + user action → expected state.
- No Compose runtime needed — pure Kotlin with `kotlinx-coroutines-test` and `Turbine`.

### Composable preview tests
- Create `@Preview` composables for every screen and component.
- Use `paparazzi` for screenshot regression without a device.

### Integration tests
- `ComposeTestRule` + `hiltRule` for each screen: launch screen → interact → assert state.

### End-to-end
- Keep existing Espresso tests running on View screens until they are migrated.
- Add Compose UI tests incrementally: one per migrated screen.

---

## 12. Library Removals at Completion

These dependencies become redundant after full migration and must be removed in Phase 5:

| Library | Replacement | Status |
|---------|-------------|--------|
| `com.stepstone.stepper` | Custom `HorizontalPager` `OnboardingFlow` | ⬜ Still needed (ViewPager2 wizard is interim; replace in Phase 4) |
| `dev.b3nedikt.app_locale` + `reword` + `viewpump` | Compose `CompositionLocalProvider(LocalContext)` + `AppCompatDelegate` | ⬜ Remove in Phase 5 |
| `com.jakewharton.processphoenix` | `AppCompatDelegate.setApplicationLocales()` — in-process locale change | ✅ Already removed from `app/build.gradle.kts` |
| `io.github.chaosleung:pinview` (if present) | Compose `BasicTextField` | ⬜ Remove in Phase 5 |
| `hbb20:ccp` (Country Code Picker) | Compose `ExposedDropdownMenuBox` | ⬜ Remove in Phase 5 |
| `androidx.viewbinding` (buildFeature) | All ViewBinding generated classes removed | ⬜ Remove in Phase 5 final cleanup |
| `io.github.chuckerteam:chucker` (if debug) | OkHttp logging interceptor | ⬜ Remove in Phase 5 |

**Do not** remove `retrofit`, `okhttp`, `room`, `hilt`, `moshi`, `firebase`, `mapbox`, `timber`,
`sentry`, or `workmanager` — all survive the Compose migration.
