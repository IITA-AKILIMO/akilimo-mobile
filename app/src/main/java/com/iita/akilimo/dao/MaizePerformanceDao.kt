package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.MaizeMarket
import com.iita.akilimo.entities.MaizePerformance

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