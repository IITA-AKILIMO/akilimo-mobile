package com.akilimo.mobile.inherit

import io.sentry.Sentry

abstract class MyBaseActivity : BaseActivity() {
    override fun validate(backPressed: Boolean) {
        val errorMsg = "validate Not implemented for this class"
        Sentry.captureMessage(errorMsg)
        throw UnsupportedOperationException(errorMsg)
    }
}