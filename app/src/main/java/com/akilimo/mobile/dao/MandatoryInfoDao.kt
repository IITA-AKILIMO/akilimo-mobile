package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.MandatoryInfo

@Dao
interface MandatoryInfoDao {

    @Query("SELECT * FROM mandatory_info")
    fun listAll(): List<MandatoryInfo>

    @Query("SELECT * FROM mandatory_info LIMIT 1")
    fun findOne(): MandatoryInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mandatoryInfo: MandatoryInfo)

    @Update
    fun update(mandatoryInfo: MandatoryInfo)

    @Delete
    fun delete(mandatoryInfo: MandatoryInfo?)

    @Query("DELETE FROM mandatory_info")
    fun deleteAll()
}
