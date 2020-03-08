package com.iita.akilimo.views.fragments;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iita.akilimo.R;
import com.iita.akilimo.inherit.BaseFragment;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivacyStatementFragment extends BaseFragment {

    @BindView(R.id.statementText)
    TextView statementText;

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
        return inflater.inflate(R.layout.fragment_privacy_statement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadDoc();
    }

    @Override
    public void refreshData() {
    }


    private void loadDoc() {

        String s = "";

        for (int x = 0; x <= 100; x++) {
            s += "Line: " + String.valueOf(x) + "\n";
        }
        statementText.setMovementMethod(new ScrollingMovementMethod());
        statementText.setText(s);
    }
}
