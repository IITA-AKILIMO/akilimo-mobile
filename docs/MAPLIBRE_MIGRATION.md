# Mapbox → MapLibre Migration

## Background

Mapbox SDK v11 requires a proprietary Maven repository, a secret download token (`MAPBOX_DOWNLOADS_TOKEN`), and a runtime API token baked into the APK. The entire Mapbox surface area in this app is a single screen — `LocationPickerScreen.kt`.

MapLibre Native Android (`org.maplibre.gl:android-sdk`) is an open-source fork of Mapbox GL Android. It is published to Maven Central (no special repository), requires no API token, and has a nearly identical API — making it the lowest-friction replacement.

**Scope**: 1 screen, 1 dependency, 4 build-config changes.

---

## Phase 1 — Build Config Cleanup

Remove all Mapbox infrastructure from the build. These three changes must land **together** to avoid a broken Gradle sync.

### 1.1 `gradle/libs.versions.toml`

Remove:
```toml
mapbox-sdk = "11.17.1"
mapbox-annotation = "0.9.0"
mapbox-places = "0.12.0"
```
```toml
mapbox-sdk = { group = "com.mapbox.maps", name = "android-ndk27", version.ref = "mapbox-sdk" }
mapbox-annotation = { group = "com.mapbox.mapboxsdk", name = "mapbox-android-plugin-annotation-v9", version.ref = "mapbox-annotation" }
mapbox-places = { group = "com.mapbox.mapboxsdk", name = "mapbox-android-plugin-places-v9", version.ref = "mapbox-places" }
```

Add:
```toml
maplibre-sdk = "11.8.8"
```
```toml
maplibre-sdk = { group = "org.maplibre.gl", name = "android-sdk", version.ref = "maplibre-sdk" }
```

### 1.2 `app/build.gradle.kts`

Remove:
```kotlin
val mapboxRuntimeToken = env.MAPBOX_RUNTIME_TOKEN.orElse("")
buildConfigField("String", "MAPBOX_RUNTIME_TOKEN", q(mapboxRuntimeToken))
```
```kotlin
implementation(libs.mapbox.sdk)
```

Add:
```kotlin
implementation(libs.maplibre.sdk)
```

### 1.3 `settings.gradle.kts`

Remove the entire Mapbox Maven block:
```kotlin
maven {
    url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
    authentication { create<BasicAuthentication>("basic") }
    credentials {
        username = "mapbox"
        password = providers.gradleProperty("MAPBOX_DOWNLOADS_TOKEN").getOrElse("")
    }
}
```
MapLibre resolves from the already-present `mavenCentral()`.

### 1.4 `gradle.properties`

Remove:
```
MAPBOX_DOWNLOADS_TOKEN=sk.eyJ...
```

### Verification
```bash
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -E "mapbox|maplibre"
# Expected: only maplibre entries, no mapbox entries
```

---

## Phase 2 — Satellite Style Asset

Create `app/src/main/assets/style_satellite.json`. This replaces `Style.STANDARD_SATELLITE` with ESRI World Imagery raster tiles — free, no API key, global coverage.

```json
{
  "version": 8,
  "sources": {
    "esri-satellite": {
      "type": "raster",
      "tiles": [
        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
      ],
      "tileSize": 256,
      "attribution": "Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGS, and the GIS User Community"
    }
  },
  "layers": [
    {
      "id": "satellite",
      "type": "raster",
      "source": "esri-satellite",
      "minzoom": 0,
      "maxzoom": 22
    }
  ]
}
```

---

## Phase 3 — Rewrite `LocationPickerScreen.kt`

File: `app/src/main/java/com/akilimo/mobile/ui/screens/settings/LocationPickerScreen.kt`

The ViewModel, address card, weather card, confirm button, and permission launcher are **unchanged**. Only the map `AndroidView` block and its supporting state/helpers are replaced.

### 3.1 Import swaps

