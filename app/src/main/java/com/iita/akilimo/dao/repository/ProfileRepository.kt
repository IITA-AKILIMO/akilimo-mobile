package com.iita.akilimo.dao.repository

import com.iita.akilimo.dao.ProfileInfoDao

/**
 * Repository module for handling data operations.
 */
class ProfileRepository private constructor(private val profileInfoDao: ProfileInfoDao) {

    fun findOne() = profileInfoDao.findOneProfile()

    companion object {

        // For Singleton instantiation
        @Volatile
        private var instance: ProfileRepository? = null

        fun getInstance(profileInfoDao: ProfileInfoDao) =
            instance ?: synchronized(this) {
                instance ?: ProfileRepository(profileInfoDao).also { instance = it }
            }
    }
}