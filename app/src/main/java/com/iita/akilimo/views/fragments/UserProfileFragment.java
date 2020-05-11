package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.util.Strings;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.hbb20.CountryCodePicker;
import com.iita.akilimo.Locales;
import com.iita.akilimo.R;
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.ValidationHelper;
import com.iita.akilimo.utils.enums.EnumGender;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindString;
import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserProfileFragment extends BaseFragment {

    private String LOG_TAG = UserProfileFragment.class.getSimpleName();
    private IFragmentCallBack fragmentCallBack;
    private ProfileInfo profileInfo;
    private ValidationHelper validationHelper;


    @BindView(R.id.edtFirstName)
    EditText edtFirstName;

    @BindView(R.id.edtLastName)
    EditText edtLastName;

    @BindView(R.id.edtPhone)
    EditText edtPhone;

    @BindView(R.id.edtEmail)
    EditText edtEmail;

    @BindView(R.id.edtFarmName)
    EditText edtFarmName;

    @BindView(R.id.genderSpinner)
    Spinner genderSpinner;

    @BindView(R.id.ccp)
    CountryCodePicker ccp;


    @BindView(R.id.btnGetRecommendation)
    AppCompatButton btnGetRec;

    @BindString(R.string.lbl_save)
    String saveTitle;

    private boolean dataIsValid;
    private String firstName;
    private String lastName;
    private String email;
    private String farmName;
    private String mobileCode;
    private String fullMobileNumber;
    private EnumGender gender;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        validationHelper = new ValidationHelper();
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_bio_data, container, false);
        return inflater.inflate(R.layout.fragment_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnGetRec.setText(saveTitle);

        final List<String> localeStrings = new ArrayList<>();
        localeStrings.add(this.getString(R.string.lbl_male));
        localeStrings.add(this.getString(R.string.lbl_female));

        final SpinnerAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, localeStrings);
        genderSpinner.setAdapter(adapter);

        btnGetRec.setOnClickListener(view1 -> saveBioData());

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            dataIsValid = true;
            if (!isValidNumber) {
                edtPhone.setError(this.getString(R.string.lbl_valid_number_req));
            } else {
                edtPhone.setError(null);
            }
        });

        ccp.setOnCountryChangeListener(() -> mobileCode = ccp.getSelectedCountryCodeWithPlus());
        ccp.registerCarrierNumberEditText(edtPhone);

    }

    @Override
    public void refreshData() {
        try {
            profileInfo = objectBoxEntityProcessor.getProfileInfo();
            if (profileInfo != null) {
                firstName = profileInfo.getFirstName();
                lastName = profileInfo.getLastName();
                farmName = profileInfo.getFarmName();
                email = profileInfo.getEmail();
                mobileCode = profileInfo.getMobileCode();
                fullMobileNumber = profileInfo.getFullMobileNumber();
                gender = profileInfo.getGenderEnum();

                edtFirstName.setText(firstName);
                edtLastName.setText(lastName);
                edtFarmName.setText(farmName);
                edtEmail.setText(email);
                ccp.setFullNumber(fullMobileNumber);
            }

        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, LOG_TAG, "An error occurred getting BioDataInfo info");
            Crashlytics.logException(ex);
        }
    }

    private void saveBioData() {
        edtFirstName.setError(null);
        edtLastName.setError(null);
        edtFarmName.setError(null);
        edtEmail.setError(null);
        dataIsValid = true;

        firstName = edtFirstName.getText().toString();
        lastName = edtLastName.getText().toString();
        farmName = edtFarmName.getText().toString();
        email = edtEmail.getText().toString();
        fullMobileNumber = ccp.getFullNumber();

        if (Strings.isEmptyOrWhitespace(firstName)) {
            dataIsValid = false;
            edtFirstName.setError(this.getString(R.string.lbl_first_name_req));
        }

        if (Strings.isEmptyOrWhitespace(lastName)) {
            dataIsValid = false;
            edtLastName.setError("Please provide your last name");
        }

        if (Strings.isEmptyOrWhitespace(farmName)) {
            dataIsValid = false;
            edtFarmName.setError("Please provide your farm name");
        }

        if (!validationHelper.isValidEmail(email) && !Strings.isEmptyOrWhitespace(email)) {
            dataIsValid = false;
            edtEmail.setError(this.getString(R.string.lbl_valid_email_req));
        }

        if (dataIsValid) {
            if (profileInfo == null) {
                profileInfo = new ProfileInfo();
            }
            profileInfo.setFirstName(firstName);
            profileInfo.setLastName(lastName);
            profileInfo.setGenderEnum(gender);
            profileInfo.setEmail(email);
            profileInfo.setFarmName(farmName);
            profileInfo.setMobileCode(mobileCode);
            profileInfo.setFullMobileNumber(fullMobileNumber);

            long id = objectBoxEntityProcessor.saveProfileInfo(profileInfo);
            if (id > 0) {
                nextFragment();
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