| Remove (`com.mapbox.*`) | Add (`org.maplibre.*`) |
|---|---|
| `com.mapbox.common.MapboxOptions` | *(not needed — no token)* |
| `com.mapbox.geojson.Point` | `org.maplibre.android.geometry.LatLng` |
| `com.mapbox.maps.CameraOptions` | `org.maplibre.android.camera.CameraUpdateFactory` |
| `com.mapbox.maps.MapView` | `org.maplibre.android.maps.MapView` |
| `com.mapbox.maps.Style` | `org.maplibre.android.maps.Style` |
| `com.mapbox.maps.plugin.animation.flyTo` | *(use `animateCamera` directly)* |
| `com.mapbox.maps.plugin.animation.MapAnimationOptions` | *(duration passed as Int ms to `animateCamera`)* |
| `com.mapbox.maps.plugin.annotation.*` | `org.maplibre.android.annotations.MarkerOptions` |
| `com.mapbox.maps.plugin.gestures.addOnMapClickListener` | `org.maplibre.android.maps.MapboxMap.OnMapClickListener` |
| `com.mapbox.maps.plugin.locationcomponent.location` | `org.maplibre.android.location.LocationComponent` + `LocationComponentActivationOptions` + `RenderMode` |

Add:
```kotlin
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapboxMap   // MapLibre reuses this class name
import android.webkit.WebView               // remove if previously added
```

### 3.2 State variables

Replace:
```kotlin
var mapView by remember { mutableStateOf<MapView?>(null) }        // MapboxMapView → MapLibreMapView (same name)
var selectedPoint by remember { mutableStateOf<Point?>(null) }    // Point → LatLng
var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
var styleLoaded by remember { mutableStateOf(false) }
```

With:
```kotlin
var mapView by remember { mutableStateOf<MapView?>(null) }
var mapboxMapRef by remember { mutableStateOf<MapboxMap?>(null) }
var selectedPoint by remember { mutableStateOf<LatLng?>(null) }
var currentMarker by remember { mutableStateOf<Marker?>(null) }
```

### 3.3 `AndroidView` factory

```kotlin
AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { ctx ->
        MapLibre.getInstance(ctx)          // no token — replaces MapboxOptions.accessToken
        MapView(ctx).also { mv ->
            mapView = mv
            mv.onCreate(null)
            mv.getMapAsync { map ->
                mapboxMapRef = map
                map.setStyle("asset://style_satellite.json") { _ ->
                    // Initial camera
                    val initLat = if (route.lat != 0.0) route.lat else -1.2921
                    val initLng = if (route.lon != 0.0) route.lon else 36.8219
                    val initZoom = if (route.zoom > 0) route.zoom else 12.0
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(initLat, initLng), initZoom)
                    )

                    // Location component
                    if (permissionHelper.hasLocationPermission(ctx)) {
                        try {
                            val lc = map.locationComponent
                            lc.activateLocationComponent(
                                LocationComponentActivationOptions.builder(ctx, style).build()
                            )
                            lc.isLocationComponentEnabled = true
                            lc.renderMode = RenderMode.COMPASS
                        } catch (e: Exception) {
                            Sentry.captureException(e)
                        }
                        viewModel.fetchCurrentLocation()
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    }

                    // Map click
                    map.addOnMapClickListener { latLng ->
                        selectedPoint = latLng
                        currentMarker = updateMarker(map, currentMarker, latLng)
                        map.animateCamera(CameraUpdateFactory.newLatLng(latLng), 1000)
                        viewModel.fetchAddress(latLng.latitude, latLng.longitude)
                        viewModel.fetchWeather(latLng.latitude, latLng.longitude)
                        true
                    }
                }
            }
        }
    },
    onRelease = { it.onDestroy() }
)
```

### 3.4 MapView lifecycle in Compose

MapLibre `MapView` requires explicit lifecycle calls. Add a `DisposableEffect`:

```kotlin
DisposableEffect(Unit) {
    mapView?.onStart()
    mapView?.onResume()
    onDispose {
        mapView?.onPause()
        mapView?.onStop()
        mapView?.onDestroy()
    }
}
```

### 3.5 Location `LaunchedEffect`

Replace:
```kotlin
val point = Point.fromLngLat(loc.longitude, loc.latitude)
mv.mapboxMap.flyTo(CameraOptions.Builder().center(point).zoom(DEFAULT_ZOOM).build(),
    MapAnimationOptions.mapAnimationOptions { duration(1500) })
selectedPoint = point
annotationManager?.let { updateMarker(it, point) }
```

