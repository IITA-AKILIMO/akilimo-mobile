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
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.button.MaterialButton;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
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


    private double currentLat;
    private double currentLon;
    private MandatoryInfo location;

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

        btnSelectLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapBoxActivity.class);
                getActivity().startActivityForResult(intent, HomeActivity.MAP_BOX_PLACE_PICKER_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadLocationInfo();
    }

    private void saveLocation() {
        location = objectBoxEntityProcessor.getMandatoryInfo();
        if (location == null) {
            location = new MandatoryInfo();
        }
        location.setLatitude(currentLat);
        location.setLongitude(currentLon);
        objectBoxEntityProcessor.saveMandatoryInfo(location);
        reloadLocationInfo();
    }

    private void reloadLocationInfo() {
        location = objectBoxEntityProcessor.getMandatoryInfo();
        locationInfo.setText(loadLocationInfo(location));
    }
}
