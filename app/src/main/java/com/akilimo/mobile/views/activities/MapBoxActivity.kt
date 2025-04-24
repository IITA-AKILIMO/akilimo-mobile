package com.akilimo.mobile.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.akilimo.mobile.BuildConfig
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.ActivityMapBoxBinding
import com.akilimo.mobile.inherit.BaseLocationPicker
import com.akilimo.mobile.services.GPSTracker
import com.akilimo.mobile.utils.SessionManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

class MapBoxActivity : BaseLocationPicker() {
    var toolbar: Toolbar? = null
    var snackbar: Snackbar? = null
    private var _binding: ActivityMapBoxBinding? = null
    private val binding get() = _binding!!


    private var currentCoordinates: LatLng? = null
    private var currentLat: Double = 0.0
    private var currentLong: Double = 0.0
    private var currentAlt: Double = 0.0

    companion object {
        const val LAT: String = "LAT"
        const val LON: String = "LON"
        const val ALT: String = "ALT"
        const val PLACE_NAME: String = "PLACE_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)
        accessToken = sessionManager.getMapBoxApiKey()
        Mapbox.getInstance(this, accessToken)

        _binding = ActivityMapBoxBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.apply {
            toolbar = toolbarLayout.toolbar
            mapView = mapBox.mapView
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initToolbar()
        initComponent()
    }

    override fun initToolbar() {
        toolbar!!.setNavigationIcon(R.drawable.ic_left_arrow)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_activity_farm_location)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar!!.setNavigationOnClickListener { v: View? -> processActivityResult() }
    }

    override fun initComponent() {
        val extras = intent.extras
        if (extras != null) {
            currentLat = extras.getDouble(LAT)
            currentLong = extras.getDouble(LON)
            currentAlt = extras.getDouble(ALT)
        }

        if (currentLong != 0.0 && currentLat != 0.0) {
            currentCoordinates = LatLng(currentLat, currentLong, currentAlt)
        } else {
            initCurrentLocation()
        }

        snackbar = Snackbar.make(
            binding.coordinatorLayout,
            "Location has been selected. press OK to save and exit",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar!!.setAction("OK") {
            processActivityResult()
        }
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        processActivityResult()
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(
            Style.SATELLITE_STREETS
        ) { style: Style ->
            enableLocationComponent(style)
            Toast.makeText(
                this@MapBoxActivity,
                getString(R.string.move_map_instruction),
                Toast.LENGTH_LONG
            )
                .show()
            initDroppedMarker(style)

            mapboxMap.addOnMapClickListener { latLng: LatLng? ->
                this.currentCoordinates =
                    latLng
                animateMapCameraChange(currentCoordinates)
                // Show the SymbolLayer icon to represent the selected map location
                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                    val source =
                        style.getSourceAs<GeoJsonSource>(DROPPED_MARKER_SOURCE_ID)
                    if (source != null) {
                        source.setGeoJson(
                            Point.fromLngLat(
                                currentCoordinates!!.longitude,
                                currentCoordinates!!.latitude
                            )
                        )
                        val coordinates = String.format(
                            "Lat:%s Lon:%s",
                            currentCoordinates!!.latitude,
                            currentCoordinates!!.longitude
                        )
                        if (snackbar != null) {
                            //                            snackbar.setText(coordinates);
                            snackbar!!.show()
                        }
                    }
                    style.getLayer(DROPPED_MARKER_LAYER_ID)!!
                        .setProperties(PropertyFactory.visibility(Property.VISIBLE))
                }
                true
            }

            //set marker to current location first
            if (currentCoordinates != null) {
                animateMapCameraChange(currentCoordinates)
                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                    val source =
                        style.getSourceAs<GeoJsonSource>(DROPPED_MARKER_SOURCE_ID)
                    source?.setGeoJson(
                        Point.fromLngLat(
                            currentCoordinates!!.longitude,
                            currentCoordinates!!.latitude
                        )
                    )
                    style.getLayer(DROPPED_MARKER_LAYER_ID)!!
                        .setProperties(PropertyFactory.visibility(Property.VISIBLE))
                }
            }
        }
    }

    private fun animateMapCameraChange(latLng: LatLng?) {
        val newCameraPosition = CameraPosition.Builder().target(latLng)
            .zoom((if (BuildConfig.DEBUG) 5 else 17).toDouble()).build()
        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
    }

    private fun initCurrentLocation() {
        val gps = GPSTracker(this@MapBoxActivity)
        gps.getLocation()
        if (gps.canGetLocation()) {
            val status =
                GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(this@MapBoxActivity)
            if (status == ConnectionResult.SUCCESS) {
                currentLong = gps.getLongitude()
                currentLat = gps.getLatitude()
            }
            gps.stopUsingGPS()
        } else {
            gps.showSettingsAlert()
        }
        currentCoordinates = LatLng(currentLat, currentLong, currentAlt)
    }

    private fun processActivityResult() {
        if (currentCoordinates != null) {
            val intent = Intent()
            intent.putExtra(LAT, currentCoordinates!!.latitude)
            intent.putExtra(LON, currentCoordinates!!.longitude)
            intent.putExtra(ALT, currentCoordinates!!.altitude)
            intent.putExtra(PLACE_NAME, placeName)
            setResult(RESULT_OK, intent)
            closeActivity(false)
        }
    }
}
