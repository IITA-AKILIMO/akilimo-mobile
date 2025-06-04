package com.akilimo.mobile.inherit

abstract class MyBaseActivity : BaseActivity() {
    @Deprecated(
        "Remove completely and use setupToolbar(toolbar, titleResId) instead.",
        replaceWith = ReplaceWith("setupToolbar(binding.toolbarLayout.toolbar, R.string.your_title)"),
        level = DeprecationLevel.WARNING
    )
    override fun initToolbar() {
        throw UnsupportedOperationException()
    }

    @Deprecated("Deprecated remove it completely")
    override fun initComponent() {
        throw UnsupportedOperationException()
    }

    override fun validate(backPressed: Boolean) {
        throw UnsupportedOperationException()
    }
}