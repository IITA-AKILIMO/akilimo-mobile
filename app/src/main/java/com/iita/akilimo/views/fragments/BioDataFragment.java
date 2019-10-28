package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.Strings;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
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
    @BindView(R.id.btnGetRec)
    MaterialButton btnGetRec;

    @BindString(R.string.lbl_save)
    String saveTitle;

    private boolean dataIsValid;
    private String firstName;
    private String lastName;
    private String email;
    private String farmName;
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

    }

    @Override
    public void refreshData() {
        profileInfo = objectBoxEntityProcessor.getProfileInfo();
        if (profileInfo != null) {
            firstName = profileInfo.getFirstName();
            lastName = profileInfo.getLastName();
            farmName = profileInfo.getFarmName();
            email = profileInfo.getEmail();
            gender = profileInfo.getGenderEnum();

            lytFirstName.getEditText().setText(firstName);
            lytLastName.getEditText().setText(lastName);
            lytFarmName.getEditText().setText(farmName);
            lytEmail.getEditText().setText(email);
            switch (gender) {
                case MALE:
                    rdgGender.check(R.id.rdMale);
                    break;
                case FEMALE:
                    rdgGender.check(R.id.rdFemale);
                    break;
            }
        } else {
            profileInfo = new ProfileInfo();
        }
    }

    public void saveBioData() {
        lytFirstName.setError(null);
        lytLastName.setError(null);
        lytFarmName.setError(null);
        lytEmail.setError(null);
        dataIsValid = true;

        firstName = lytFirstName.getEditText().getText().toString();
        lastName = lytLastName.getEditText().getText().toString();
        farmName = lytFarmName.getEditText().getText().toString();
        email = lytEmail.getEditText().getText().toString();

        if (Strings.isEmptyOrWhitespace(firstName)) {
            dataIsValid = false;
            lytLastName.setError("Please provide your first name");
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
            lytEmail.setError("Please provide a valid email");
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
