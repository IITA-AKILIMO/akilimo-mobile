package com.iita.akilimo.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.iita.akilimo.databinding.FragmentFieldInfoBinding;
import com.iita.akilimo.inherit.BaseStepFragment;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldInfoFragment extends BaseStepFragment {

    FragmentFieldInfoBinding binding;

    public FieldInfoFragment() {
        // Required empty public constructor
    }

    public static FieldInfoFragment newInstance() {
        return new FieldInfoFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFieldInfoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        return null;
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
