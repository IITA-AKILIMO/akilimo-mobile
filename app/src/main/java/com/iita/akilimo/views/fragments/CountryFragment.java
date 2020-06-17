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

import io.realm.Realm;

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
            profileInfo = realmProcessor.getProfileInfo();
            mandatoryInfo = realmProcessor.getMandatoryInfo();
            if (profileInfo != null) {
                name = profileInfo.getFirstName();
            }
            if (mandatoryInfo != null) {
                selectedCountryIndex = mandatoryInfo.getSelectedCountryIndex();
                countryCode = mandatoryInfo.getCountryCode();
                countrySpinner.setSelection(selectedCountryIndex);
            }
        } catch (Exception ex) {
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
            countries.add(EnumCountry.TANZANIA.name());
            countryImages.add(World.getFlagOf(countryCode));
        } else {
            countries.add(EnumCountry.NIGERIA.name());
            countryImages.add(World.getFlagOf(EnumCountry.NIGERIA.countryCode()));
            countries.add(EnumCountry.TANZANIA.name());
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
                        countryName = EnumCountry.KENYA.name();
                        currency = EnumCountry.KENYA.currency();
                        countryCode = EnumCountry.KENYA.countryCode();
                        break;
                    case "tanzania":
                        countryName = EnumCountry.TANZANIA.name();
                        currency = EnumCountry.TANZANIA.currency();
                        countryCode = EnumCountry.TANZANIA.countryCode();
                        break;
                    case "nigeria":
                        countryName = EnumCountry.NIGERIA.name();
                        currency = EnumCountry.NIGERIA.currency();
                        countryCode = EnumCountry.NIGERIA.countryCode();
                        break;
                    default:
                        countryName = EnumCountry.OTHERS.name();
                        currency = EnumCountry.OTHERS.currency();
                        countryCode = EnumCountry.OTHERS.countryCode();
                        break;
                }
                updateSelectedCountry(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updateSelectedCountry(int selectedCountryIndex) {
        try (Realm myRealm = getRealmInstance()) {
            myRealm.executeTransaction(realm -> {
                if (mandatoryInfo == null) {
                    mandatoryInfo = myRealm.createObject(MandatoryInfo.class);
                }

                mandatoryInfo.setSelectedCountryIndex(selectedCountryIndex);
                mandatoryInfo.setCountryCode(countryCode);
                mandatoryInfo.setCountryName(countryName);
                mandatoryInfo.setCurrency(currency);
            });
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

    }

}
