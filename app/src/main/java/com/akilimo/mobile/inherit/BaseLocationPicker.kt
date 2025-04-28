package com.akilimo.mobile.inherit

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import com.akilimo.mobile.R
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.geocoding.v5.GeocodingCriteria
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.core.exceptions.ServicesException
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import io.sentry.Sentry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class BaseLocationPicker : BaseActivity(), OnMapReadyCallback {

    protected val TAG: String = BaseLocationPicker::class.java.simpleName
    protected val REQUEST_CODE_AUTOCOMPLETE = 1
    protected val DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID"
    protected val DROPPED_MARKER_SOURCE_ID = "DROPPED_MARKER_SOURCE_ID"
    protected val IMAGE_NAME = "DROPPED-ICON-IMAGE"

    protected var accessToken: String? = null
    protected var placeName: String? = null

    protected lateinit var mapView: MapView
    protected lateinit var mapboxMap: MapboxMap

    protected var home: CarmenFeature? = null
    protected var work: CarmenFeature? = null
    protected var geojsonSourceLayerId: String = "geojsonSourceLayerId"
    protected var symbolIconId: String = "symbolIconId"

    protected fun initDroppedMarker(loadedMapStyle: Style) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default)
        loadedMapStyle.addImage(IMAGE_NAME, bitmap)
        loadedMapStyle.addSource(GeoJsonSource(DROPPED_MARKER_SOURCE_ID))
        loadedMapStyle.addLayer(
            SymbolLayer(DROPPED_MARKER_LAYER_ID, DROPPED_MARKER_SOURCE_ID).withProperties(
                PropertyFactory.iconImage(IMAGE_NAME),
                PropertyFactory.visibility(Property.NONE),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true)
            )
        )
    }

    protected fun reverseGeocode(style: Style, point: Point, context: Context) {
        try {
            val geoCodingTypes = arrayOf(
                GeocodingCriteria.TYPE_NEIGHBORHOOD,
                GeocodingCriteria.TYPE_COUNTRY,
                GeocodingCriteria.TYPE_LOCALITY,
                GeocodingCriteria.TYPE_ADDRESS,
                GeocodingCriteria.TYPE_DISTRICT,
                GeocodingCriteria.TYPE_PLACE,
                GeocodingCriteria.TYPE_COUNTRY
            )

            val client = MapboxGeocoding.builder()
                .accessToken(accessToken!!)
                .query(Point.fromLngLat(point.longitude(), point.latitude()))
                .geocodingTypes(*geoCodingTypes)
                .build()

            client.enqueueCall(object : Callback<GeocodingResponse> {
                override fun onResponse(
                    call: Call<GeocodingResponse>,
                    response: Response<GeocodingResponse>
                ) {
                    response.body()?.features()?.firstOrNull()?.let { feature ->
                        if (style.isFullyLoaded && style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                            placeName = feature.placeName()
                            Toast.makeText(
                                context,
                                getString(
                                    R.string.location_picker_place_name_result,
                                    feature.placeName()
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                    Sentry.captureException(t)
                }
            })
        } catch (ex: ServicesException) {
            Sentry.captureException(ex)
        }
    }

    @SuppressLint("MissingPermission")
    protected fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(this, loadedMapStyle).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.NORMAL
        } else {
            Toast.makeText(
                this@BaseLocationPicker,
                "I am unable to enable the location component",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
