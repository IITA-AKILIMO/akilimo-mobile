package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akilimo.mobile.entities.CropSchedule

@Dao
interface CropScheduleDao: BaseDao<CropSchedule> {

    @Query("SELECT * FROM crop_schedules")
    fun listAll(): List<CropSchedule>

    @Query("SELECT * FROM crop_schedules LIMIT 1")
    fun findOne(): CropSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cropSchedules: CropSchedule)
}
