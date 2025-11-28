package com.akilimo.mobile.ui.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import com.akilimo.mobile.base.BaseActivity
import com.akilimo.mobile.databinding.ActivityLocationPickerBinding
import com.akilimo.mobile.helper.SessionManager
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style

class LocationPickerActivity : BaseActivity<ActivityLocationPickerBinding>(), OnMapReadyCallback {

    companion object {
        const val LAT: String = "LAT"
        const val LON: String = "LON"
        const val ALT: String = "ALT"
        const val ZOOM: String = "ZOOM"
        const val PLACE_NAME: String = "PLACE_NAME"
    }

    private lateinit var mapboxMap: MapboxMap
    private var selectedLatLng: LatLng? = null

    override fun attachBaseContext(newBase: Context) {
        val session = SessionManager(newBase)
        Mapbox.getInstance(newBase, session.mapBoxApiKey)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState, persistentState)
    }


    override fun inflateBinding() = ActivityLocationPickerBinding.inflate(layoutInflater)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(this)

        binding.btnConfirmLocation.setOnClickListener {
            val cameraPosition = mapboxMap.cameraPosition
            val zoom = cameraPosition.zoom
            if (selectedLatLng != null) {
                val resultIntent = Intent().apply {
                    putExtra(LAT, selectedLatLng?.latitude)
                    putExtra(LON, selectedLatLng?.longitude)
                    putExtra(ALT, selectedLatLng?.altitude)
                    putExtra(ZOOM, zoom)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        val lat = intent.getDoubleExtra(LAT, 0.0)
        val lng = intent.getDoubleExtra(LON, 0.0)
        val zoom = intent.getDoubleExtra(ZOOM, 12.0)
        val initialLatLng = if (lat != 0.0 || lng != 0.0) {
            LatLng(lat, lng)
        } else {
            LatLng(-1.2921, 36.8219) // Default to Nairobi
        }

        mapboxMap.setStyle(Style.SATELLITE_STREETS) { style ->
            enableLocationComponent(style, initialLatLng, zoom)

            mapboxMap.addOnMapClickListener { point ->
                selectedLatLng = point
                mapboxMap.clear()
                mapboxMap.addMarker(MarkerOptions().position(point))
                true
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun enableLocationComponent(style: Style, initialLatLng: LatLng, zoom: Double) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, style).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS

            locationComponent.lastKnownLocation?.let {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, zoom))
            }
        }
    }

    // Forward lifecycle events to MapView
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        binding.mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        binding.mapView.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
    }
}