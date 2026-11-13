package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.FertilizerDao
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumCountry
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


    /** Drops cached fertilizers missing from [current], the full list just fetched. */
    suspend fun pruneStale(current: List<Fertilizer>) {
        val ids = current.mapNotNull { it.id }
        if (ids.isNotEmpty()) dao.deleteStale(ids)
    }

    suspend fun byCountry(countryCode: EnumCountry): List<Fertilizer> =
        dao.findAllByCountry(countryCode)

    fun observeAll(): Flow<List<Fertilizer>> =
        dao.observeAll()

    fun observeByCountry(countryCode: EnumCountry): Flow<List<Fertilizer>> =
        dao.observeAllByCountry(countryCode)

    fun observeByCimAvailable(countryCode: EnumCountry): Flow<List<Fertilizer>> =
        dao.observeAllByCimAvailable(countryCode)

    fun observeByCisAvailable(countryCode: EnumCountry): Flow<List<Fertilizer>> =
        dao.observeAllByCisAvailable(countryCode)


}
