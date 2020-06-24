package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.iita.akilimo.databinding.FragmentPrivacyStatementBinding;
import com.iita.akilimo.inherit.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivacyStatementFragment extends BaseFragment {

    TextView statementText;
    FragmentPrivacyStatementBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    public PrivacyStatementFragment() {
        // Required empty public constructor
    }

    public static PrivacyStatementFragment newInstance() {
        return new PrivacyStatementFragment();
    }


    @Override
    protected View loadFragmentLayout(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPrivacyStatementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        statementText = binding.statementText;
        loadDoc();
    }

    @Override
    public void refreshData() {
    }


    private void loadDoc() {

        StringBuilder statementString = new StringBuilder();

        for (int x = 0; x <= 100; x++) {
            statementString.append("Line: ").append(String.valueOf(x)).append("\n");
        }
        statementText.setMovementMethod(new ScrollingMovementMethod());
        statementText.setText(statementString.toString());
    }
}
