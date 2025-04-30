package com.akilimo.mobile.inherit

import io.sentry.Sentry

abstract class BaseRecommendationActivity : BaseActivity() {

    @Deprecated(
        "Remove completely and use setupToolbar(toolbar, titleResId) instead.",
        replaceWith = ReplaceWith("setupToolbar(binding.toolbarLayout.toolbar, R.string.your_title)"),
        level = DeprecationLevel.WARNING
    )
    override fun initToolbar() {
        val ex = UnsupportedOperationException("intiTooBar Not implemented for this class")
        Sentry.captureException(ex)
    }

    @Deprecated("Deprecated remove it completely")
    override fun initComponent() {
        val ex = UnsupportedOperationException("initComponent Not implemented for this class")
        Sentry.captureException(ex)
    }

    override fun validate(backPressed: Boolean) {
        val ex = UnsupportedOperationException("validate Not implemented for this class")
        Sentry.captureException(ex)
    }
}