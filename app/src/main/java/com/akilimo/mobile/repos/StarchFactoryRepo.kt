package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.StarchFactoryDao
import com.akilimo.mobile.entities.StarchFactory
import kotlinx.coroutines.flow.Flow

class StarchFactoryRepo(private val dao: StarchFactoryDao) {
    fun observeAll(): Flow<List<StarchFactory>> {
        return dao.observeAll()
    }

    suspend fun saveAll(starchFactories: List<StarchFactory>) {
        starchFactories.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f)
            }
        }
    }

    fun observeByCountry(countryCode: String): Flow<List<StarchFactory>> =
        dao.observeAllByCountry(countryCode)

    /** One-shot fetch of all factories by country */
    suspend fun findAllByCountry(countryCode: String): List<StarchFactory> =
        dao.findAllByCountry(countryCode)
}