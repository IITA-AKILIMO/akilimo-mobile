package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.MandatoryInfo

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
}