package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
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

import com.blongho.country_data.World;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentCountryBinding;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.utils.enums.EnumCountry;
import com.jakewharton.processphoenix.ProcessPhoenix;

;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CountryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CountryFragment extends BaseFragment {

    AppCompatTextView title;
    AppCompatButton btnPickCountry;
    ImageView countryImage;
    AppCompatTextView txtCountryName;
    FragmentCountryBinding binding;


    private ProfileInfo profileInfo;
    private String name = "";

    boolean userSelect = false;
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
            profileInfo = database.profileInfoDao().findOne();
            if (profileInfo != null) {
                name = profileInfo.getFirstName();
                selectedCountryIndex = profileInfo.getSelectedCountryIndex();
                countryCode = profileInfo.getCountryCode();
                countryName = profileInfo.getCountryName();

                if (!Strings.isEmptyOrWhitespace(countryCode)) {
                    countryImage.setImageResource(World.getFlagOf(countryCode));
                }
                if (!Strings.isEmptyOrWhitespace(countryName)) {
                    txtCountryName.setText(countryName);
                }
            }

            String message = context.getString(R.string.lbl_country_location, name);
            title.setText(message);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = binding.title;
        btnPickCountry = binding.btnPickCountry;
        txtCountryName = binding.countryName;
        countryImage = binding.countryImage;

        final String[] countries = new String[]{
                EnumCountry.Nigeria.name(),
                EnumCountry.Tanzania.name()
        };

        btnPickCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.lbl_pick_your_country));
                builder.setSingleChoiceItems(countries, selectedCountryIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        countryName = countries[i];
                        selectedCountryIndex = i;
                        switch (countryName.toLowerCase()) {
                            case "kenya":
                                countryName = EnumCountry.Kenya.name();
                                currency = EnumCountry.Kenya.currency();
                                countryCode = EnumCountry.Kenya.countryCode();
                                break;
                            case "tanzania":
                                countryName = EnumCountry.Tanzania.name();
                                currency = EnumCountry.Tanzania.currency();
                                countryCode = EnumCountry.Tanzania.countryCode();
                                break;
                            case "nigeria":
                                countryName = EnumCountry.Nigeria.name();
                                currency = EnumCountry.Nigeria.currency();
                                countryCode = EnumCountry.Nigeria.countryCode();
                                break;
                            default:
                                countryName = EnumCountry.Other.name();
                                currency = EnumCountry.Other.currency();
                                countryCode = EnumCountry.Other.countryCode();
                                break;
                        }

                        countryImage.setImageResource(World.getFlagOf(countryCode));
                        txtCountryName.setText(countryName);
                        updateSelectedCountry();
                        dialogInterface.dismiss();
                    }
                });

                // Create the alert dialog
                AlertDialog dialog = builder.create();

                // Finally, display the alert dialog
                dialog.show();
            }
        });
    }

    private void updateSelectedCountry() {
        if (profileInfo == null) {
            profileInfo = new ProfileInfo();
        }

        profileInfo.setSelectedCountryIndex(selectedCountryIndex);
        profileInfo.setCountryCode(countryCode);
        profileInfo.setCountryName(countryName);
        profileInfo.setCurrency(currency);

        if (profileInfo.getProfileId() != null) {
            int id = profileInfo.getProfileId();
            if (id > 0) {
                database.profileInfoDao().update(profileInfo);
            }
        } else {
            database.profileInfoDao().insert(profileInfo);
        }
    }
}
