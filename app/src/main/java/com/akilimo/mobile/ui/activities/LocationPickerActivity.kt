package com.akilimo.mobile.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityLocationPickerBinding
import com.akilimo.mobile.helper.SessionManager
import com.akilimo.mobile.utils.LocationHelper
import com.akilimo.mobile.utils.PermissionHelper
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import io.sentry.Sentry
import kotlinx.coroutines.launch
import com.akilimo.mobile.R

class LocationPickerActivity : BaseActivity<ActivityLocationPickerBinding>() {

    companion object {
        const val LAT: String = "LAT"
        const val LON: String = "LON"
        const val ALT: String = "ALT"
        const val ZOOM: String = "ZOOM"
        const val PLACE_NAME: String = "PLACE_NAME"

        private const val DEFAULT_ZOOM_LEVEL = 15.0
        private const val MARKER_ICON_ID = "custom-marker-icon"
    }

    private lateinit var mapboxMap: MapboxMap
    private var selectedPoint: Point? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var locationHelper: LocationHelper

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        handlePermissionResult(permissions)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        val session = SessionManager(newBase)
        com.mapbox.common.MapboxOptions.accessToken = session.mapBoxApiKey
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    override fun inflateBinding() = ActivityLocationPickerBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        initializeHelpers()
        setupMap()
        setupListeners()
        checkAndRequestLocationPermission()
    }

    private fun initializeHelpers() {
        permissionHelper = PermissionHelper()
        locationHelper = LocationHelper()
    }

    private fun setupMap() {
        mapboxMap = binding.mapView.mapboxMap

        val lat = intent.getDoubleExtra(LAT, 0.0)
        val lng = intent.getDoubleExtra(LON, 0.0)
        val zoom = intent.getDoubleExtra(ZOOM, 12.0)

        val initialPoint = if (isValidLocation(lat, lng)) {
            Point.fromLngLat(lng, lat)
        } else {
            Point.fromLngLat(36.8219, -1.2921) // Default to Nairobi
        }

        mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(initialPoint)
                .zoom(zoom)
                .build()
        )

        mapboxMap.loadStyle(Style.SATELLITE_STREETS) { style ->
            addCustomMarkerToStyle(style)
            setupAnnotations()
            setupMapClickListener()
        }
    }

    private fun addCustomMarkerToStyle(style: Style) {
        try {
            // Convert vector drawable to bitmap
            val markerBitmap = bitmapFromDrawableRes(
                this,
                R.drawable.ic_location_pin // Replace with your vector drawable resource
            )

            markerBitmap?.let {
                style.addImage(MARKER_ICON_ID, it)
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
            Toast.makeText(this, "Failed to load marker icon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapFromDrawableRes(context: Context, resourceId: Int): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, resourceId) ?: return null

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            bitmap
        } catch (e: Exception) {
            Sentry.captureException(e)
            null
        }
    }

    private fun setupListeners() {
        binding.btnConfirmLocation.setOnClickListener {
            handleLocationConfirmation()
        }
    }

    private fun handleLocationConfirmation() {
        selectedPoint?.let { point ->
            val cameraState = mapboxMap.cameraState
            val resultIntent = Intent().apply {
                putExtra(LAT, point.latitude())
                putExtra(LON, point.longitude())
                putExtra(ALT, point.altitude().takeUnless { it.isNaN() } ?: 0.0)
                putExtra(ZOOM, cameraState.zoom)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } ?: run {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAndUseCurrentLocation() {
        lifecycleScope.launch {
            when (val locationResult =
                locationHelper.getCurrentLocation(this@LocationPickerActivity)) {
                is LocationHelper.LocationResult.Success -> {
                    val location = locationResult.location
                    val currentPoint = Point.fromLngLat(location.longitude, location.latitude)

                    mapboxMap.setCamera(
                        CameraOptions.Builder()
                            .center(currentPoint)
                            .zoom(DEFAULT_ZOOM_LEVEL)
                            .build()
                    )

                    selectedPoint = currentPoint
                    updateMarker(currentPoint)
                    enableMapboxLocationComponent()

                    Toast.makeText(
                        this@LocationPickerActivity,
                        "Location updated to your current position",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is LocationHelper.LocationResult.Error -> {
                    Toast.makeText(
                        this@LocationPickerActivity,
                        locationResult.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is LocationHelper.LocationResult.LocationDisabled -> {
                    showLocationServicesDialog()
                }

                is LocationHelper.LocationResult.PermissionDenied -> {
                    requestLocationPermission()
                }
            }
        }
    }

    private fun setupAnnotations() {
        val annotationApi = binding.mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
    }

    private fun setupMapClickListener() {
        mapboxMap.addOnMapClickListener { point ->
            selectedPoint = point
            updateMarker(point)

            Toast.makeText(
                this,
                "Location selected: ${String.format("%.6f", point.latitude())}, " +
                        String.format("%.6f", point.longitude()),
                Toast.LENGTH_SHORT
            ).show()

            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(point)
                    .build()
            )

            true
        }
    }

    private fun updateMarker(point: Point) {
        pointAnnotationManager.deleteAll()

        val pointAnnotationOptions = PointAnnotationOptions()
            .withIconImage(MARKER_ICON_ID)
            .withIconSize(1.5) // Adjust size as needed
            .withPoint(point)

        pointAnnotationManager.create(pointAnnotationOptions)
    }

    private fun checkAndRequestLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            enableMapboxLocationComponent()
            return
        }

        when {
            permissionHelper.hasLocationPermission(this) -> {
                enableMapboxLocationComponent()
                fetchAndUseCurrentLocation()
            }

            permissionHelper.shouldShowPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                showPermissionRationale()
            }

            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun showPermissionRationale() {
        Toast.makeText(
            this,
            "Location permission is needed to show your current location",
            Toast.LENGTH_LONG
        ).show()
        requestLocationPermission()
    }

    private fun handlePermissionResult(permissions: Map<String, Boolean>) {
        if (permissions.any { it.value }) {
            enableMapboxLocationComponent()
            fetchAndUseCurrentLocation()
        } else {
            Toast.makeText(
                this,
                "Location permission denied. You can still select a location manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun enableMapboxLocationComponent() {
        if (!permissionHelper.hasLocationPermission(this)) {
            return
        }

        try {
            val locationPlugin = binding.mapView.location
            locationPlugin.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun showLocationServicesDialog() {
        Toast.makeText(
            this,
            "Please enable location services to use this feature",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isValidLocation(lat: Double, lng: Double): Boolean {
        return lat != 0.0 || lng != 0.0
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}