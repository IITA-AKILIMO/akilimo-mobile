package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.FieldOperationCost

@Dao
interface FieldOperationCostsDao : BaseDao<FieldOperationCost> {

    @Query("SELECT * FROM field_operation_costs")
    suspend fun listAll(): List<FieldOperationCost>

    @Query("SELECT * FROM field_operation_costs LIMIT 1")
    suspend fun findOne(): FieldOperationCost?
}
