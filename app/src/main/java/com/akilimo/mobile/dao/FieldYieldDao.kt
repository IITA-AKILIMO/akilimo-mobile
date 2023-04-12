package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.FieldYield

@Dao
interface FieldYieldDao {

    @Query("SELECT * FROM field_yield")
    fun listAll(): List<FieldYield>

    @Query("SELECT * FROM field_yield LIMIT 1")
    fun findOne(): FieldYield?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fieldYield: FieldYield)

    @Update
    fun update(fieldYield: FieldYield?)

    @Delete
    fun delete(fieldYield: FieldYield?)

    @Query("DELETE FROM field_yield")
    fun deleteAll()
}
