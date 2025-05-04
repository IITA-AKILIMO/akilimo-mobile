package com.akilimo.mobile.inherit

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.akilimo.mobile.adapters.RecOptionsAdapter
import com.akilimo.mobile.models.RecommendationOptions
import io.sentry.Sentry

abstract class BaseRecommendationActivity<T : ViewBinding> : BaseActivity() {

    private var _binding: T? = null
    protected val binding
        get() = _binding ?: throw IllegalStateException("Binding is not initialized yet.")

    private val recList get() = getRecommendationOptions()

    protected var mAdapter: RecOptionsAdapter = RecOptionsAdapter(emptyList())
    protected var dataPositionChanged = 0

    protected abstract fun inflateBinding(): T
    protected abstract fun getRecommendationOptions(): List<RecommendationOptions>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateBinding()
        setContentView(binding.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        mAdapter.setData(recList, dataPositionChanged)
    }

    @Deprecated(
        "Remove completely and use setupToolbar(toolbar, titleResId) instead.",
        replaceWith = ReplaceWith("setupToolbar(binding.toolbarLayout.toolbar, R.string.your_title)"),
        level = DeprecationLevel.WARNING
    )
    override fun initToolbar() {
        // Log the usage of deprecated method
        Sentry.captureMessage("initToolbar is deprecated. Use setupToolbar instead.")
    }

    @Deprecated("Deprecated remove it completely")
    override fun initComponent() {
        // Log the usage of deprecated method
        Sentry.captureMessage("initComponent is deprecated and should no longer be used.")
    }

    override fun validate(backPressed: Boolean) {
        // Logging to indicate that validate is not implemented
        val errorMsg = "validate Not implemented for this class"
        Sentry.captureMessage(errorMsg)
        throw UnsupportedOperationException(errorMsg)
    }
}