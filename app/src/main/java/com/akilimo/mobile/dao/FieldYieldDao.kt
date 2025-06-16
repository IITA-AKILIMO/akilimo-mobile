package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.akilimo.mobile.entities.FieldYield

@Dao
interface FieldYieldDao {

    @Query("SELECT * FROM field_yields")
    fun listAll(): List<FieldYield>

    @Query("SELECT * FROM field_yields LIMIT 1")
    fun findOne(): FieldYield?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(fieldYield: FieldYield)

    @Delete
    fun delete(fieldYield: FieldYield?)

    @Query("DELETE FROM field_yields")
    fun deleteAll()
}
