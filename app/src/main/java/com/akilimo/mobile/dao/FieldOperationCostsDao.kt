package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.FieldOperationCost

@Dao
interface FieldOperationCostsDao {

    @Query("SELECT * FROM field_operation_cost")
    fun listAll(): List<FieldOperationCost>

    @Query("SELECT * FROM field_operation_cost LIMIT 1")
    fun findOne(): FieldOperationCost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg fieldYield: FieldOperationCost)

    @Update
    fun update(vararg fieldYield: FieldOperationCost?)

    @Delete
    fun delete(fieldYield: FieldOperationCost?)
}
