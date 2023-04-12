package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.ScheduledDate

@Dao
interface ScheduleDateDao {

    @Query("SELECT * FROM scheduled_date")
    fun listAll(): List<ScheduledDate>

    @Query("SELECT * FROM scheduled_date LIMIT 1")
    fun findOne(): ScheduledDate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg scheduledDates: ScheduledDate)

    @Update
    fun update(vararg scheduledDates: ScheduledDate)

    @Delete
    fun delete(scheduledDate: ScheduledDate?)

    @Query("DELETE FROM scheduled_date")
    fun deleteAll()
}
