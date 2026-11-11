package com.akilimo.mobile.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.sentry.Sentry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/** Runs a pull-to-refresh [block], showing the spinner through [setRefreshing] until it ends. */
internal fun ViewModel.launchRefresh(setRefreshing: (Boolean) -> Unit, block: suspend () -> Unit) {
    viewModelScope.launch {
        setRefreshing(true)
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Sentry.captureException(e)
        } finally {
            setRefreshing(false)
        }
    }
}