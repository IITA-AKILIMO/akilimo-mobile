package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.ProfileInfo
import com.iita.akilimo.entities.ScheduledDate
import com.iita.akilimo.entities.StarchFactory

@Dao
interface StarchFactoryDao {

    @Query("SELECT * FROM starch_factory")
    fun listAll(): List<StarchFactory>

    @Query("SELECT * FROM starch_factory LIMIT 1")
    fun findOne(): StarchFactory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: StarchFactory)

    @Update
    fun update(vararg users: StarchFactory)

    @Delete
    fun delete(user: StarchFactory?)
}