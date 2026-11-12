package com.akilimo.mobile.repos.account

import android.content.Context
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AccountApi
import com.akilimo.mobile.network.ApiClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Builds the account API with the farmer's token and the current server address (which Settings may change). */
@Singleton
class AccountApiFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionStore: AccountSessionStore,
) {
    fun create(): AccountApi = ApiClient.createService(
        context = context,
        baseUrl = AppConfig.resolveBaseUrlFor(context, EnumServiceType.AKILIMO),
        bearerToken = { sessionStore.token },
    )
}
