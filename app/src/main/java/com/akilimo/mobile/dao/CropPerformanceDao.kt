package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.CropPerformance

@Dao
interface CropPerformanceDao: BaseDao<CropPerformance> {

    @Query("SELECT * FROM crop_performance")
    fun listAll(): List<CropPerformance>

    @Query("SELECT * FROM crop_performance LIMIT 1")
    fun findOne(): CropPerformance?
}
