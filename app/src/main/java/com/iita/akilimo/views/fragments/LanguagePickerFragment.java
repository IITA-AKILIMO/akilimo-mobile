package com.iita.akilimo.views.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;

/**
 * A simple {@link BaseFragment} subclass.
 * Use the {@link LanguagePickerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LanguagePickerFragment extends BaseFragment {

    public LanguagePickerFragment() {
        // Required empty public constructor
    }

    public static LanguagePickerFragment newInstance() {
        return new LanguagePickerFragment();
    }

    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language_picker, container, false);
    }

    @Override
    public void refreshData() {

    }

}
