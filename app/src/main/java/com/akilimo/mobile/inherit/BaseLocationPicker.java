package com.akilimo.mobile.inherit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.crashlytics.android.Crashlytics;
import com.akilimo.mobile.R;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public abstract class BaseLocationPicker extends BaseActivity implements OnMapReadyCallback {

    protected static final String TAG = BaseLocationPicker.class.getSimpleName();
    protected static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    protected static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";
    protected static final String DROPPED_MARKER_SOURCE_ID = "DROPPED_MARKER_SOURCE_ID";
    protected static final String IMAGE_NAME = "DROPPED-ICON-IMAGE";
    protected String accessToken;
    protected String placeName;

    protected MapView mapView;
    protected MapboxMap mapboxMap;

    protected CarmenFeature home;
    protected CarmenFeature work;
    protected String geojsonSourceLayerId = "geojsonSourceLayerId";
    protected String symbolIconId = "symbolIconId";

    protected void initDroppedMarker(@NonNull Style loadedMapStyle) {
        // Add the marker image to map
//        loadedMapStyle.addImage(IMAGE_NAME, ContextCompat.getDrawable(this, R.drawable.ic_location_on));
        loadedMapStyle.addImage(IMAGE_NAME, BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));
        loadedMapStyle.addSource(new GeoJsonSource(DROPPED_MARKER_SOURCE_ID));
        loadedMapStyle.addLayer(new SymbolLayer(DROPPED_MARKER_LAYER_ID, DROPPED_MARKER_SOURCE_ID)
                .withProperties(
                        iconImage(IMAGE_NAME),
                        visibility(NONE),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                ));
    }

    /**
     * This method is used to reverse geocode where the user has dropped the marker.
     *
     * @param style style
     * @param point The location to use for the search
     */

    protected void reverseGeocode(@NonNull final Style style, final Point point) {
        try {
            String[] geoCodingTypes = new String[]{
                    GeocodingCriteria.TYPE_NEIGHBORHOOD,
                    GeocodingCriteria.TYPE_COUNTRY,
                    GeocodingCriteria.TYPE_LOCALITY,
                    GeocodingCriteria.TYPE_ADDRESS,
                    GeocodingCriteria.TYPE_DISTRICT,
                    GeocodingCriteria.TYPE_PLACE,
                    GeocodingCriteria.TYPE_COUNTRY,
            };

            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken(accessToken)
                    .query(Point.fromLngLat(point.longitude(), point.latitude()))
                    .geocodingTypes(geoCodingTypes)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {
                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            CarmenFeature feature = results.get(0);
                            // If the geo-coder returns a result, we take the first in the list and show a Toast with the place name.
                            if (style.isFullyLoaded() && style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                                placeName = feature.placeName();
                                Toast.makeText(context, String.format(getString(R.string.location_picker_place_name_result), feature.placeName()), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Crashlytics.log("Mapbox geocoding failure");
                    Crashlytics.logException(throwable);
                }
            });
        } catch (ServicesException servicesException) {
            Crashlytics.log("Mapbox issue happening");
            Crashlytics.logException(servicesException);
        }
    }

    @SuppressLint("MissingPermission")
    protected void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.NORMAL);
        } else {
            Toast.makeText(context, "I am unable to enable the location component", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
