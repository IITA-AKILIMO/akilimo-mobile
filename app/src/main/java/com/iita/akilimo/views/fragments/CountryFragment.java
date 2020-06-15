package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.blongho.country_data.World;
import com.crashlytics.android.Crashlytics;
import com.iita.akilimo.R;
import com.iita.akilimo.adapters.MySpinnerAdapter;
import com.iita.akilimo.databinding.FragmentCountryBinding;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.utils.enums.EnumCountry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CountryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountryFragment extends BaseFragment {

    AppCompatTextView title;
    Spinner countrySpinner;
    FragmentCountryBinding binding;

    private ProfileInfo profileInfo;
    private MandatoryInfo mandatoryInfo;
    private EnumCountry countryEnum = EnumCountry.OTHERS;
    private String name = "";

    private int selectedCountryIndex = -1;


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
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCountryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void refreshData() {
        try {
            profileInfo = objectBoxEntityProcessor.getProfileInfo();
            mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
            if (profileInfo != null) {
                name = profileInfo.getFirstName();
            }
            if (mandatoryInfo != null) {
                selectedCountryIndex = mandatoryInfo.getSelectedCountryIndex();
                countryEnum = mandatoryInfo.getCountryEnum();
                countrySpinner.setSelection(selectedCountryIndex);
            }
        } catch (Exception ex) {
            mandatoryInfo = new MandatoryInfo();
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred fetching info");
            Crashlytics.logException(ex);
        }

        String message = context.getString(R.string.lbl_country_location, name);
        title.setText(message);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = binding.title;
        countrySpinner = binding.countrySpinner;

        //save this data

        final List<String> countries = new ArrayList<>();
        final List<Integer> countryImages = new ArrayList<>();

        //let us get the current locale and limit countries to that locale
        Locale currentLocale = getCurrentLocale();
        if (currentLocale.getCountry().equalsIgnoreCase(EnumCountry.TANZANIA.countryCode())) {
            countries.add(EnumCountry.TANZANIA.countryName());
            countryImages.add(World.getFlagOf(EnumCountry.TANZANIA.countryCode()));
        } else {
            countries.add(EnumCountry.NIGERIA.countryName());
            countryImages.add(World.getFlagOf(EnumCountry.NIGERIA.countryCode()));
            countries.add(EnumCountry.TANZANIA.countryName());
            countryImages.add(World.getFlagOf(EnumCountry.TANZANIA.countryCode()));
        }

        final MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(context, countries, countryImages);
        countrySpinner.setAdapter(spinnerAdapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountryIndex = position;
                String selectedCountry = countries.get(position).toLowerCase();
                switch (selectedCountry) {
                    case "kenya":
                        countryEnum = EnumCountry.KENYA;
                        break;
                    case "tanzania":
                        countryEnum = EnumCountry.TANZANIA;
                        break;
                    case "nigeria":
                        countryEnum = EnumCountry.NIGERIA;
                        break;
                    default:
                        countryEnum = EnumCountry.OTHERS;
                        break;
                }
                updateSelectedCountry(countryEnum, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateSelectedCountry(EnumCountry countryEnum, int selectedCountryIndex) {
        mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (mandatoryInfo == null) {
            mandatoryInfo = new MandatoryInfo();
        }

        mandatoryInfo.setSelectedCountryIndex(selectedCountryIndex);
        mandatoryInfo.setCountryCode(countryEnum.countryCode());
        mandatoryInfo.setCountryName(countryEnum.countryName());
        mandatoryInfo.setCurrency(countryEnum.currency());
        mandatoryInfo.setCountryEnum(countryEnum);
        objectBoxEntityProcessor.saveMandatoryInfo(mandatoryInfo);
    }

}
