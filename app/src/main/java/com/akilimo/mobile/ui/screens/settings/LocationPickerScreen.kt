package com.akilimo.mobile.ui.screens.settings

import android.Manifest
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Parcelable
import android.provider.Settings
import android.view.animation.BounceInterpolator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.enums.EnumCoverageStatus
import com.akilimo.mobile.navigation.LocationPickerRoute
import com.akilimo.mobile.ui.components.compose.BackTopAppBar
import com.akilimo.mobile.ui.components.compose.popBackStackSafely
import com.akilimo.mobile.ui.viewmodels.LocationPickerViewModel
import com.akilimo.mobile.ui.viewmodels.LocationPickerViewModel.LocationProblem
import com.akilimo.mobile.utils.PermissionHelper
import com.mapbox.common.Cancelable
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapLoadingErrorType
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import io.sentry.Sentry
import kotlinx.parcelize.Parcelize

private const val DEFAULT_ZOOM = 15.0
private const val DEFAULT_INITIAL_ZOOM = 12.0
private const val NAIROBI_LON = 36.8219
private const val NAIROBI_LAT = -1.2921

/** Where the map opens for a country: its centre and a zoom that shows most of it. */
private data class CountryView(val lat: Double, val lon: Double, val zoom: Double)

private val COUNTRY_VIEWS = mapOf(
    "NG" to CountryView(lat = 9.08, lon = 8.68, zoom = 5.5),
    "TZ" to CountryView(lat = -6.37, lon = 34.89, zoom = 5.5),
    "GH" to CountryView(lat = 7.95, lon = -1.02, zoom = 6.3),
    "RW" to CountryView(lat = -1.94, lon = 29.87, zoom = 7.8),
    "BI" to CountryView(lat = -3.37, lon = 29.92, zoom = 8.0),
)
private const val MARKER_ICON_ID = "custom-marker"
private const val MARKER_ICON_SIZE = 1.5
private const val BOUNCE_DURATION_MS = 600L
private const val BOUNCE_SCALE_OFFSET = 0.5f
private const val MAP_FLY_DURATION_MS = 1500L
private const val MAP_CLICK_FLY_DURATION_MS = 1000L
private const val COVERAGE_OVERVIEW_ZOOM = 5.5
private const val COVERAGE_SOURCE_ID = "akilimo-coverage"
private const val COVERAGE_FILL_LAYER_ID = "akilimo-coverage-fill"
private const val COVERAGE_HALO_LAYER_ID = "akilimo-coverage-halo"
private const val COVERAGE_LINE_LAYER_ID = "akilimo-coverage-line"
private const val COVERAGE_SLOT = "middle"
private const val COVERAGE_COLOR = "#43A047"
private const val COVERAGE_HALO_COLOR = "#FFFFFF"
// Satellite imagery is busy and dark: a stronger fill plus a white-haloed outline keeps the edge visible
private const val COVERAGE_FILL_OPACITY = 0.35
private const val COVERAGE_LINE_WIDTH = 2.5
private const val COVERAGE_HALO_WIDTH = 5.0
private const val COVERAGE_HALO_OPACITY = 0.8

