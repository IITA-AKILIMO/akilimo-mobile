package com.akilimo.mobile.views.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

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


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (sessionManager != null) {
            sessionManager = new SessionManager(context);
        }

        WebView.setWebContentsDebuggingEnabled(false);
        binding.webView.setWebChromeClient(new WebChromeClient());
        binding.webView.setScrollContainer(true);
        binding.webView.setVerticalScrollBarEnabled(false);
        binding.webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        binding.webView.setHorizontalScrollBarEnabled(false);
        binding.webView.setWebChromeClient(new WebChromeClient());

        WebSettings webSettings = binding.webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setGeolocationEnabled(true);


        binding.chkAgreeToTerms.setOnCheckedChangeListener((compoundButton, checked) -> policyAccepted = checked);
    }

    @Override
    public void onSelected() {
        String termsLink = sessionManager.getTermsLink();
        binding.webView.loadUrl(termsLink);
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
    public void onError(@NonNull VerificationError error) {

    }
}
