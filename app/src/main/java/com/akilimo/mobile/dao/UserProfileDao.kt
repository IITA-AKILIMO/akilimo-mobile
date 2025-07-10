package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.UserProfile

@Dao
interface UserProfileDao: BaseDao<UserProfile> {

    @Query("SELECT * FROM user_profiles")
    fun findAll(): List<UserProfile>

    @Query("SELECT * FROM user_profiles WHERE device_token IS NOT NULL AND TRIM(device_token) != '' LIMIT 1")
    fun findOne(): UserProfile?

    @Query("DELETE FROM user_profiles")
    fun deleteAll()
}
