package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CropPerformance

@Dao
interface CropPerformanceDao {

    @Query("SELECT * FROM crop_performance")
    fun listAll(): List<CropPerformance>

    @Query("SELECT * FROM crop_performance LIMIT 1")
    fun findOne(): CropPerformance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cropPerformance: CropPerformance)

    @Update
    fun update(cropPerformance: CropPerformance)

    @Delete
    fun delete(cropPerformance: CropPerformance?)

    @Query("DELETE FROM crop_performance")
    fun deleteAll()
}
