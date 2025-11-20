package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.AkilimoUserDao
import com.akilimo.mobile.entities.AkilimoUser
import io.sentry.Sentry

class AkilimoUserRepo(private val dao: AkilimoUserDao) {
    suspend fun saveOrUpdateUser(user: AkilimoUser, userName: String) {
        val existing = dao.findOne(userName)
        if (existing != null) {
            dao.update(user)
        } else {
            user.userName = userName
            dao.insert(user)
        }
    }

    suspend fun getUser(userName: String): AkilimoUser? =
        runCatching { dao.findOne(userName) }
            .onFailure { ex -> Sentry.captureException(ex) }
            .getOrNull()
}
