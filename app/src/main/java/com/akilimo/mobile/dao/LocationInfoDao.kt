package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.UserLocation

@Dao
interface LocationInfoDao {

    @Query("SELECT * FROM location_info")
    fun listAll(): List<UserLocation>

    @Query("SELECT * FROM location_info LIMIT 1")
    fun findOne(): UserLocation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: UserLocation)

    @Update
    fun update(location: UserLocation)

    @Delete
    fun delete(location: UserLocation?)

    @Query("DELETE FROM location_info")
    fun deleteAll()
}
