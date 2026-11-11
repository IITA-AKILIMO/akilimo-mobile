package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.MaizeUnitDao
import com.akilimo.mobile.entities.MaizeUnit
import kotlinx.coroutines.flow.Flow

class MaizeUnitRepo(private val dao: MaizeUnitDao) {

    fun observeActive(): Flow<List<MaizeUnit>> = dao.observeActive()

    suspend fun saveAll(units: List<MaizeUnit>) = dao.upsertAll(units)

    /** Drops cached units missing from [current], the full list just fetched. */
    suspend fun pruneStale(current: List<MaizeUnit>) {
        if (current.isNotEmpty()) dao.deleteStale(current.map { it.id })
    }
}