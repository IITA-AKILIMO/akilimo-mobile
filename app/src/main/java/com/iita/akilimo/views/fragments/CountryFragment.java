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
 * Use the {@link CountryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountryFragment extends BaseFragment {

    @BindView(R.id.rdgCountry)
    RadioGroup rdgCountry;
    private MyLocation location;

    public CountryFragment() {
        // Required empty public constructor
    }

    public static CountryFragment newInstance() {
        return new CountryFragment();
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
        return inflater.inflate(R.layout.fragment_country, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //save this data
        rdgCountry.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdNg:
                    countryCode = EnumCountries.NIGERIA.countryCode();
                    countryName = EnumCountries.NIGERIA.countryName();
                    currency = EnumCountries.NIGERIA.currency();
                    break;
                case R.id.rdTz:
                    countryCode = EnumCountries.TANZANIA.countryCode();
                    countryName = EnumCountries.TANZANIA.countryName();
                    currency = EnumCountries.TANZANIA.currency();
                    break;
            }
            location = objectBoxEntityProcessor.getLocation();
            if (location == null) {
                location = new MyLocation();
            }

            location.setCountryCode(countryCode);
            location.setCountryName(countryName);
            location.setCurrency(currency);
            objectBoxEntityProcessor.saveLocationData(location);
        });
    }

}
