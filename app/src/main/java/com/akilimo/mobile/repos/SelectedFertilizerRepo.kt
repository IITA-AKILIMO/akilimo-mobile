package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.SelectedFertilizerDao
import com.akilimo.mobile.entities.SelectedFertilizer
import kotlinx.coroutines.flow.Flow

class SelectedFertilizerRepo(private val dao: SelectedFertilizerDao) {

    /**
     * Insert a selection and return true when insert succeeded.
     * This method does not read or update existing rows; it only inserts.
     */
    suspend fun select(selected: SelectedFertilizer) {
        val existing = dao.getSelectedByFertilizer(selected.fertilizerId)

        val updated = SelectedFertilizer(
            id = existing?.id ?: 0,
            userId = selected.userId,
            fertilizerId = selected.fertilizerId,
            fertilizerPriceId = selected.fertilizerPriceId,
            fertilizerPrice = selected.fertilizerPrice,
            displayPrice = selected.displayPrice,
            isExactPrice = selected.isExactPrice,
        )

        updated.createdAt = existing?.createdAt ?: System.currentTimeMillis()
        updated.updatedAt = System.currentTimeMillis()

        dao.upsert(updated)
    }

    suspend fun deselect(userId: Int, fertilizerId: Int) {
        dao.deleteSelected(userId, fertilizerId)
    }

    fun observeSelected(userId: Int): Flow<List<SelectedFertilizer>> =
        dao.observeByUser(userId)

    suspend fun getSelectedByFertilizer(fertilizerId: Int): SelectedFertilizer? {
        return dao.getSelectedByFertilizer(fertilizerId)
    }

    suspend fun getSelectedByUser(userId: Int): List<SelectedFertilizer> {
        return dao.getSelectedByUser(userId)
    }

    suspend fun getSelectedSync(userId: Int): List<SelectedFertilizer> {
        return dao.getSelectedByUser(userId)
    }

    suspend fun deleteByUserId(userId: Int) {
        dao.deleteAllByUser(userId)
    }


}
