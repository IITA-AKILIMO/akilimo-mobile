package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.CassavaYieldDao
import com.akilimo.mobile.entities.CassavaYield
import kotlinx.coroutines.flow.Flow


class CassavaYieldRepo(private val dao: CassavaYieldDao) {

    suspend fun saveAll(yields: List<CassavaYield>) {
        yields.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f) // record exists, update in place
            }
        }
    }

    fun observeAll(): Flow<List<CassavaYield>> = dao.observeAll()
}