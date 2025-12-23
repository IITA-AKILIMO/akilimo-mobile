package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.CurrentPracticeDao
import com.akilimo.mobile.entities.CurrentPractice

class CurrentPracticeRepo(private val dao: CurrentPracticeDao) {

    suspend fun getPracticeForUser(userId: Int): CurrentPractice? {
        return dao.getPracticeForUser(userId)
    }

    suspend fun savePractice(practice: CurrentPractice) {
        dao.upsert(practice)
    }
}
