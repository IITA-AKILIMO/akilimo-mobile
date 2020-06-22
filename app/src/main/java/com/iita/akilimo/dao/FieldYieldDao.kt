package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.FieldYield

@Dao
interface FieldYieldDao {

    @Query("SELECT * FROM field_yield")
    fun listAll(): List<FieldYield>

    @Query("SELECT * FROM field_yield LIMIT 1")
    fun findOne(): FieldYield?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg fieldYield: FieldYield)

    @Update
    fun update(vararg fieldYield: FieldYield?)

    @Delete
    fun delete(fieldYield: FieldYield?)
}