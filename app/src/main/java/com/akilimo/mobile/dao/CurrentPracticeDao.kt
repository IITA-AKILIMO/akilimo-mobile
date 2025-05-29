package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CurrentPractice

@Dao
interface CurrentPracticeDao {

    @Query("SELECT * FROM current_practice")
    fun listAll(): List<CurrentPractice>

    @Query("SELECT * FROM current_practice LIMIT 1")
    fun findOne(): CurrentPractice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: CurrentPractice)

    @Update
    fun update(currentPractice: CurrentPractice)

    @Delete
    fun delete(currentPractice: CurrentPractice?)
    @Query("DELETE FROM current_practice")
    fun deleteAll()
}
