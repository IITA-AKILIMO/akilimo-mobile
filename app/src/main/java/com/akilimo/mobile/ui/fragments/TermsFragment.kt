package com.akilimo.mobile.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import com.akilimo.mobile.R
import com.akilimo.mobile.base.BaseStepFragment
import com.akilimo.mobile.databinding.FragmentTermsBinding
import com.stepstone.stepper.VerificationError

/**
 * A simple [Fragment] subclass.
 * Use the [TermsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TermsFragment : BaseStepFragment<FragmentTermsBinding>() {
    companion object {
        fun newInstance() = TermsFragment()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTermsBinding {
        return FragmentTermsBinding.inflate(inflater, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onBindingReady(savedInstanceState: Bundle?) {
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

            chkAgreeToTerms.setOnCheckedChangeListener { _: CompoundButton?, checked: Boolean ->
                sessionManager.termsAccepted = checked
            }
        }
    }

    override fun onSelected() {
        super.onSelected()
        binding.webView.loadUrl(sessionManager.termsLink)
    }

    override fun verifyStep(): VerificationError? {
        if (!sessionManager.termsAccepted) {
            return VerificationError(getString(R.string.lbl_accept_terms_prompt))
        }
        return null
    }
}