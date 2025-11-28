package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.AkilimoUser

@Dao
interface AkilimoUserDao {

    @Query("SELECT * FROM akilimo_users")
    fun listAll(): List<AkilimoUser>

    @Query("SELECT * FROM akilimo_users WHERE user_name=:userName LIMIT 1")
    fun findOne(userName: String): AkilimoUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(profile: AkilimoUser)

    @Update
    fun update(profile: AkilimoUser)
}
