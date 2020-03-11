package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

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
import com.iita.akilimo.entities.ProfileInfo;
import com.iita.akilimo.inherit.BaseFragment;
import com.iita.akilimo.interfaces.IFragmentCallBack;
import com.iita.akilimo.utils.ValidationHelper;
import com.iita.akilimo.utils.enums.EnumGender;

import butterknife.BindString;
import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class BioDataFragment extends BaseFragment {

    private String LOG_TAG = BioDataFragment.class.getSimpleName();
    private IFragmentCallBack fragmentCallBack;
    private ProfileInfo profileInfo;
    private ValidationHelper validationHelper;


    @BindView(R.id.rdgGender)
    RadioGroup rdgGender;

    @BindView(R.id.lytFirstName)
    TextInputLayout lytFirstName;

    @BindView(R.id.lytLastName)
    TextInputLayout lytLastName;

    @BindView(R.id.lytFarmName)
    TextInputLayout lytFarmName;

    @BindView(R.id.lytEmail)
    TextInputLayout lytEmail;

    @BindView(R.id.lytPhone)
    TextInputLayout lytPhone;
    @BindView(R.id.edtPhone)
    TextInputEditText edtPhone;

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
        return inflater.inflate(R.layout.fragment_bio_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnGetRec.setText(saveTitle);

        btnGetRec.setOnClickListener(view1 -> saveBioData());
        rdgGender.setOnCheckedChangeListener((radioGroup, radioIndex) -> {
            switch (radioIndex) {
                case R.id.rdFemale:
                    gender = EnumGender.FEMALE;
                    break;
                case R.id.rdMale:
                    gender = EnumGender.MALE;
                    break;
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
            profileInfo = objectBoxEntityProcessor.getProfileInfo();
            if (profileInfo != null) {
                firstName = profileInfo.getFirstName();
                lastName = profileInfo.getLastName();
                farmName = profileInfo.getFarmName();
                email = profileInfo.getEmail();
                mobileCode = profileInfo.getMobileCode();
                fullMobileNumber = profileInfo.getFullMobileNumber();
                gender = profileInfo.getGenderEnum();

                lytFirstName.getEditText().setText(firstName);
                lytLastName.getEditText().setText(lastName);
                lytFarmName.getEditText().setText(farmName);
                lytEmail.getEditText().setText(email);
                ccp.setFullNumber(fullMobileNumber);

                switch (gender) {
                    case MALE:
                        rdgGender.check(R.id.rdMale);
                        break;
                    case FEMALE:
                        rdgGender.check(R.id.rdFemale);
                        break;
                }
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

        if (Strings.isEmptyOrWhitespace(firstName)) {
            dataIsValid = false;
            lytLastName.setError(this.getString(R.string.lbl_first_name_req));
        }

        if (Strings.isEmptyOrWhitespace(lastName)) {
            dataIsValid = false;
            lytLastName.setError("Please provide your last name");
        }

        if (Strings.isEmptyOrWhitespace(farmName)) {
            dataIsValid = false;
            lytFarmName.setError("Please provide your farm name");
        }

        if (!validationHelper.isValidEmail(email) && !Strings.isEmptyOrWhitespace(email)) {
            dataIsValid = false;
            lytEmail.setError(this.getString(R.string.lbl_valid_email_req));
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
