package com.akilimo.mobile.views.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentCountryBinding;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.enums.EnumCountry;
import com.blongho.country_data.World;
import com.stepstone.stepper.VerificationError;

import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;

;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CountryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountryFragment extends BaseStepFragment {

    AppCompatTextView title;
    AppCompatButton btnPickCountry;
    ImageView countryImage;
    AppCompatTextView txtCountryName;
    FragmentCountryBinding binding;


    private ProfileInfo profileInfo;
    private String name = "";
    private String selectedLanguage = "";
    private int selectedCountryIndex = -1;

    private String[] countries = new String[]{EnumCountry.Burundi.name(), EnumCountry.Ghana.name(), EnumCountry.Nigeria.name(), EnumCountry.Tanzania.name(),
//            EnumCountry.Rwanda.name(),
    };

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

    public void refreshData() {
        try {
            profileInfo = database.profileInfoDao().findOne();
            if (profileInfo != null) {
                name = profileInfo.getFirstName();
                countryCode = profileInfo.getCountryCode();
                currency = profileInfo.getCurrency();
                countryName = profileInfo.getCountryName();
                currency = profileInfo.getCurrency();
                selectedLanguage = profileInfo.getLanguage();

                if (countryCode != null && !countryCode.isEmpty()) {
                    selectedCountryIndex = profileInfo.getSelectedCountryIndex();
                    countryImage.setImageResource(World.getFlagOf(countryCode));
                }
                if (countryName != null && !countryName.isEmpty()) {
                    txtCountryName.setText(countryName);
                }
            }

            String message = context.getString(R.string.lbl_country_location, name);
            title.setText(message);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Sentry.captureException(ex);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = binding.title;
        btnPickCountry = binding.btnPickCountry;
        txtCountryName = binding.countryName;
        countryImage = binding.countryImage;

        btnPickCountry.setOnClickListener(pickerDialog -> {
            if (selectedLanguage.equalsIgnoreCase("sw")) {
                countries = new String[]{EnumCountry.Tanzania.name()};
            }


            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.lbl_pick_your_country));
            builder.setSingleChoiceItems(countries, selectedCountryIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectedCountryIndex = i;
                }
            });

            Map<String, EnumCountry> countryMap = new HashMap<>();
            countryMap.put("kenya", EnumCountry.Kenya);
            countryMap.put("tanzania", EnumCountry.Tanzania);
            countryMap.put("nigeria", EnumCountry.Nigeria);
            countryMap.put("ghana", EnumCountry.Ghana);
            countryMap.put("rwanda", EnumCountry.Rwanda);
            countryMap.put("burundi", EnumCountry.Burundi);


            builder.setPositiveButton(context.getString(R.string.lbl_ok), (dialogInterface, whichButton) -> {
                if (selectedCountryIndex >= 0 && countries.length > 0) {
                    countryName = countries[selectedCountryIndex];
                    EnumCountry selectedCountry = countryMap.get(countryName.toLowerCase());
                    if (selectedCountry == null) {
                        selectedCountry = EnumCountry.Other;
                    }

                    countryName = selectedCountry.name();
                    currency = selectedCountry.currency();
                    countryCode = selectedCountry.countryCode();
                    countryImage.setImageResource(World.getFlagOf(countryCode));
                    txtCountryName.setText(countryName);
                    dialogInterface.dismiss();
                    updateSelectedCountry();
                }
            });
            builder.setNegativeButton(context.getString(R.string.lbl_cancel), ((dialogInterface, i) -> {
                dialogInterface.dismiss();
            }));

            AlertDialog dialog = builder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        });
    }

    private void updateSelectedCountry() {
        try {
            if (profileInfo == null) {
                profileInfo = new ProfileInfo();
            }

            profileInfo.setSelectedCountryIndex(selectedCountryIndex);
            profileInfo.setCountryCode(countryCode);
            profileInfo.setCountryName(countryName);
            profileInfo.setCurrency(currency);

            dataIsValid = !TextUtils.isEmpty(countryCode);
            if (profileInfo.getProfileId() != null) {
                int id = profileInfo.getProfileId();
                if (id > 0) {
                    database.profileInfoDao().update(profileInfo);
                }
            } else {
                database.profileInfoDao().insert(profileInfo);
            }
            sessionManager.setCountry(countryCode);
        } catch (Exception ex) {
            dataIsValid = false;
            Sentry.captureException(ex);
        }
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        updateSelectedCountry();
        if (!dataIsValid) {
            return new VerificationError("Please select country");
        }
        return null;
    }

    @Override
    public void onSelected() {
        refreshData();
    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
