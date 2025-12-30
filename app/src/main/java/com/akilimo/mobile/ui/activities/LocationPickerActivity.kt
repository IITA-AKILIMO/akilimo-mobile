package com.akilimo.mobile.ui.activities

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityLocationPickerBinding
import com.akilimo.mobile.utils.GeocodingService
import com.akilimo.mobile.utils.LocationHelper
import com.akilimo.mobile.utils.PermissionHelper
import com.akilimo.mobile.utils.WeatherService
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import io.sentry.Sentry
import kotlinx.coroutines.launch

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

    private var isStyleLoaded = false
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
        com.mapbox.common.MapboxOptions.accessToken = sessionManager.mapBoxApiKey
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

        mapboxMap.loadStyle(Style.STANDARD_SATELLITE) { style ->
            addCustomMarkerToStyle(style)
            setupAnnotations()
            setupMapClickListener()
            isStyleLoaded = true
        }
    }

    private fun addCustomMarkerToStyle(style: Style) {
        try {
            val markerBitmap = bitmapFromDrawableRes(
                this,
                R.drawable.ic_location_pin
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
                putExtra(PLACE_NAME, binding.tvAddress.text.toString())
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        } ?: run {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchAndUseCurrentLocation() {
        if (!isStyleLoaded) return

        lifecycleScope.launch {
            when (val result = locationHelper.getCurrentLocation(this@LocationPickerActivity)) {
                is LocationHelper.LocationResult.Success -> {
                    val loc = result.location
                    val point = Point.fromLngLat(loc.longitude, loc.latitude)

                    mapboxMap.flyTo(
                        CameraOptions.Builder()
                            .center(point)
                            .zoom(DEFAULT_ZOOM_LEVEL)
                            .build(),
                        com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                            duration(1500)
                        }
                    )

                    selectedPoint = point
                    updateMarker(point)
                    enableMapboxLocationComponent()
                    fetchLocationDetails(point)
                }

                is LocationHelper.LocationResult.LocationDisabled ->
                    showLocationServicesDialog()

                is LocationHelper.LocationResult.PermissionDenied ->
                    requestLocationPermission()

                is LocationHelper.LocationResult.Error ->
                    Toast.makeText(this@LocationPickerActivity, result.message, Toast.LENGTH_SHORT)
                        .show()
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
            fetchLocationDetails(point)

            mapboxMap.flyTo(
                CameraOptions.Builder().center(point).build(),
                com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                    duration(1000)
                }
            )
            true
        }
    }


    private fun fetchLocationDetails(point: Point) {
        // Address card is always relevant
        binding.addressCard.visibility = View.GONE
        // Weather is optional
        binding.weatherCard.visibility = View.GONE

        fetchAddress(point)
        fetchWeather(point)
    }


    private fun fetchAddress(point: Point) {
        val geocodingService =
            GeocodingService(
                this@LocationPickerActivity,
                sessionManager.locationIqToken
            )
        lifecycleScope.launch {
            geocodingService.fetchAddressFlow(point.latitude(), point.longitude())
                .collect { result ->
                    when (result) {
                        is GeocodingService.GeocodingResult.Success -> {
                            updateAddressUI(result.data.formattedAddress)
                        }

                        is GeocodingService.GeocodingResult.Error -> {
                            updateAddressUI(result.fallbackMessage)
                        }
                    }
                }
        }
    }


    private fun fetchWeather(point: Point) {
        val weatherService = WeatherService(this@LocationPickerActivity)

        lifecycleScope.launch {
            weatherService.fetchWeatherFlow(point.latitude(), point.longitude())
                .collect { result ->
                    when (result) {
                        is WeatherService.WeatherResult.Success -> {
                            updateWeatherUI(result.data)
                            binding.weatherCard.visibility = View.VISIBLE
                        }

                        is WeatherService.WeatherResult.Error -> {
                            binding.weatherCard.visibility = View.GONE
                        }
                    }
                }
        }
    }


    private fun updateAddressUI(addressText: String) = with(binding) {
        tvAddress.text = addressText
        addressCard.visibility = View.VISIBLE
    }

    private fun updateWeatherUI(weather: WeatherService.WeatherData) = with(binding) {
        weatherCard.visibility = View.VISIBLE
        tvTemperature.text = "${weather.temperature.toInt()}°"
        tvWeatherDescription.text = weather.condition
        tvFeelsLike.text = "Feels like ${weather.feelsLike.toInt()}°C"
        tvHumidity.text = "${weather.humidity}%"
        tvWindSpeed.text = String.format("%.1f m/s", weather.windSpeed)
    }

    private fun updateMarker(point: Point) {
        if (!::pointAnnotationManager.isInitialized) {
            setupAnnotations()
        }
        pointAnnotationManager.deleteAll()

        val pointAnnotationOptions = PointAnnotationOptions()
            .withIconImage(MARKER_ICON_ID)
            .withIconSize(1.5)
            .withPoint(point)

        pointAnnotationManager.create(pointAnnotationOptions)
        animateMarker()
    }

    private fun animateMarker() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 600
        animator.interpolator = BounceInterpolator()

        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            val scale = 0.5f + (animatedValue * 1.0f)

            pointAnnotationManager.annotations.forEach { annotation ->
                annotation.iconSize = scale * 1.5
                pointAnnotationManager.update(annotation)
            }
        }

        animator.start()
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
}