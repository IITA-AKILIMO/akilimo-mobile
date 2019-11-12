package com.iita.akilimo.views.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;

public class UserBioDataFragment extends BaseFragment {


    public UserBioDataFragment() {
        // Required empty public constructor
    }

    public static UserBioDataFragment newInstance() {
        return new UserBioDataFragment();
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bio_data_b, container, false);
    }

    @Override
    public void refreshData() {
        throw new UnsupportedOperationException();
    }
}
