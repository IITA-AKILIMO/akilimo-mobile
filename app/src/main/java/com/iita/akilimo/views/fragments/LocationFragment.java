package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentLocationBinding;
import com.iita.akilimo.entities.LocationInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.services.GPSTracker;
import com.iita.akilimo.views.activities.HomeStepperActivity;
import com.iita.akilimo.views.activities.MapBoxActivity;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.stepstone.stepper.VerificationError;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends BaseStepFragment {


    AppCompatButton btnCurrentLocation;
    AppCompatButton btnSelectLocation;
    TextView locationInfo;
    TextView title;
    FragmentLocationBinding binding;


    private double currentLat;
    private double currentLon;
    private double currentAlt;
    private String countryLocation;
    private String placeName;

    private ProfileInfo profileInfo;
    private LocationInfo locationInformation;
    private String farmName = "";
    private String MAP_BOX_ACCESS_TOKEN = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public LocationFragment() {
    }


    public static LocationFragment newInstance() {
        return new LocationFragment();
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = binding.title;
        btnCurrentLocation = binding.btnCurrentLocation;
        btnSelectLocation = binding.btnSelectLocation;
        locationInfo = binding.locationInfo;

        btnCurrentLocation.setOnClickListener(view1 -> {
            getCurrentLocation();
        });

        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapBoxActivity.class);
            intent.putExtra(MapBoxActivity.LAT, currentLat);
            intent.putExtra(MapBoxActivity.LON, currentLon);
            intent.putExtra(MapBoxActivity.ALT, currentAlt);
            this.startActivityForResult(intent, HomeStepperActivity.MAP_BOX_PLACE_PICKER_REQUEST_CODE);
        });
        errorMessage = context.getString(R.string.lbl_location_error);

        MAP_BOX_ACCESS_TOKEN = sessionManager.getMapBoxApiKey();
    }

    private void getCurrentLocation() {
        GPSTracker gps = new GPSTracker(context);
        gps.getLocation();
        if (gps.canGetLocation()) {
            int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
            if (status == ConnectionResult.SUCCESS) {
                currentLat = gps.getLatitude();
                currentLon = gps.getLongitude();
                gps.stopUsingGPS();
                reverseGeoCode(currentLat, currentLon);
            } else {
                showCustomWarningDialog("Google play services not available on your phone", "Google Play unavailable");
            }
        } else {
            gps.showSettingsAlert();
        }
    }

    private void reverseGeoCode(double lat, double lon) {
        MapboxGeocoding reverseGeocode = MapboxGeocoding.builder()
                .accessToken(MAP_BOX_ACCESS_TOKEN)
                .query(Point.fromLngLat(lon, lat))
                .geocodingTypes(GeocodingCriteria.TYPE_COUNTRY)
                .build();

        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {
                if (response.body() != null) {
                    List<CarmenFeature> features = response.body().features();
                    placeName = "Unknown";
                    countryLocation = "Unknown";
                    if (features.size() > 0) {
                        CarmenFeature carmenFeature = response.body().features().get(0);
                        countryLocation = carmenFeature.properties().get("short_code").getAsString();
                        placeName = carmenFeature.placeName();
                    }
                }
                saveLocation();
            }

            @Override
            public void onFailure(@NotNull Call<GeocodingResponse> call, @NotNull Throwable throwable) {
                saveLocation();
                FirebaseCrashlytics.getInstance().log(throwable.getMessage());
                FirebaseCrashlytics.getInstance().recordException(throwable);
            }
        });
    }

    private void saveLocation() {
        try {
            if (locationInformation == null) {
                locationInformation = new LocationInfo();
            }
            locationInformation.setLocationCountry(countryLocation);
            locationInformation.setPlaceName(placeName);
            locationInformation.setLatitude(currentLat);
            locationInformation.setLongitude(currentLon);

            if (locationInformation.getId() != null) {
                database.locationInfoDao().update(locationInformation);
            } else {
                database.locationInfoDao().insert(locationInformation);
            }

            dataIsValid = currentLat != 0 || currentLon != 0;
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
        reloadLocationInfo();
    }

    private void reloadLocationInfo() {
        try {
            profileInfo = database.profileInfoDao().findOne();
            locationInformation = database.locationInfoDao().findOne();

            if (profileInfo != null) {
                farmName = profileInfo.getFarmName();
            }
            if (locationInformation != null) {
                StringBuilder locInfo = loadLocationInfo(locationInformation);
                currentLon = locationInformation.getLongitude();
                currentLat = locationInformation.getLatitude();
                currentAlt = locationInformation.getAltitude();
                dataIsValid = currentLat != 0 || currentLon != 0;
                if (dataIsValid) {
                    locationInfo.setText(locInfo.toString());
                }
            }

            String message = context.getString(R.string.lbl_farm_location, farmName);
            title.setText(message);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == HomeStepperActivity.MAP_BOX_PLACE_PICKER_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        currentLat = data.getDoubleExtra(MapBoxActivity.LAT, 0.0);
                        currentLon = data.getDoubleExtra(MapBoxActivity.LON, 0.0);
                        currentAlt = data.getDoubleExtra(MapBoxActivity.ALT, 0.0);
                        reverseGeoCode(currentLat, currentLon);
                    } else {
                        dataIsValid = false;
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        reverseGeoCode(currentLat, currentLon);
        if (!dataIsValid) {
            return new VerificationError(errorMessage);
        }
        return null;
    }

    @Override
    public void onSelected() {
        reloadLocationInfo();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
