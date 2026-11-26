package com.akilimo.mobile.repos

import android.content.Context
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.network.AppVersionApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Builds the version API for the current server address (which Settings may change). */
@Singleton
class AppVersionApiFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun create(): AppVersionApi = ApiClient.createService(
        context = context,
        baseUrl = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO),
        timeoutSeconds = TIMEOUT_SECONDS,
    )

    private companion object {
        // Short: the check runs at launch and must never keep a farmer waiting
        const val TIMEOUT_SECONDS = 15L
    }
}
