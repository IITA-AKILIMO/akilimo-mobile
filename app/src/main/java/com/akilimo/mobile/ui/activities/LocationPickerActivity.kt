package com.akilimo.mobile.ui.activities

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.akilimo.mobile.R
import com.mapbox.maps.plugin.animation.flyTo
import org.json.JSONObject
import java.net.URL
import java.util.Locale

class LocationPickerActivity : BaseActivity<ActivityLocationPickerBinding>() {

    companion object {
        const val LAT: String = "LAT"
        const val LON: String = "LON"
        const val ALT: String = "ALT"
        const val ZOOM: String = "ZOOM"
        const val PLACE_NAME: String = "PLACE_NAME"

        private const val DEFAULT_ZOOM_LEVEL = 15.0
        private const val MARKER_ICON_ID = "custom-marker-icon"

        // Get your free API key from https://www.weatherapi.com/
        private const val WEATHER_API_KEY = "4538add05d16412f80a222914252912"
    }

    private var isStyleLoaded = false
    private lateinit var mapboxMap: MapboxMap
    private var selectedPoint: Point? = null
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var locationHelper: LocationHelper
    private var geocoder: Geocoder? = null

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
        geocoder = if (Geocoder.isPresent()) Geocoder(this, Locale.getDefault()) else null
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
            when (val locationResult =
                locationHelper.getCurrentLocation(this@LocationPickerActivity)) {
                is LocationHelper.LocationResult.Success -> {
                    val location = locationResult.location
                    val currentPoint = Point.fromLngLat(location.longitude, location.latitude)

                    mapboxMap.flyTo(
                        CameraOptions.Builder()
                            .center(currentPoint)
                            .zoom(DEFAULT_ZOOM_LEVEL)
                            .build(),
                        com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                            duration(1500)
                        }
                    )

                    selectedPoint = currentPoint
                    updateMarker(currentPoint)
                    enableMapboxLocationComponent()

                    // Fetch address and weather
                    fetchLocationDetails(currentPoint)

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

            // Fetch address and weather for selected location
            fetchLocationDetails(point)

            Toast.makeText(
                this,
                "Location selected: ${String.format("%.6f", point.latitude())}, " +
                        String.format("%.6f", point.longitude()),
                Toast.LENGTH_SHORT
            ).show()

            mapboxMap.flyTo(
                CameraOptions.Builder()
                    .center(point)
                    .build(),
                com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                    duration(1000)
                }
            )

            true
        }
    }

    private fun fetchLocationDetails(point: Point) {
        // Show loading state
        binding.locationInfoCard.visibility = View.VISIBLE
        binding.tvAddress.text = "Loading address..."

        // Fetch both address and weather
        fetchAddress(point)
        fetchWeather(point)
    }

    private fun fetchAddress(point: Point) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val addresses = geocoder?.getFromLocation(point.latitude(), point.longitude(), 1)

                withContext(Dispatchers.Main) {
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressText = buildString {
                            address.thoroughfare?.let { append(it) }
                            if (address.subLocality != null || address.locality != null) {
                                if (isNotEmpty()) append(", ")
                                append(address.subLocality ?: address.locality)
                            }
                            if (address.adminArea != null) {
                                if (isNotEmpty()) append(", ")
                                append(address.adminArea)
                            }
                            if (address.countryName != null) {
                                if (isNotEmpty()) append(", ")
                                append(address.countryName)
                            }
                        }

                        binding.tvAddress.text = addressText.ifEmpty {
                            "Lat: ${String.format("%.6f", point.latitude())}, " +
                                    "Lon: ${String.format("%.6f", point.longitude())}"
                        }
                    } else {
                        binding.tvAddress.text = "Address not found"
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                withContext(Dispatchers.Main) {
                    binding.tvAddress.text = "Unable to fetch address"
                }
            }
        }
    }

    private fun fetchWeather(point: Point) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // WeatherAPI.com - Simple and feature-rich
                val url = "https://api.weatherapi.com/v1/current.json?" +
                        "key=$WEATHER_API_KEY" +
                        "&q=${point.latitude()},${point.longitude()}" +
                        "&aqi=no"

                val response = URL(url).readText()
                val json = JSONObject(response)
                val current = json.getJSONObject("current")

                val temp = current.getDouble("temp_c")
                val condition = current.getJSONObject("condition").getString("text")
                val humidity = current.getInt("humidity")
                val windSpeed = current.getDouble("wind_kph") / 3.6 // Convert kph to m/s
                val feelsLike = current.getDouble("feelslike_c")

                withContext(Dispatchers.Main) {
                    binding.locationInfoCard.visibility = View.VISIBLE
                    binding.tvTemperature.text = "${temp.toInt()}°C"
                    binding.tvWeatherDescription.text = condition
                    binding.tvHumidity.text = "💧 $humidity%"
                    binding.tvWindSpeed.text = "💨 ${String.format("%.1f", windSpeed)} m/s"

                    // Optional: Show "feels like" temperature
                    if (feelsLike != temp) {
                        binding.tvWeatherDescription.text = "$condition • Feels like ${feelsLike.toInt()}°C"
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e)
                withContext(Dispatchers.Main) {
                    binding.locationInfoCard.visibility = View.GONE
                }
            }
        }
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