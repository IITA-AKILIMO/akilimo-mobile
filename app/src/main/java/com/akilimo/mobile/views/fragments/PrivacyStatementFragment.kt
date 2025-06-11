package com.akilimo.mobile.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.CompoundButton
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentPrivacyStatementBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.stepstone.stepper.VerificationError


/**
 * A simple [Fragment] subclass.
 * [...](https://app-privacy-policy-generator.firebaseapp.com/#)
 */
class PrivacyStatementFragment : BindBaseStepFragment<FragmentPrivacyStatementBinding>() {

    companion object {
        fun newInstance(): PrivacyStatementFragment {
            return PrivacyStatementFragment()
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPrivacyStatementBinding.inflate(inflater, container, false)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(false)
        binding.apply {

            webView.apply {
                webViewClient = WebViewClient()
                isScrollContainer = true
                isVerticalScrollBarEnabled = false
                scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
                isHorizontalScrollBarEnabled = false

                val webSettings = webView.settings
                webSettings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true

                    setSupportZoom(true)
                    setGeolocationEnabled(false)
                }
            }


            chkAgreeToTerms.setOnCheckedChangeListener { compoundButton: CompoundButton?, checked: Boolean ->
                sessionManager.setTermsAccepted(checked)
            }
        }
    }

    override fun onSelected() {
        val termsLink = sessionManager.getTermsLink()
        binding.webView.loadUrl(termsLink)
    }


    override fun verifyStep(): VerificationError? {
        if (sessionManager.getTermsAccepted()) {
            return null
        }
        return VerificationError(getString(R.string.lbl_accept_terms_prompt))
    }
}
