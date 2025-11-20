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
    fun getPracticeForUser(userId: Int): CurrentPractice?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun save(practice: CurrentPractice): Long

    @Upsert
    fun upsert(practice: CurrentPractice)

    @Update
    fun update(practice: CurrentPractice)

    @Delete
    fun delete(practice: CurrentPractice)
}
