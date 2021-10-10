package com.akilimo.mobile.views.fragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;

import com.akilimo.mobile.R;
import com.akilimo.mobile.databinding.FragmentPrivacyStatementBinding;
import com.akilimo.mobile.inherit.BaseStepFragment;
import com.akilimo.mobile.utils.SessionManager;
import com.stepstone.stepper.VerificationError;

/**
 * A simple {@link Fragment} subclass.
 * https://app-privacy-policy-generator.firebaseapp.com/#
 */
public class PrivacyStatementFragment extends BaseStepFragment {


    WebView webView;
    AppCompatCheckBox chkAgreeToTerms;
    FragmentPrivacyStatementBinding binding;
    private boolean policyAccepted;

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
        webView = binding.webView;
        if (sessionManager != null) {
            sessionManager = new SessionManager(context);
        }

        WebView.setWebContentsDebuggingEnabled(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setScrollContainer(true);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setGeolocationEnabled(true);


        String termsLink = sessionManager.getTermsLink();
        chkAgreeToTerms = binding.chkAgreeToTerms;
        webView.loadUrl(termsLink);
        chkAgreeToTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                policyAccepted = checked;
            }
        });
    }

    @Nullable
    @Override
    public VerificationError verifyStep() {
        sessionManager.setTermsAccepted(policyAccepted);
        if (policyAccepted) {
            //save to session and skip in future startup
            return null;
        }
        return new VerificationError(getString(R.string.lbl_accept_terms_prompt));
    }

    @Override
    public void onSelected() {

    }

    @Override
    public void onError(@NonNull VerificationError error) {

    }
}
