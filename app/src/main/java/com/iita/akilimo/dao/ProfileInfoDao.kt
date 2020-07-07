package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.ProfileInfo

@Dao
interface ProfileInfoDao {

    @Query("SELECT * FROM profile_info")
    fun listAll(): List<ProfileInfo>

    @Query("SELECT * FROM profile_info LIMIT 1")
    fun findOne(): ProfileInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: ProfileInfo)

    @Update
    fun update(vararg users: ProfileInfo)

    @Delete
    fun delete(user: ProfileInfo?)
}