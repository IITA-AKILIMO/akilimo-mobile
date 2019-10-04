package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.models.MyLocation;
import com.iita.akilimo.utils.enums.EnumCountries;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AreaUnitFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AreaUnitFragment extends BaseFragment {


    @BindView(R.id.rdgAreaUnit)
    RadioGroup rdgAreaUnit;

    private String selectedAreaUnit;
    private MyLocation location;

    public AreaUnitFragment() {
        // Required empty public constructor
    }

    public static AreaUnitFragment newInstance() {
        return new AreaUnitFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_area_unit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //save this data
        rdgAreaUnit.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdAcre:
                    selectedAreaUnit = "acre";
                    break;
                case R.id.rdHa:
                    selectedAreaUnit = "ha";
                    break;
                case R.id.rdSqm:
                    selectedAreaUnit = "m2";
                    break;
            }
            location = objectBoxEntityProcessor.getLocation();
            if (location == null) {
                location = new MyLocation();
            }
            location.setAreaUnit(selectedAreaUnit);
            objectBoxEntityProcessor.saveLocationData(location);
        });
    }
}
