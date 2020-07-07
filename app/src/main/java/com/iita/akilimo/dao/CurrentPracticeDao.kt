package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.CurrentPractice

@Dao
interface CurrentPracticeDao {

    @Query("SELECT * FROM current_practice")
    fun listAll(): List<CurrentPractice>

    @Query("SELECT * FROM current_practice LIMIT 1")
    fun findOne(): CurrentPractice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: CurrentPractice)

    @Update
    fun update(location: CurrentPractice)

    @Delete
    fun delete(location: CurrentPractice?)
}