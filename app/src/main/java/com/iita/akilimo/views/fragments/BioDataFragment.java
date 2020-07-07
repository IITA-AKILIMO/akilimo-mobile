package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentBioDataBinding;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.ValidationHelper;

import java.util.ArrayList;
import java.util.List;

;

/**
 * A simple {@link Fragment} subclass.
 */
public class BioDataFragment extends BaseFragment {

    private String LOG_TAG = BioDataFragment.class.getSimpleName();
    private IFragmentCallBack fragmentCallBack;
    private ProfileInfo profileInfo;
    private ValidationHelper validationHelper;


    Spinner genderSpinner;
    TextInputLayout lytFirstName;
    TextInputLayout lytLastName;
    TextInputLayout lytFarmName;
    TextInputLayout lytEmail;
    TextInputLayout lytPhone;
    TextInputEditText edtPhone;
    CountryCodePicker ccp;
    AppCompatButton btnGetRec;

    FragmentBioDataBinding binding;


    private boolean dataIsValid;
    private String firstName;
    private String lastName;
    private String email;
    private String farmName;
    private String mobileCode;
    private String fullMobileNumber;
    private String gender;
    private int selectedGenderIndex = -1;

    public BioDataFragment() {
        // Required empty public constructor
    }

    public static BioDataFragment newInstance() {
        return new BioDataFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        validationHelper = new ValidationHelper();
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBioDataBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        genderSpinner = binding.genderSpinner;
        lytFirstName = binding.lytFirstName;
        lytLastName = binding.lytLastName;
        lytFarmName = binding.lytFarmName;
        lytEmail = binding.lytEmail;
        lytPhone = binding.lytPhone;
        edtPhone = binding.edtPhone;
        ccp = binding.ccp;
        btnGetRec = binding.singleButton.btnGetRecommendation;


        btnGetRec.setText(getString(R.string.lbl_save));

        final List<String> genderStrings = new ArrayList<>();
        genderStrings.add(this.getString(R.string.lbl_male));
        genderStrings.add(this.getString(R.string.lbl_female));

        final SpinnerAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, genderStrings);
        genderSpinner.setAdapter(adapter);

        btnGetRec.setOnClickListener(view1 -> saveBioData());

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                gender = genderStrings.get(position);
                selectedGenderIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            dataIsValid = true;
            if (!isValidNumber) {
                lytPhone.setError(this.getString(R.string.lbl_valid_number_req));
            } else {
                lytPhone.setError(null);
            }
        });

        ccp.setOnCountryChangeListener(() -> mobileCode = ccp.getSelectedCountryCodeWithPlus());
        ccp.registerCarrierNumberEditText(edtPhone);

    }

    @Override
    public void refreshData() {
        try {

            profileInfo = database.profileInfoDao().findOne();
            if (profileInfo != null) {
                firstName = profileInfo.getFirstName();
                lastName = profileInfo.getLastName();
                farmName = profileInfo.getFarmName();
                email = profileInfo.getEmail();
                mobileCode = profileInfo.getMobileCode();
                fullMobileNumber = profileInfo.getFullMobileNumber();
                gender = profileInfo.getGender();
                selectedGenderIndex = profileInfo.getSelectedGenderIndex();

                lytFirstName.getEditText().setText(firstName);
                lytLastName.getEditText().setText(lastName);
                lytFarmName.getEditText().setText(farmName);
                lytEmail.getEditText().setText(email);
                ccp.setFullNumber(fullMobileNumber);

                genderSpinner.setSelection(selectedGenderIndex);
            }

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred getting BioDataInfo info");
            Crashlytics.logException(ex);
        }
    }

    private void saveBioData() {
        lytFirstName.setError(null);
        lytLastName.setError(null);
        lytFarmName.setError(null);
        lytEmail.setError(null);
        dataIsValid = true;

        firstName = lytFirstName.getEditText().getText().toString();
        lastName = lytLastName.getEditText().getText().toString();
        farmName = lytFarmName.getEditText().getText().toString();
        email = lytEmail.getEditText().getText().toString();
        fullMobileNumber = ccp.getFullNumber();
        mobileCode = ccp.getSelectedCountryCodeWithPlus();

        if (Strings.isEmptyOrWhitespace(firstName)) {
            dataIsValid = false;
            lytFirstName.setError(this.getString(R.string.lbl_first_name_req));
        }

        if (Strings.isEmptyOrWhitespace(lastName)) {
            dataIsValid = false;
            lytLastName.setError(this.getString(R.string.lbl_last_name_req));
        }

        if (Strings.isEmptyOrWhitespace(farmName)) {
            //dataIsValid = false;
            //lytFarmName.setError(this.getString(R.string.lbl_last_name_req));
            farmName = String.format("%s%s", firstName, lastName);
            lytFarmName.getEditText().setText(farmName);
        }

        if (!validationHelper.isValidEmail(email) && !Strings.isEmptyOrWhitespace(email)) {
            dataIsValid = false;
            lytEmail.setError(this.getString(R.string.lbl_valid_email_req));
        }

        if (dataIsValid) {
            try {
                if (profileInfo == null) {
                    profileInfo = new ProfileInfo();
                }
                profileInfo.setFirstName(firstName);
                profileInfo.setLastName(lastName);
                profileInfo.setGender(gender);
                profileInfo.setEmail(email);
                profileInfo.setFarmName(farmName);
                profileInfo.setFieldDescription(farmName);
                profileInfo.setMobileCode(mobileCode);
                profileInfo.setFullMobileNumber(fullMobileNumber);
                profileInfo.setSelectedGenderIndex(selectedGenderIndex);
                if (sessionManager != null) {
                    profileInfo.setDeviceToken(sessionManager.getDeviceToken());
                }

                profileInfo.setUserName(profileInfo.getNames());

                int pk = 0;
                if (profileInfo.getProfileId() != null) {
                    pk = profileInfo.getProfileId();
                }
                if (pk > 0) {
                    database.profileInfoDao().update(profileInfo);
                } else {
                    database.profileInfoDao().insert(profileInfo);
                }
                nextFragment();
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
                Crashlytics.logException(ex);
            }

        }
    }

    public void setOnFragmentCloseListener(IFragmentCallBack callBack) {
        this.fragmentCallBack = callBack;
    }

    private void nextFragment() {
        if (fragmentCallBack != null) {
            fragmentCallBack.onDataSaved();
        }
    }
}
