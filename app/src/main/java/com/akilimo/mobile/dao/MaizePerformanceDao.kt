package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.MaizePerformance

@Dao
interface MaizePerformanceDao {

    @Query("SELECT * FROM maize_performance")
    fun listAll(): List<MaizePerformance>

    @Query("SELECT * FROM maize_performance LIMIT 1")
    fun findOne(): MaizePerformance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: MaizePerformance)

    @Update
    fun update(location: MaizePerformance)

    @Delete
    fun delete(location: MaizePerformance?)
}
