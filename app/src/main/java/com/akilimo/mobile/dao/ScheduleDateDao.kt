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
    fun insert(vararg users: ScheduledDate)

    @Update
    fun update(vararg users: ScheduledDate)

    @Delete
    fun delete(user: ScheduledDate?)
}
