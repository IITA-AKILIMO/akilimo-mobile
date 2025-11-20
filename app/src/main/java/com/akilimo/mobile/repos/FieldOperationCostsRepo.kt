package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.FieldOperationCostDao
import com.akilimo.mobile.entities.FieldOperationCost
import kotlinx.coroutines.flow.Flow

class FieldOperationCostsRepo(private val dao: FieldOperationCostDao) {
    /**
     * Upsert a single cost record.
     */
    suspend fun saveCost(cost: FieldOperationCost): Long {
        return dao.upsert(cost)
    }


    /**
     * Observe costs for a specific user.
     */
    fun observeCostsForUser(userId: Int): Flow<List<FieldOperationCost>> {
        return dao.getCostsForUser(userId)
    }

    suspend fun getCostForUser(userId: Int): FieldOperationCost? {
        return dao.getCostForUser(userId)
    }
}