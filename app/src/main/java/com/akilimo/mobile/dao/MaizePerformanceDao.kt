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
    fun insert(maizePerformance: MaizePerformance)

    @Update
    fun update(maizePerformance: MaizePerformance)

    @Delete
    fun delete(maizePerformance: MaizePerformance?)

    @Query("DELETE FROM maize_performance")
    fun deleteAll()
}
