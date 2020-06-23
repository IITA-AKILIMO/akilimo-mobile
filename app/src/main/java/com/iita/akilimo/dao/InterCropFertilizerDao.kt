package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.InterCropFertilizer

//@Deprecated("To be removed when app is stable")
@Dao
interface InterCropFertilizerDao {

    @Query("SELECT * FROM intercrop_fertilizer")
    fun listAll(): List<InterCropFertilizer>

    @Query("SELECT * FROM intercrop_fertilizer LIMIT 1")
    fun findOne(): InterCropFertilizer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg interCropFertilizer: InterCropFertilizer)

    @Update
    fun update(vararg interCropFertilizer: InterCropFertilizer)

    @Delete
    fun delete(interCropFertilizer: InterCropFertilizer)
}