package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.FieldOperationCost

@Dao
interface FieldOperationCostsDao {

    @Query("SELECT * FROM field_operation_costs")
    fun listAll(): List<FieldOperationCost>

    @Query("SELECT * FROM field_operation_costs LIMIT 1")
    fun findOne(): FieldOperationCost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(vararg fieldYield: FieldOperationCost)

    @Update
    fun update(vararg fieldYield: FieldOperationCost?)

    @Delete
    fun delete(fieldYield: FieldOperationCost?)
    @Query("DELETE FROM field_operation_costs")
    fun deleteAll()
}