/** Holds the location result returned from this screen. */
@Parcelize
data class LocationResult(
    val lat: Double,
    val lon: Double,
    val alt: Double,
    val zoom: Double,
    val placeName: String,
) : Parcelable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    route: LocationPickerRoute,
    navController: NavHostController,
) {
    val viewModel = hiltViewModel<LocationPickerViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionHelper = remember { PermissionHelper() }
    val weatherSummaryFormat = stringResource(R.string.lbl_weather_summary)
    val weatherFeelsLikeFormat = stringResource(R.string.lbl_weather_feels_like)
    val mapDescription = stringResource(R.string.cd_location_map)

    val hasSavedLocation = route.lat != 0.0 || route.lon != 0.0

    // Mapbox Map references managed via remember
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }
    // Bumped on every successful style load so the overlay and marker are redrawn on a fresh style
    var styleGeneration by remember { mutableIntStateOf(0) }
    var mapLoadFailed by remember { mutableStateOf(false) }
    // Survives rotation so the permission prompt / GPS fetch is not repeated
    var autoLocateDone by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        // Approximate (coarse) location is enough to place the farm
        if (perms.any { it.value }) {
            viewModel.fetchCurrentLocation()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    fun locateMe() {
        if (permissionHelper.hasLocationPermission(context)) {
            viewModel.fetchCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
        }
    }

    // Start from the saved farm (Confirm enabled straight away); only look up GPS when there is none.
    // Independent of the map style, so it works offline when no map tiles load.
    LaunchedEffect(Unit) {
        if (hasSavedLocation) {
            viewModel.seedSavedLocation(route.lat, route.lon)
        } else if (!autoLocateDone && route.countryCode.isBlank()) {
            // With a country chosen the farmer picked the manual option: stay on that country
            // instead of flying to the phone's position, which may be elsewhere
            autoLocateDone = true
            locateMe()
        }
    }

    LaunchedEffect(route.countryCode) {
        viewModel.loadCoverage(route.countryCode)
    }

    // Show the blue "you are here" puck once permission is available
    LaunchedEffect(mapView, styleGeneration, state.selected) {
        val mv = mapView ?: return@LaunchedEffect
        if (permissionHelper.hasLocationPermission(context)) {
            try {
                mv.location.updateSettings {
                    enabled = true
                    pulsingEnabled = true
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }
    }

    // GPS fix: move the camera there
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LocationPickerViewModel.Effect.FlyTo -> mapView?.mapboxMap?.flyTo(
                    CameraOptions.Builder()
                        .center(Point.fromLngLat(effect.lon, effect.lat))
                        .zoom(DEFAULT_ZOOM)
                        .build(),
                    MapAnimationOptions.mapAnimationOptions { duration(MAP_FLY_DURATION_MS) }
                )
            }
        }
    }

    // Keep the pin in step with the selection (tap, GPS or saved farm)
    LaunchedEffect(state.selected, annotationManager, styleGeneration) {
        val am = annotationManager ?: return@LaunchedEffect
        val sel = state.selected ?: return@LaunchedEffect
        if (!styleLoaded) return@LaunchedEffect
        updateMarker(am, Point.fromLngLat(sel.lon, sel.lat))
    }

    // Shade the supported area once both the style and the GeoJSON are ready
    LaunchedEffect(styleGeneration, state.coverageGeoJson) {
        val geoJson = state.coverageGeoJson ?: return@LaunchedEffect
        val mv = mapView ?: return@LaunchedEffect
        if (!styleLoaded) return@LaunchedEffect
        mv.mapboxMap.style?.let { style ->
            try {
                drawCoverage(style, geoJson)
            } catch (e: Exception) {
                Sentry.captureException(e)
            }
        }
        // No saved farm and no pick yet: show the country's supported area instead of a default city
        val area = state.coverageArea
        if (area != null && state.selected == null && !hasSavedLocation) {
            mv.mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(area.centerLon, area.centerLat))
                    .zoom(COVERAGE_OVERVIEW_ZOOM)
                    .build()
            )
        }
    }

    fun loadMapStyle(mv: MapView) {
        mapLoadFailed = false
        mv.mapboxMap.loadStyle(Style.STANDARD_SATELLITE) { style ->
            addMarkerImage(context, style)
            styleLoaded = true
            styleGeneration++
        }
    }

    // Style / tile errors (typically offline): tell the user instead of leaving a blank screen
    DisposableEffect(mapView) {
        val mv = mapView
        val subscription: Cancelable? = mv?.mapboxMap?.subscribeMapLoadingError { error ->
            if (error.type == MapLoadingErrorType.STYLE ||
                error.type == MapLoadingErrorType.SPRITE ||
                error.type == MapLoadingErrorType.TILE ||
                error.type == MapLoadingErrorType.SOURCE
            ) {
                mapLoadFailed = true
            }
        }
        onDispose { subscription?.cancel() }
    }

    Scaffold(
        topBar = {
            BackTopAppBar(
                title = stringResource(R.string.lbl_farm_location),
                onBack = { navController.popBackStackSafely() },
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            // Mapbox map via AndroidView
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = mapDescription },
                factory = { ctx ->
                    MapboxOptions.accessToken = BuildConfig.MAPBOX_RUNTIME_TOKEN
                    MapView(ctx).also { mv ->
                        // A saved farm wins; otherwise open on the country the farmer chose, so the
                        // map is right even before (or without) the coverage overlay loading
                        val countryView = COUNTRY_VIEWS[route.countryCode.uppercase()]
                        val initial = when {
                            hasSavedLocation -> Point.fromLngLat(route.lon, route.lat)
                            countryView != null -> Point.fromLngLat(countryView.lon, countryView.lat)
                            else -> Point.fromLngLat(NAIROBI_LON, NAIROBI_LAT) // no country chosen: Nairobi
                        }
                        val initialZoom = when {
                            hasSavedLocation && route.zoom > 0 -> route.zoom
                            !hasSavedLocation && countryView != null -> countryView.zoom
                            route.zoom > 0 -> route.zoom
                            else -> DEFAULT_INITIAL_ZOOM
                        }
                        mv.mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(initial)
                                .zoom(initialZoom)
                                .build()
                        )
                        annotationManager = mv.annotations.createPointAnnotationManager()

                        // Taps work even when the style or tiles failed to load
                        mv.mapboxMap.addOnMapClickListener { point ->
                            mv.mapboxMap.flyTo(
                                CameraOptions.Builder().center(point).build(),
                                MapAnimationOptions.mapAnimationOptions { duration(MAP_CLICK_FLY_DURATION_MS) }
                            )
                            viewModel.selectPoint(point.latitude(), point.longitude())
                            true
                        }
                        mapView = mv
                        loadMapStyle(mv)
                    }
                },
            )

            // Status and place information, stacked so they never overlap at large font sizes
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isLocating || state.isLoadingCoverage) {
                    StatusCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(
                                    if (state.isLocating) R.string.lbl_locating else R.string.lbl_coverage_loading
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                state.locationProblem?.let { problem ->
                    LocationProblemCard(
                        problem = problem,
                        onTurnOnLocation = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                            } catch (e: ActivityNotFoundException) {
                                Sentry.captureException(e)
                            }
                        },
                        onRetry = { locateMe() },
                        onTapMapInstead = { viewModel.dismissLocationProblem() },
                    )
                }

                if (mapLoadFailed) {
                    StatusCard(error = true) {
                        Column {
                            Text(
                                text = stringResource(R.string.lbl_map_load_failed),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            TextButton(onClick = { mapView?.let { loadMapStyle(it) } }) {
                                Text(stringResource(R.string.lbl_retry))
                            }
                        }
                    }
                }

                state.addressText?.let { address ->
                    StatusCard {
                        Text(text = address, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                state.weatherData?.let { weather ->
                    StatusCard {
                        Column {
                            Text(
                                text = weatherSummaryFormat.format(
                                    weather.temperature.toInt(),
                                    weather.condition,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = weatherFeelsLikeFormat.format(
                                    weather.feelsLike.toInt(),
                                    weather.humidity,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }

            // Supported-area status and Confirm, stacked at the bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val coverageText = when {
                    state.coverageStatus == EnumCoverageStatus.OUTSIDE -> stringResource(R.string.lbl_coverage_outside)
                    state.coverageStatus == EnumCoverageStatus.INSIDE -> stringResource(R.string.lbl_coverage_inside)
                    state.coverageGeoJson != null -> stringResource(R.string.lbl_coverage_legend)
                    state.coverageUnavailable -> stringResource(R.string.lbl_coverage_unavailable)
                    else -> null
                }
                if (coverageText != null) {
                    val outside = state.coverageStatus == EnumCoverageStatus.OUTSIDE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (outside) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (outside) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Text(
                            text = coverageText,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Button(
                    onClick = {
                        state.selected?.let { sel ->
                            val zoom = mapView?.mapboxMap?.cameraState?.zoom ?: DEFAULT_ZOOM
                            val result = LocationResult(
                                lat = sel.lat,
                                lon = sel.lon,
                                alt = 0.0,
                                zoom = zoom,
                                placeName = state.addressText.orEmpty(),
                            )
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("location_result", result)
                            navController.popBackStackSafely()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    // The API refuses to compute outside the supported area; UNKNOWN is still allowed
                    enabled = state.selected != null && state.coverageStatus != EnumCoverageStatus.OUTSIDE,
                ) {
                    Text(stringResource(R.string.lbl_confirm))
                }
            }
        }
    }
}

@Composable
private fun StatusCard(error: Boolean = false, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (error) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Box(modifier = Modifier.padding(12.dp)) { content() }
    }
}

@Composable
private fun LocationProblemCard(
    problem: LocationProblem,
    onTurnOnLocation: () -> Unit,
    onRetry: () -> Unit,
    onTapMapInstead: () -> Unit,
) {
    StatusCard(error = true) {
        Column {
            Text(
                text = stringResource(
                    when (problem) {
                        LocationProblem.DISABLED -> R.string.lbl_location_disabled
                        LocationProblem.PERMISSION_DENIED -> R.string.lbl_location_permission_denied
                        LocationProblem.FAILED -> R.string.lbl_location_error
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (problem) {
                    LocationProblem.DISABLED -> TextButton(onClick = onTurnOnLocation) {
                        Text(stringResource(R.string.lbl_turn_on_location))
                    }
                    LocationProblem.PERMISSION_DENIED -> TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.lbl_allow_location))
                    }
                    LocationProblem.FAILED -> TextButton(onClick = onRetry) {
                        Text(stringResource(R.string.lbl_retry))
                    }
                }
                TextButton(onClick = onTapMapInstead) {
                    Text(stringResource(R.string.lbl_tap_map_instead))
                }
            }
        }
    }
}

private fun addMarkerImage(context: android.content.Context, style: Style) {
    try {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_location_pin) ?: return
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        style.addImage(MARKER_ICON_ID, bmp)
    } catch (e: Exception) {
        Sentry.captureException(e)
    }
}

/**
 * Adds (or refreshes) the supported area as a green fill with a white-haloed outline. The layers go
 * in the Standard style's "middle" slot: under labels and under the pin, over the satellite image.
 */
private fun drawCoverage(style: Style, geoJson: String) {
    val existing = style.getSourceAs<GeoJsonSource>(COVERAGE_SOURCE_ID)
    if (existing != null) {
        existing.data(geoJson)
        return
    }
    style.addSource(geoJsonSource(COVERAGE_SOURCE_ID) { data(geoJson) })
    style.addLayer(
        fillLayer(COVERAGE_FILL_LAYER_ID, COVERAGE_SOURCE_ID) {
            slot(COVERAGE_SLOT)
            fillColor(COVERAGE_COLOR)
            fillOpacity(COVERAGE_FILL_OPACITY)
        }
    )
    style.addLayer(
        lineLayer(COVERAGE_HALO_LAYER_ID, COVERAGE_SOURCE_ID) {
            slot(COVERAGE_SLOT)
            lineColor(COVERAGE_HALO_COLOR)
            lineWidth(COVERAGE_HALO_WIDTH)
            lineOpacity(COVERAGE_HALO_OPACITY)
        }
    )
    style.addLayer(
        lineLayer(COVERAGE_LINE_LAYER_ID, COVERAGE_SOURCE_ID) {
            slot(COVERAGE_SLOT)
            lineColor(COVERAGE_COLOR)
            lineWidth(COVERAGE_LINE_WIDTH)
        }
    )
}

private fun updateMarker(manager: PointAnnotationManager, point: Point) {
    manager.deleteAll()
    manager.create(
        PointAnnotationOptions()
            .withIconImage(MARKER_ICON_ID)
            .withIconSize(MARKER_ICON_SIZE)
            .withPoint(point)
    )
    // Bounce animation
    val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = BOUNCE_DURATION_MS
        interpolator = BounceInterpolator()
        addUpdateListener { anim ->
            val scale = BOUNCE_SCALE_OFFSET + (anim.animatedValue as Float)
            manager.annotations.forEach { annotation ->
                annotation.iconSize = scale * MARKER_ICON_SIZE
                manager.update(annotation)
            }
        }
    }
    animator.start()
}
