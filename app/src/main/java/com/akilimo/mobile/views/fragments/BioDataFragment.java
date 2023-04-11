package com.akilimo.mobile.views.fragments;


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
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.hbb20.CountryCodePicker;
import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentBioDataBinding;
import com.akilimo.mobile.entities.ProfileInfo;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.interfaces.IFragmentCallBack;
import com.akilimo.mobile.utils.ValidationHelper;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BioDataFragment extends BaseStepFragment {
    private ProfileInfo profileInfo;
    private ValidationHelper validationHelper;
    private boolean phoneIsValid = true;

    FragmentBioDataBinding binding;
    Spinner genderSpinner, interestSpinner;
    TextInputEditText edtFirstName;
    TextInputEditText edtLastName;
    //    TextInputEditText edtFamName;
    TextInputEditText edtEmail;
    TextInputEditText edtPhone;
    CountryCodePicker ccp;

    private String firstName;
    private String lastName;
    private String email;
    private String farmName;
    private String mobileCode;
    private String fullMobileNumber;
    private String userEnteredNumber;
    private String gender, akilimoInterest;
    private int selectedGenderIndex = -1;
    private int selectedInterestIndex = -1;

    private boolean rememberUserInfo = false;

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
        interestSpinner = binding.interestSpinner;
        edtFirstName = binding.edtFirstName;
        edtLastName = binding.edtLastName;
//        edtFamName = binding.edtFarmName;
        edtEmail = binding.edtEmail;
        edtPhone = binding.edtPhone;
        ccp = binding.ccp;

        final List<String> genderStrings = new ArrayList<>();
        genderStrings.add(this.getString(R.string.lbl_female));
        genderStrings.add(this.getString(R.string.lbl_male));
        genderStrings.add(this.getString(R.string.lbl_prefer_not_to_say));

        final SpinnerAdapter genderAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, genderStrings);
        genderSpinner.setAdapter(genderAdapter);

        final List<String> interestStrings = new ArrayList<>();
        interestStrings.add(this.getString(R.string.lbl_interest_farmer));
        interestStrings.add(this.getString(R.string.lbl_interest_extension_agent));
        interestStrings.add(this.getString(R.string.lbl_interest_agronomist));
        interestStrings.add(this.getString(R.string.lbl_interest_curious));
        final SpinnerAdapter interestAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, interestStrings);
        interestSpinner.setAdapter(interestAdapter);


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

        interestSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                akilimoInterest = interestStrings.get(position);
                selectedInterestIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            phoneIsValid = isValidNumber;
        });

        ccp.setOnCountryChangeListener(() -> mobileCode = ccp.getSelectedCountryCodeWithPlus());
        ccp.registerCarrierNumberEditText(edtPhone);

        binding.chkRememberDetails.setOnCheckedChangeListener((compoundButton, rememberInfo) -> {
            rememberUserInfo = rememberInfo;
        });

    }

    public void refreshData() {
        try {

            profileInfo = database.profileInfoDao().findOne();
            rememberUserInfo = sessionManager.userInfoRemembered();
            if (profileInfo != null) {
                firstName = profileInfo.getFirstName();
                lastName = profileInfo.getLastName();
                farmName = profileInfo.getFarmName();
                email = profileInfo.getEmail();
                mobileCode = profileInfo.getMobileCode();
                fullMobileNumber = profileInfo.getFullMobileNumber();
                gender = profileInfo.getGender();
                akilimoInterest = profileInfo.getAkilimoInterest();
                selectedGenderIndex = profileInfo.getSelectedGenderIndex();
                selectedInterestIndex = profileInfo.getSelectedInterestIndex();

                edtFirstName.setText(firstName);
                edtLastName.setText(lastName);
//                edtFamName.setText(farmName);
                edtEmail.setText(email);
                if (!Strings.isEmptyOrWhitespace(fullMobileNumber)) {
                    ccp.setFullNumber(fullMobileNumber);
                }

                genderSpinner.setSelection(selectedGenderIndex);
                binding.chkRememberDetails.setChecked(rememberUserInfo);
            }

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
    }

    private void saveBioData() {
        edtFirstName.setError(null);
        edtLastName.setError(null);
//        edtFamName.setError(null);
        edtEmail.setError(null);
        dataIsValid = true;

        firstName = edtFirstName.getText().toString();
        lastName = edtLastName.getText().toString();
//        farmName = edtFamName.getText().toString();
        email = edtEmail.getText().toString().trim();
        userEnteredNumber = edtPhone.getText().toString();
        fullMobileNumber = ccp.getFullNumber();
        mobileCode = ccp.getSelectedCountryCodeWithPlus();

        if (Strings.isEmptyOrWhitespace(firstName)) {
            dataIsValid = false;
            errorMessage = this.getString(R.string.lbl_first_name_req);
            edtFirstName.setError(errorMessage);
        }

        if (Strings.isEmptyOrWhitespace(lastName)) {
            dataIsValid = false;
            errorMessage = this.getString(R.string.lbl_last_name_req);
            edtLastName.setError(errorMessage);
        }

        if (Strings.isEmptyOrWhitespace(farmName)) {
            farmName = String.format("%s%s", firstName, lastName);
        }

        if (!Strings.isEmptyOrWhitespace(fullMobileNumber) && !Strings.isEmptyOrWhitespace(userEnteredNumber)) {
            if (!phoneIsValid) {
                dataIsValid = false;
                errorMessage = this.getString(R.string.lbl_valid_number_req);
                edtPhone.setError(errorMessage);
            } else {
                edtPhone.setError(null);
            }
        }

        if (!validationHelper.isValidEmail(email) && !Strings.isEmptyOrWhitespace(email)) {
            dataIsValid = false;
            errorMessage = this.getString(R.string.lbl_valid_email_req);
            edtEmail.setError(errorMessage);
        }

        if (!dataIsValid) {
            return;
        }

        try {
            if (profileInfo == null) {
                profileInfo = new ProfileInfo();
            }
            profileInfo.setFirstName(firstName);
            profileInfo.setLastName(lastName);
            profileInfo.setGender(gender);
            profileInfo.setAkilimoInterest(akilimoInterest);
            profileInfo.setEmail(email);
            profileInfo.setFarmName(farmName);
            profileInfo.setFieldDescription(farmName);
            profileInfo.setMobileCode(mobileCode);
            profileInfo.setFullMobileNumber(fullMobileNumber);
            profileInfo.setSelectedGenderIndex(selectedGenderIndex);
            profileInfo.setSelectedInterestIndex(selectedInterestIndex);
            if (sessionManager != null) {
                profileInfo.setDeviceToken(sessionManager.getDeviceToken());
            }

            profileInfo.setUserName(profileInfo.getNames());

            if (profileInfo.getProfileId() != null) {
                database.profileInfoDao().update(profileInfo);
            } else {
                database.profileInfoDao().insert(profileInfo);
            }
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Crashlytics.log(Log.ERROR, LOG_TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }

    }


    @Nullable
    @Override
    public VerificationError verifyStep() {
        saveBioData();
        if (!dataIsValid) {
            return new VerificationError(errorMessage);
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
