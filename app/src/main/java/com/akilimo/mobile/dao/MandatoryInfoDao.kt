package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akilimo.mobile.entities.MandatoryInfo

@Dao
interface MandatoryInfoDao: BaseDao<MandatoryInfo> {

    @Query("SELECT * FROM mandatory_info")
    fun listAll(): List<MandatoryInfo>

    @Query("SELECT * FROM mandatory_info LIMIT 1")
    fun findOne(): MandatoryInfo?
}
