package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.UserPreferencesDao
import com.akilimo.mobile.entities.UserPreferences
import io.sentry.Sentry

class UserPreferencesRepo(private val dao: UserPreferencesDao) {

    suspend fun getOrDefault(): UserPreferences =
        runCatching { dao.getPreferences() }
            .onFailure { ex -> Sentry.captureException(ex) }
            .getOrNull() ?: UserPreferences()

    suspend fun save(preferences: UserPreferences) {
        runCatching { dao.insertOrReplace(preferences) }
            .onFailure { ex -> Sentry.captureException(ex) }
    }
}
