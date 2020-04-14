package com.iita.akilimo.views.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class FieldInfoFragment extends BaseFragment {

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
        return inflater.inflate(R.layout.fragment_field_info, container, false);
    }

    @Override
    public void refreshData() {

    }
}
