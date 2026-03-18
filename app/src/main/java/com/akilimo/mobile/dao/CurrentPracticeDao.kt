package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.akilimo.mobile.entities.CurrentPractice

@Dao
interface CurrentPracticeDao {

    @Query("SELECT * FROM current_practices WHERE user_id = :userId LIMIT 1")
    suspend fun getPracticeForUser(userId: Int): CurrentPractice?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(practice: CurrentPractice): Long

    @Upsert
    suspend fun upsert(practice: CurrentPractice)

    @Update
    suspend fun update(practice: CurrentPractice)

    @Delete
    suspend fun delete(practice: CurrentPractice)
}
