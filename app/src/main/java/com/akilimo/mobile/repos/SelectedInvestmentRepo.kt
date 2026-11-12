package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.SelectedInvestmentDao
import com.akilimo.mobile.entities.SelectedInvestment
import kotlinx.coroutines.flow.Flow

class SelectedInvestmentRepo(private val dao: SelectedInvestmentDao) {

    /**
     * Save the user's investment selection: updates the user's existing row
     * (looked up by userId) or inserts a new one.
     */
    suspend fun saveOrUpdate(selectedInvestment: SelectedInvestment) {
        val existing = dao.getSelectedByUser(selectedInvestment.userId)
        val toSave = if (existing != null) selectedInvestment.copy(id = existing.id) else selectedInvestment
        toSave.createdAt = existing?.createdAt ?: System.currentTimeMillis()
        toSave.updatedAt = System.currentTimeMillis()

        if (existing != null) {
            dao.update(toSave)
        } else {
            dao.insert(toSave)
        }
    }

    suspend fun select(selected: SelectedInvestment) {
        val existing = dao.getSelectedByInvestmentId(selected.investmentId)

        val updated = SelectedInvestment(
            id = existing?.id ?: 0,
            userId = selected.userId,
            investmentId = selected.investmentId,
            chosenAmount = selected.chosenAmount,
            isExactAmount = selected.isExactAmount
        )

        updated.createdAt = existing?.createdAt ?: System.currentTimeMillis()
        updated.updatedAt = System.currentTimeMillis()

        dao.upsert(updated)
    }

    suspend fun deselect(userId: Int, fertilizerId: Int) {
        dao.deleteSelected(userId, fertilizerId)
    }

    fun observeSelected(userId: Int): Flow<SelectedInvestment?> =
        dao.observeByUser(userId)

    suspend fun getSelectedByUser(userId: Int): SelectedInvestment? =
        dao.getSelectedByUser(userId)

    suspend fun getSelectedByFertilizer(investmentId: Int): SelectedInvestment? {
        return dao.getSelectedByInvestmentId(investmentId)
    }

    suspend fun getSelectedSync(userId: Int): SelectedInvestment? {
        return dao.getSelectedByUser(userId)
    }


    suspend fun clearForUser(userId: Int) = dao.deleteAllByUser(userId)
}
