package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.UserLocation

@Dao
interface UserLocationDao: BaseDao<UserLocation> {

    @Query("SELECT * FROM user_location")
    fun listAll(): List<UserLocation>

    @Query("SELECT * FROM user_location LIMIT 1")
    fun findOne(): UserLocation?

    @Query("DELETE FROM user_location")
    fun deleteAll()
}
