package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akilimo.mobile.entities.UserPreferences

@Dao
interface UserPreferencesDao {

    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun getPreferences(): UserPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplace(preferences: UserPreferences)
}
