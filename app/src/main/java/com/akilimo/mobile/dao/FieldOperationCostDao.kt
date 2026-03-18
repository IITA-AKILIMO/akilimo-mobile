package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.akilimo.mobile.entities.FieldOperationCost
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldOperationCostDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cost: FieldOperationCost): Long

    @Update
    suspend fun update(cost: FieldOperationCost)

    // Insert or update a single cost record
    @Upsert
    suspend fun upsert(cost: FieldOperationCost): Long

    // Insert or update multiple cost records
    @Upsert
    suspend fun upsertCosts(costs: List<FieldOperationCost>): List<Long>

    // Delete a cost record
    @Delete
    suspend fun deleteCost(cost: FieldOperationCost)

    // Fetch all costs
    @Query("SELECT * FROM field_operation_costs")
    fun getAllCosts(): Flow<List<FieldOperationCost>>

    @Query("SELECT * FROM field_operation_costs WHERE user_id = :userId")
    suspend fun getCostForUser(userId: Int): FieldOperationCost?

    // Fetch costs for a specific user
    @Query("SELECT * FROM field_operation_costs WHERE user_id = :userId")
    fun getCostsForUser(userId: Int): Flow<List<FieldOperationCost>>


}
