package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.UserProfile

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles")
    fun findAll(): List<UserProfile>

    @Query("SELECT * FROM user_profiles WHERE device_token IS NOT NULL AND TRIM(device_token) != '' LIMIT 1")
    fun findOne(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg profiles: UserProfile)

    @Update
    fun update(profiles: UserProfile)

    @Delete
    fun delete(userProfile: UserProfile?)

    @Query("DELETE FROM user_profiles")
    fun deleteAll()
}
