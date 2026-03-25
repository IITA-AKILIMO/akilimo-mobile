package com.akilimo.mobile.ui.screens.settings

import android.Manifest
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Parcelable
import android.view.animation.BounceInterpolator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.navigation.LocationPickerRoute
import com.akilimo.mobile.ui.viewmodels.LocationPickerViewModel
import com.akilimo.mobile.network.LocationHelper
import com.akilimo.mobile.utils.PermissionHelper
import com.mapbox.common.MapboxOptions
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
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
private const val MARKER_ICON_ID = "custom-marker"

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
    val permissionHelper = remember { PermissionHelper() }
    val weatherSummaryFormat = stringResource(R.string.lbl_weather_summary)
    val weatherFeelsLikeFormat = stringResource(R.string.lbl_weather_feels_like)

    // Mapbox Map references managed via remember
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var selectedPoint by remember { mutableStateOf<Point?>(null) }
    var annotationManager by remember { mutableStateOf<PointAnnotationManager?>(null) }
    var styleLoaded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms.any { it.value }) {
            viewModel.fetchCurrentLocation()
        }
    }

    // React to location results from the VM
    LaunchedEffect(state.locationResult) {
        val result = state.locationResult ?: return@LaunchedEffect
        val mv = mapView ?: return@LaunchedEffect
        if (result is LocationHelper.LocationResult.Success) {
            val loc = result.location
            val point = Point.fromLngLat(loc.longitude, loc.latitude)
            mv.mapboxMap.flyTo(
                CameraOptions.Builder().center(point).zoom(DEFAULT_ZOOM).build(),
                MapAnimationOptions.mapAnimationOptions { duration(1500) }
            )
            selectedPoint = point
            annotationManager?.let { updateMarker(it, point) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.lbl_farm_location)) })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Mapbox map via AndroidView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapboxOptions.accessToken = BuildConfig.MAPBOX_RUNTIME_TOKEN
                    MapView(ctx).also { mv ->
                        mapView = mv
                        val initial = if (route.lat != 0.0 || route.lon != 0.0) {
                            Point.fromLngLat(route.lon, route.lat)
                        } else {
                            Point.fromLngLat(36.8219, -1.2921) // default: Nairobi
                        }
                        mv.mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(initial)
                                .zoom(if (route.zoom > 0) route.zoom else 12.0)
                                .build()
                        )
                        mv.mapboxMap.loadStyle(Style.STANDARD_SATELLITE) { style ->
                            // Add marker icon
                            try {
                                val drawable = ContextCompat.getDrawable(ctx, R.drawable.ic_location_pin)
                                if (drawable != null) {
                                    val bmp = Bitmap.createBitmap(
                                        drawable.intrinsicWidth,
                                        drawable.intrinsicHeight,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    val canvas = Canvas(bmp)
                                    drawable.setBounds(0, 0, canvas.width, canvas.height)
                                    drawable.draw(canvas)
                                    style.addImage(MARKER_ICON_ID, bmp)
                                }
                            } catch (e: Exception) {
                                Sentry.captureException(e)
                            }

                            // Setup annotations and click listener
                            val am = mv.annotations.createPointAnnotationManager()
                            annotationManager = am

                            mv.mapboxMap.addOnMapClickListener { point ->
                                selectedPoint = point
                                updateMarker(am, point)
                                mv.mapboxMap.flyTo(
                                    CameraOptions.Builder().center(point).build(),
                                    MapAnimationOptions.mapAnimationOptions { duration(1000) }
                                )
                                viewModel.fetchAddress(point.latitude(), point.longitude())
                                viewModel.fetchWeather(point.latitude(), point.longitude())
                                true
                            }
                            styleLoaded = true

                            // Request location permission or use current location
                            if (permissionHelper.hasLocationPermission(ctx)) {
                                try {
                                    mv.location.updateSettings {
                                        enabled = true
                                        pulsingEnabled = true
                                    }
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
                        }
                    }
                },
            )

            // Address card
            state.addressText?.let { address ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = address,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Weather card
            state.weatherData?.let { weather ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = if (state.addressText != null) 90.dp else 16.dp),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
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

            // Confirm button
            Button(
                onClick = {
                    val point = selectedPoint
                    if (point != null) {
                        val mv = mapView
                        val zoom = mv?.mapboxMap?.cameraState?.zoom ?: DEFAULT_ZOOM
                        val result = LocationResult(
                            lat = point.latitude(),
                            lon = point.longitude(),
                            alt = point.altitude().takeUnless { it.isNaN() } ?: 0.0,
                            zoom = zoom,
                            placeName = state.addressText.orEmpty(),
                        )
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("location_result", result)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                enabled = selectedPoint != null,
            ) {
                Text(stringResource(R.string.lbl_confirm))
            }
        }
    }
}

private fun updateMarker(manager: PointAnnotationManager, point: Point) {
    manager.deleteAll()
    manager.create(
        PointAnnotationOptions()
            .withIconImage(MARKER_ICON_ID)
            .withIconSize(1.5)
            .withPoint(point)
    )
    // Bounce animation
    val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 600
        interpolator = BounceInterpolator()
        addUpdateListener { anim ->
            val scale = 0.5f + (anim.animatedValue as Float)
            manager.annotations.forEach { annotation ->
                annotation.iconSize = scale * 1.5
                manager.update(annotation)
            }
        }
    }
    animator.start()
}
