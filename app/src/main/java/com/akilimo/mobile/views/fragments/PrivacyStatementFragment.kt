package com.akilimo.mobile.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.akilimo.mobile.R
import com.akilimo.mobile.databinding.FragmentPrivacyStatementBinding
import com.akilimo.mobile.inherit.BindBaseStepFragment
import com.stepstone.stepper.VerificationError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PrivacyStatementFragment : BindBaseStepFragment<FragmentPrivacyStatementBinding>() {

    companion object {
        const val SCROLL_DELAY = 300L
        fun newInstance(): PrivacyStatementFragment = PrivacyStatementFragment()
    }

    private var scrollCheckJob: Job? = null

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentPrivacyStatementBinding.inflate(inflater, container, false)

    override fun onBindingReady(savedInstanceState: Bundle?) {
        WebView.setWebContentsDebuggingEnabled(false)

        binding.privacyStatementWebView.run {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    startScrollMonitoring()
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

            loadUrl(preferenceManager.privacyPolicyLink)
        }
    }

    private fun startScrollMonitoring() {
        scrollCheckJob?.cancel() // cancel any previous job

        scrollCheckJob = viewLifecycleOwner.lifecycleScope.launch {
            while (!preferenceManager.privacyPolicyRead) {
                val webView = binding.privacyStatementWebView
                if (webView.isScrolledToBottom()) {
                    preferenceManager.privacyPolicyRead = true
                    break
                }
                delay(SCROLL_DELAY) // check every 300ms
            }
        }
    }

    override fun setupObservers() {
        TODO("Not yet implemented")
    }

    override fun verifyStep(): VerificationError? {
        return if (preferenceManager.privacyPolicyRead) {
            null
        } else {
            VerificationError(getString(R.string.lbl_read_full_policy_prompt))
        }
    }

    override fun onDestroyView() {
        scrollCheckJob?.cancel()
        scrollCheckJob = null
        super.onDestroyView()
    }
}
