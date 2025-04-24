package com.akilimo.mobile.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentInfoBinding;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends BaseStepFragment {

    FragmentInfoBinding binding;

    public InfoFragment() {
        // Required empty public constructor
    }

    public static InfoFragment newInstance() {
        return new InfoFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.chkAgreeToDisclaimer.setOnCheckedChangeListener((compoundButton, checked) -> {
            sessionManager.setDisclaimerRead(checked);
        });

    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        if (!sessionManager.getDisclaimerRead()) {
            return new VerificationError(getString(R.string.lbl_agree_to_disclaimer));
        }
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
