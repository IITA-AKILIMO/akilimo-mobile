package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.FertilizerDao
import com.akilimo.mobile.entities.Fertilizer
import kotlinx.coroutines.flow.Flow

class FertilizerRepo(private val dao: FertilizerDao) {
    suspend fun saveAll(fertilizers: List<Fertilizer>) {
        fertilizers.forEach { f ->
            val rowId = dao.insert(f)
            if (rowId == -1L) {
                dao.update(f) // record exists, update in place
            }
        }
    }


    suspend fun byCountry(countryCode: String): List<Fertilizer> =
        dao.findAllByCountry(countryCode)

    suspend fun getAllFertilizers(): List<Fertilizer> {
        return  dao.getAll()
    }

    fun observeAll(): Flow<List<Fertilizer>> =
        dao.observeAll()

    fun observeByCountry(countryCode: String): Flow<List<Fertilizer>> =
        dao.observeAllByCountry(countryCode)


}
