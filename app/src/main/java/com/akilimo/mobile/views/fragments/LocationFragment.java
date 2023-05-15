package com.akilimo.mobile.views.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.akilimo.mobile.rest.MapBoxApi;
import com.akilimo.mobile.rest.MapBoxApiInterface;
import com.akilimo.mobile.rest.response.ReverseGeoCode;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentLocationBinding;
import com.akilimo.mobile.entities.LocationInfo;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.services.GPSTracker;
import com.akilimo.mobile.utils.enums.EnumCountry;
import com.akilimo.mobile.views.activities.MapBoxActivity;
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
    private String userSelectedCountryCode;
    private String placeName;
    private boolean countrySupported;

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

        ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            currentLat = data.getDoubleExtra(MapBoxActivity.LAT, 0.0);
                            currentLon = data.getDoubleExtra(MapBoxActivity.LON, 0.0);
                            currentAlt = data.getDoubleExtra(MapBoxActivity.ALT, 0.0);
                            reverseGeoCode(currentLat, currentLon);
                        } else {
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception ex) {
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    //@TODO migrate crashlytics code to latest crash analytics platform
                }
            }
        });

        btnSelectLocation.setOnClickListener(v -> {
//            Intent intent = new Intent(getActivity(), MapSelectionActivity.class);
            Intent intent = new Intent(getActivity(), MapBoxActivity.class);
            intent.putExtra(MapBoxActivity.LAT, currentLat);
            intent.putExtra(MapBoxActivity.LON, currentLon);
            intent.putExtra(MapBoxActivity.ALT, currentAlt);
//            this.startActivityForResult(intent, HomeStepperActivity.MAP_BOX_PLACE_PICKER_REQUEST_CODE);
            mStartForResult.launch(intent);

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

    @SuppressLint("LogNotTimber")
    private void reverseGeoCodeNew(double lat, double lon) {


        MapBoxApiInterface mapBoxApiInterface = MapBoxApi.create();

        Call<ReverseGeoCode> result = mapBoxApiInterface.reverseGeoCode(lon, lat, "place", MAP_BOX_ACCESS_TOKEN);
        result.enqueue(new Callback<ReverseGeoCode>() {
            @Override
            public void onResponse(@NonNull Call<ReverseGeoCode> call, @NonNull Response<ReverseGeoCode> response) {
                Log.d("TAG", response.code() + "");
                ReverseGeoCode data = response.body();
            }

            @Override
            public void onFailure(@NonNull Call<ReverseGeoCode> call, @NonNull Throwable t) {
                errorMessage = t.getMessage();
            }
        });
    }

    private void reverseGeoCode(double lat, double lon) {
        MapboxGeocoding reverseGeocode = MapboxGeocoding
                .builder().accessToken(MAP_BOX_ACCESS_TOKEN)
                .query(Point.fromLngLat(lon, lat))
                .fuzzyMatch(true)
//                .geocodingTypes(GeocodingCriteria.TYPE_PLACE)
                .build();

        //https://api.mapbox.com/geocoding/v5/{endpoint}/{longitude},{latitude}.json
        //https://api.mapbox.com/geocoding/v5/mapbox.places/39.326888,-3.384999.json?access_token=pk.eyJ1IjoibWFzZ2VlayIsImEiOiJjanp0bm43ZmwwNm9jM29udjJod3V6dzB1In0.MevkJtANWZ8Wl9abnLu1Uw
        reverseGeocode.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(@NotNull Call<GeocodingResponse> call, @NotNull Response<GeocodingResponse> response) {
                if (response.body() != null) {
                    List<CarmenFeature> features = response.body().features();
                    placeName = "NA";
                    countryLocation = "NA";
                    if (!features.isEmpty()) {
                        int featureSize = features.size();
                        CarmenFeature carmenFeature = features.get(featureSize - 1);
                        countryLocation = carmenFeature.properties().get("short_code").getAsString();
                        placeName = carmenFeature.placeName();
                        saveLocation();
                    } else {
                        showCustomWarningDialog("Unable to save location please pick a different location");
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<GeocodingResponse> call, @NotNull Throwable throwable) {
                saveLocation();
                Crashlytics.log(Log.ERROR, LOG_TAG, throwable.getMessage());
                Crashlytics.logException(throwable);
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
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        reloadLocationInfo();
    }

    private void reloadLocationInfo() {
        try {
            profileInfo = database.profileInfoDao().findOne();
            locationInformation = database.locationInfoDao().findOne();

            if (profileInfo != null) {
                farmName = profileInfo.getFarmName();
                userSelectedCountryCode = profileInfo.getCountryCode();
            }
            if (locationInformation != null) {
                StringBuilder locInfo = loadLocationInfo(locationInformation);
                currentLon = locationInformation.getLongitude();
                currentLat = locationInformation.getLatitude();
                currentAlt = locationInformation.getAltitude();
                countryLocation = locationInformation.getLocationCountry();
                locationInfo.setText(locInfo.toString());
                isSupportedCountry(countryLocation);
            }
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
        }

    }

    private void isSupportedCountry(String countryLocation) {
        if (countryLocation == null && userSelectedCountryCode == null) {
            countrySupported = false;
            return;
        }
        countrySupported = countryLocation.equalsIgnoreCase(userSelectedCountryCode);
        errorMessage = getString(R.string.lbl_country_supported);
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        reverseGeoCode(currentLat, currentLon);
        if (countrySupported) {
            return null;
        }

        //check if country location matches the specified country
        showCustomWarningDialog(String.format(getString(R.string.lbl_unsupported_country), placeName), errorMessage);
        return new VerificationError(errorMessage);
    }

    @Override
    public void onSelected() {
        reloadLocationInfo();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
