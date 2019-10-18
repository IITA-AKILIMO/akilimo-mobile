package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.services.GPSTracker;
import com.iita.akilimo.views.activities.HomeActivity;
import com.iita.akilimo.views.activities.MapBoxActivity;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LocationFragment extends BaseFragment {

    @BindView(R.id.btnCurrentLocation)
    MaterialButton btnCurrentLocation;

    @BindView(R.id.btnSelectLocation)
    MaterialButton btnSelectLocation;

    @BindView(R.id.locationInfo)
    TextView locationInfo;

    @BindView(R.id.title)
    AppCompatTextView title;


    private double currentLat;
    private double currentLon;
    private ProfileInfo profileInfo;
    private MandatoryInfo mandatoryInfo;
    private String farmName = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public LocationFragment() {
        // Required empty public constructor
    }


    public static LocationFragment newInstance() {
        return new LocationFragment();
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void refreshData() {
        reloadLocationInfo();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnCurrentLocation.setOnClickListener(view1 -> {
            GPSTracker gps = new GPSTracker(context);
            gps.getLocation();
            if (gps.canGetLocation()) {
                int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
                if (status == ConnectionResult.SUCCESS) {
                    currentLat = gps.getLatitude();
                    currentLon = gps.getLongitude();
                    gps.stopUsingGPS();
                    saveLocation();
                }
            } else {
                gps.showSettingsAlert();
            }
        });

        btnSelectLocation.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MapBoxActivity.class);
            getActivity().startActivityForResult(intent, HomeActivity.MAP_BOX_PLACE_PICKER_REQUEST_CODE);
        });
    }

    private void saveLocation() {
        profileInfo = objectBoxEntityProcessor.getProfileInfo();
        mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo == null) {
            mandatoryInfo = new MandatoryInfo();
        }
        mandatoryInfo.setLatitude(currentLat);
        mandatoryInfo.setLongitude(currentLon);

        objectBoxEntityProcessor.saveMandatoryInfo(mandatoryInfo);
        reloadLocationInfo();

    }

    private void reloadLocationInfo() {
        profileInfo = objectBoxEntityProcessor.getProfileInfo();
        mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();

        if (profileInfo != null) {
            farmName = profileInfo.getFarmName();
        }
        if (mandatoryInfo != null) {
            locationInfo.setText(loadLocationInfo(mandatoryInfo));
        }

        String message = context.getString(R.string.lbl_farm_location, farmName);
        title.setText(message);

    }
}