With:
```kotlin
val latLng = LatLng(loc.latitude, loc.longitude)
mapboxMapRef?.animateCamera(
    CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM), 1500
)
selectedPoint = latLng
mapboxMapRef?.let { currentMarker = updateMarker(it, currentMarker, latLng) }
```

### 3.6 Confirm button `onClick`

Replace:
```kotlin
val zoom = mv?.mapboxMap?.cameraState?.zoom ?: DEFAULT_ZOOM
val result = LocationResult(
    lat = point.latitude(), lon = point.longitude(),
    alt = point.altitude().takeUnless { it.isNaN() } ?: 0.0,
    ...
)
```

With:
```kotlin
val zoom = mapboxMapRef?.cameraPosition?.zoom ?: DEFAULT_ZOOM
val result = LocationResult(
    lat = pt.latitude, lon = pt.longitude,
    alt = pt.altitude,
    ...
)
```

### 3.7 `updateMarker` helper

Replace `PointAnnotationManager` version with:

```kotlin
private fun updateMarker(map: MapboxMap, current: Marker?, latLng: LatLng): Marker {
    current?.remove()
    return map.addMarker(
        MarkerOptions()
            .position(latLng)
            .icon(IconFactory.getInstance(context).fromResource(R.drawable.ic_location_pin))
    )
}
```

Remove: `updateMarker(manager: PointAnnotationManager, point: Point)`, the `ValueAnimator` bounce block, `MARKER_ICON_ID` constant, and the bitmap-drawing block inside the style callback.

---

## Feature Parity

| Feature | Mapbox v11 | MapLibre |
|---|---|---|
| Satellite tiles | `Style.STANDARD_SATELLITE` | ESRI raster style (`asset://style_satellite.json`) |
| API token | `MAPBOX_RUNTIME_TOKEN` required | None — `MapLibre.getInstance(ctx)` |
| Initial camera | `setCamera(CameraOptions.Builder()...)` | `moveCamera(CameraUpdateFactory.newLatLngZoom(...))` |
| Tap to select | `addOnMapClickListener { point: Point -> }` | `addOnMapClickListener { latLng: LatLng -> }` |
| Animated pan | `mapboxMap.flyTo(CameraOptions, MapAnimationOptions)` | `animateCamera(CameraUpdateFactory.newLatLng(...), durationMs)` |
| Red pin marker | `PointAnnotationManager` + `PointAnnotationOptions` | `MapboxMap.addMarker(MarkerOptions())` |
| Location dot | Mapbox `location` plugin | MapLibre `LocationComponent` |
| Zoom read | `mapboxMap.cameraState.zoom` | `mapboxMap.cameraPosition.zoom` |
| Coordinate type | `Point` (GeoJSON, lon-first) | `LatLng` (lat-first) |

---

## Files Touched

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | Remove 3 mapbox entries, add 1 maplibre entry |
| `app/build.gradle.kts` | Swap dependency, remove `MAPBOX_RUNTIME_TOKEN` BuildConfig field |
| `settings.gradle.kts` | Remove Mapbox Maven repo block |
| `gradle.properties` | Remove `MAPBOX_DOWNLOADS_TOKEN` |
| `app/src/main/assets/style_satellite.json` | **New** — ESRI satellite style |
| `app/src/main/java/.../ui/screens/settings/LocationPickerScreen.kt` | Rewrite map block, import swap |

`LocationPickerViewModel.kt`, `LocationHelper.kt`, `GeocodingService.kt`, `WeatherService.kt` — **no changes**.

---

## Verification

```bash
# Compile — no Mapbox symbols, no missing imports
./gradlew :app:compileDebugKotlin

# Confirm dependency tree
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep -E "mapbox|maplibre"
# Expected: only org.maplibre.gl entries

# Unit tests (ViewModel unchanged)
./gradlew testDebugUnitTest
```

Manual checklist after installing the debug APK:
- [ ] Satellite map loads (ESRI imagery visible)
- [ ] Tap anywhere → red pin drops, address card and weather card appear
- [ ] "Use my location" → map animates to device position, pin placed
- [ ] Confirm button is disabled until a point is selected
- [ ] Confirm returns `LocationResult` to the previous screen correctly
- [ ] Back navigation works without crash
