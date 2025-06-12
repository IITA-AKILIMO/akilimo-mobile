package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentPrivacyStatementBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.stepstone.stepper.VerificationError

class PrivacyStatementFragment : BindBaseStepFragment<FragmentPrivacyStatementBinding>() {

    companion object {
        fun newInstance(): PrivacyStatementFragment = PrivacyStatementFragment()
    }

    private var hasScrollListenerBeenAdded = false
    private val scrollChangedListener = ViewTreeObserver.OnScrollChangedListener {
        val webView = binding.privacyStatementWebView
        if (webView.isScrolledToBottom() && !preferenceManager.privacyPolicyRead) {
            preferenceManager.privacyPolicyRead = true
        }
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPrivacyStatementBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        WebView.setWebContentsDebuggingEnabled(false)
        setupWebView()
    }

    private fun setupWebView() = binding.privacyStatementWebView.run {
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (!hasScrollListenerBeenAdded) {
                    viewTreeObserver?.addOnScrollChangedListener(scrollChangedListener)
                    hasScrollListenerBeenAdded = true
                }
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false
        }

        isScrollContainer = true
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY

        settings.apply {
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

    override fun onSelected() {
        binding.privacyStatementWebView.loadUrl(preferenceManager.privacyPolicyLink)
    }

    override fun verifyStep(): VerificationError? {
        return if (preferenceManager.privacyPolicyRead) {
            null
        } else {
            VerificationError(getString(R.string.lbl_read_full_policy_prompt))
        }
    }

    override fun onDestroyView() {
        binding.privacyStatementWebView.viewTreeObserver
            ?.removeOnScrollChangedListener(scrollChangedListener)
        super.onDestroyView()
    }
}
