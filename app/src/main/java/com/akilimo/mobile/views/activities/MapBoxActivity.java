package com.akilimo.mobile.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.ActivityMapBoxBinding;
import com.akilimo.mobile.inherit.BaseLocationPicker;
import com.akilimo.mobile.services.GPSTracker;
import com.akilimo.mobile.utils.SessionManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MapBoxActivity extends BaseLocationPicker {

    public static final String LAT = "LAT";
    public static final String LON = "LON";
    public static final String ALT = "ALT";
    public static final String PLACE_NAME = "PLACE_NAME";

    FloatingActionButton btnSelectLocation;
    Toolbar toolbar;
    ActivityMapBoxBinding binding;


    LatLng currentCoordinates;
    double currentLat;
    double currentLong;
    double currentAlt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MapBoxActivity.this;
        sessionManager = new SessionManager(this);
        accessToken = sessionManager.getMapBoxApiKey();
        Mapbox.getInstance(this, accessToken);

        binding = ActivityMapBoxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        toolbar = binding.toolbarLayout.toolbar;
        btnSelectLocation = binding.btnGetLocation;
        mapView = binding.mapBox.mapView;

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.title_activity_farm_location));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> processActivityResult());

    }

    @Override
    protected void initComponent() {
        //check if activity has extra values
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentLat = extras.getDouble(LAT);
            currentLong = extras.getDouble(LON);
            currentAlt = extras.getDouble(ALT);
        }

        if (currentLong != 0 && currentLat != 0) {
            currentCoordinates = new LatLng(currentLat, currentLong, currentAlt);
        } else {
            initCurrentLocation();
        }
    }

    @Override
    protected void validate(boolean backPressed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBackPressed() {
        processActivityResult();
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.SATELLITE_STREETS, style -> {
            enableLocationComponent(style);
            Toast.makeText(context, getString(R.string.move_map_instruction), Toast.LENGTH_LONG).show();
            initDroppedMarker(style);

            btnSelectLocation.setOnClickListener(view -> {
                processActivityResult();
            });

            mapboxMap.addOnMapClickListener(latLng -> {
                this.currentCoordinates = latLng;
                animateMapCameraChange(currentCoordinates);
                // Show the SymbolLayer icon to represent the selected map location
                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                    GeoJsonSource source = style.getSourceAs(DROPPED_MARKER_SOURCE_ID);
                    if (source != null) {
                        source.setGeoJson(Point.fromLngLat(currentCoordinates.getLongitude(), currentCoordinates.getLatitude()));
                        String coordinates = String.format("Lat:%s Lon:%s", currentCoordinates.getLatitude(), currentCoordinates.getLongitude());
                        Toast.makeText(context, coordinates, Toast.LENGTH_LONG).show();
                    }
                    style.getLayer(DROPPED_MARKER_LAYER_ID).setProperties(visibility(VISIBLE));
                }

                // Use the map camera target's coordinates to make a reverse geocoding search
//                reverseGeocode(style, Point.fromLngLat(currentCoordinates.getLongitude(), currentCoordinates.getLatitude()));

                return true;
            });

            //set marker to current location first
            if (currentCoordinates != null) {
                animateMapCameraChange(currentCoordinates);
                if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {
                    GeoJsonSource source = style.getSourceAs(DROPPED_MARKER_SOURCE_ID);
                    if (source != null) {
                        source.setGeoJson(Point.fromLngLat(currentCoordinates.getLongitude(), currentCoordinates.getLatitude()));
                    }
                    style.getLayer(DROPPED_MARKER_LAYER_ID).setProperties(visibility(VISIBLE));
                }
            }
        });
    }

    private void animateMapCameraChange(LatLng latLng) {
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .build();
        if (mapboxMap != null) {
            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
        }
    }

    private void initCurrentLocation() {
        GPSTracker gps = new GPSTracker(context);
        gps.getLocation();
        if (gps.canGetLocation()) {
            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this.context);
            if (status == ConnectionResult.SUCCESS) {
                currentLong = gps.getLongitude();
                currentLat = gps.getLatitude();
            }
            gps.stopUsingGPS();
        } else {
            gps.showSettingsAlert();
        }
        currentCoordinates = new LatLng(currentLat, currentLong, currentAlt);
    }

    private void processActivityResult() {
        if (currentCoordinates != null) {
            Intent intent = new Intent();
            intent.putExtra(LAT, currentCoordinates.getLatitude());
            intent.putExtra(LON, currentCoordinates.getLongitude());
            intent.putExtra(ALT, currentCoordinates.getAltitude());
            intent.putExtra(PLACE_NAME, placeName);
            setResult(Activity.RESULT_OK, intent);
            closeActivity(false);
        }
    }

}
