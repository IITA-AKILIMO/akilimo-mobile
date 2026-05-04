package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.MaizePerformanceDao
import com.akilimo.mobile.entities.MaizePerformance
import kotlinx.coroutines.flow.Flow

class MaizePerformanceRepo(
    private val dao: MaizePerformanceDao
) {
    suspend fun saveOrUpdatePerformance(entry: MaizePerformance) {
        val existing = dao.getByUserId(entry.userId)
        if (existing != null) {
            val updated = existing.copy(
                maizePerformance = entry.maizePerformance
            ).apply {
                createdAt = existing.createdAt
                updatedAt = System.currentTimeMillis()
            }
            dao.update(updated)
        } else {
            dao.insert(entry)
        }
    }

    suspend fun getPerformanceForUser(userId: Int): MaizePerformance? {
        return dao.getByUserId(userId)
    }

    fun observeByUserId(userId: Int): Flow<MaizePerformance?> {
        return dao.observeByUserId(userId)
    }

    suspend fun deletePerformanceForUser(userId: Int) {
        dao.deleteByUserId(userId)
    }
}
