package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.iita.akilimo.R;
import com.iita.akilimo.entities.MandatoryInfo;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
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

    @BindView(R.id.title)
    AppCompatTextView title;

    private ProfileInfo profileInfo;
    private MandatoryInfo mandatoryInfo;
    private EnumCountries countryEnum = EnumCountries.OTHERS;
    private String name = "";

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
        return inflater.inflate(R.layout.fragment_country, container, false);
    }

    @Override
    public void refreshData() {
        profileInfo = objectBoxEntityProcessor.getProfileInfo();
        mandatoryInfo = objectBoxEntityProcessor.getMandatoryInfo();
        if (profileInfo != null) {
            name = profileInfo.getFirstName();
        }
        if (mandatoryInfo != null) {
            countryEnum = mandatoryInfo.getCountryEnum();
            switch (countryEnum) {
                case NIGERIA:
                    rdgCountry.check(R.id.rdNg);
                    break;
                case TANZANIA:
                    rdgCountry.check(R.id.rdTz);
                    break;
            }
        }


        String message = context.getString(R.string.lbl_country_location, name);
        title.setText(message);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //save this data
        rdgCountry.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdNg:
                    countryEnum = EnumCountries.NIGERIA;
                    break;
                case R.id.rdTz:
                    countryEnum = EnumCountries.TANZANIA;
                    break;
                default:
                    countryEnum = EnumCountries.OTHERS;
                    break;
            }
            if (mandatoryInfo == null) {
                mandatoryInfo = new MandatoryInfo();
            }

            mandatoryInfo.setCountryCode(countryEnum.countryCode());
            mandatoryInfo.setCountryName(countryEnum.countryName());
            mandatoryInfo.setCurrency(countryEnum.currency());
            mandatoryInfo.setCountryEnum(countryEnum);
            objectBoxEntityProcessor.saveMandatoryInfo(mandatoryInfo);
        });
    }

}
