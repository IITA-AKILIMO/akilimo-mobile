package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CropSchedule

@Dao
interface CropScheduleDao {

    @Query("SELECT * FROM crop_schedules")
    fun listAll(): List<CropSchedule>

    @Query("SELECT * FROM crop_schedules LIMIT 1")
    fun findOne(): CropSchedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cropSchedules: CropSchedule)

    @Update
    fun update(vararg cropSchedules: CropSchedule)

    @Delete
    fun delete(cropSchedule: CropSchedule?)

    @Query("DELETE FROM crop_schedules")
    fun deleteAll()
}
