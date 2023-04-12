package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.LocationInfo

@Dao
interface LocationInfoDao {

    @Query("SELECT * FROM location_info")
    fun listAll(): List<LocationInfo>

    @Query("SELECT * FROM location_info LIMIT 1")
    fun findOne(): LocationInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(location: LocationInfo)

    @Update
    fun update(location: LocationInfo)

    @Delete
    fun delete(location: LocationInfo?)

    @Query("DELETE FROM location_info")
    fun deleteAll()
}
