package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.ProfileInfo
import com.iita.akilimo.entities.ScheduledDate

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