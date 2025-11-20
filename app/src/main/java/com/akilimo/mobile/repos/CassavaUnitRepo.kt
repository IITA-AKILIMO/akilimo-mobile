package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.CassavaUnitDao
import com.akilimo.mobile.entities.CassavaUnit
import kotlinx.coroutines.flow.Flow

class CassavaUnitRepo(
    private val dao: CassavaUnitDao
) {

    suspend fun saveAll(fertilizers: List<CassavaUnit>) {
        fertilizers.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f) // record exists, update in place
            }
        }
    }


    /** Observe all units sorted by sort_order */
    fun observeAll(): Flow<List<CassavaUnit>> = dao.observeAll()

    /** Get a single unit by ID */
    suspend fun getById(id: Int): CassavaUnit? = dao.getById(id)

    /** Clear all units */
    suspend fun clear() = dao.clear()
}
