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
import androidx.fragment.app.Fragment;


import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.hbb20.CountryCodePicker;
import com.iita.akilimo.R;
import com.iita.akilimo.databinding.FragmentBioDataBinding;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.ValidationHelper;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.List;

;

/**
 * A simple {@link Fragment} subclass.
 */
public class BioDataFragment extends BaseStepFragment {

    private String LOG_TAG = BioDataFragment.class.getSimpleName();
    private IFragmentCallBack fragmentCallBack;
    private ProfileInfo profileInfo;
    private ValidationHelper validationHelper;
    private boolean phoneIsValid = true;

    FragmentBioDataBinding binding;
    Spinner genderSpinner;
    TextInputEditText edtFirstName;
    TextInputEditText edtLastName;
    TextInputEditText edtFamName;
    TextInputEditText edtEmail;
    TextInputEditText edtPhone;
    CountryCodePicker ccp;

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
        edtFirstName = binding.edtFirstName;
        edtLastName = binding.edtLastName;
        edtFamName = binding.edtFarmName;
        edtEmail = binding.edtEmail;
        edtPhone = binding.edtPhone;
        ccp = binding.ccp;

        final List<String> genderStrings = new ArrayList<>();
        genderStrings.add(this.getString(R.string.lbl_female));
        genderStrings.add(this.getString(R.string.lbl_male));
        genderStrings.add(this.getString(R.string.lbl_prefer_not_to_say));

        final SpinnerAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, genderStrings);
        genderSpinner.setAdapter(adapter);

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
            phoneIsValid = isValidNumber;
        });

        ccp.setOnCountryChangeListener(() -> mobileCode = ccp.getSelectedCountryCodeWithPlus());
        ccp.registerCarrierNumberEditText(edtPhone);

    }

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

                edtFirstName.setText(firstName);
                edtLastName.setText(lastName);
                edtFamName.setText(farmName);
                edtEmail.setText(email);
                if (!Strings.isEmptyOrWhitespace(fullMobileNumber)) {
                    ccp.setFullNumber(fullMobileNumber);
                }

                genderSpinner.setSelection(selectedGenderIndex);
            }

        } catch (Exception ex) {
            FirebaseCrashlytics.getInstance().log(ex.getMessage());
            FirebaseCrashlytics.getInstance().recordException(ex);
        }
    }

    private void saveBioData() {
        edtFirstName.setError(null);
        edtLastName.setError(null);
        edtFamName.setError(null);
        edtEmail.setError(null);
        dataIsValid = true;

        firstName = edtFirstName.getText().toString();
        lastName = edtLastName.getText().toString();
        farmName = edtFamName.getText().toString();
        email = edtEmail.getText().toString();
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
            edtFamName.setText(farmName);
        }

        if (!Strings.isEmptyOrWhitespace(fullMobileNumber)) {
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

                if (profileInfo.getProfileId() != null) {
                    database.profileInfoDao().update(profileInfo);
                } else {
                    database.profileInfoDao().insert(profileInfo);
                }
            } catch (Exception ex) {
                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                FirebaseCrashlytics.getInstance().log(ex.getMessage());
                FirebaseCrashlytics.getInstance().recordException(ex);
            }

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
