package com.iita.akilimo.views.activities;

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
import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseLocationPicker;
import com.iita.akilimo.services.GPSTracker;
import com.iita.akilimo.utils.SessionManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MapBoxActivity extends BaseLocationPicker {

    public static final String LAT = "LAT";
    public static final String LON = "LON";
    public static final String ALT = "ALT";
    public static final String PLACE_NAME = "PLACE_NAME";

    @BindView(R.id.btnGetLocation)
    FloatingActionButton btnSelectLocation;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindString(R.string.title_activity_farm_location)
    String activityTitle;

    LatLng currentCoordinates;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MapBoxActivity.this;
        sessionManager = new SessionManager(this);
        accessToken = sessionManager.getMapBoxApiKey();
        Mapbox.getInstance(this, accessToken);
        setContentView(R.layout.activity_map_box);
        ButterKnife.bind(this);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        initToolbar();
        initComponent();
    }

    @Override
    protected void initToolbar() {
        toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(activityTitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(v -> processActivityResult());

    }

    @Override
    protected void initComponent() {
        initCurrentLocation();
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

        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
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
                currentCoordinates = new LatLng(gps.getLatitude(), gps.getLongitude());
            }
            gps.stopUsingGPS();
        } else {
            gps.showSettingsAlert();
        }
    }

    private void processActivityResult() {
        if (currentCoordinates != null) {
            Intent intent = new Intent();
            intent.putExtra(LAT, currentCoordinates.getLatitude());
            intent.putExtra(LON, currentCoordinates.getLongitude());
            intent.putExtra(ALT, currentCoordinates.getAltitude());
            intent.putExtra(PLACE_NAME, placeName);
            Toast.makeText(context, "Location selected", Toast.LENGTH_LONG).show();
            setResult(Activity.RESULT_OK, intent);
            closeActivity(false);
        }
    }
}
