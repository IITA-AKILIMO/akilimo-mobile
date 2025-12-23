package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.SelectedInvestmentDao
import com.akilimo.mobile.entities.SelectedInvestment
import kotlinx.coroutines.flow.Flow

class SelectedInvestmentRepo(private val dao: SelectedInvestmentDao) {

    /**
     * Insert a selection and return true when insert succeeded.
     * This method does not read or update existing rows; it only inserts.
     */
    fun saveOrUpdate(selectedInvestment: SelectedInvestment) {
        val existing = dao.getSelectedByUser(selectedInvestment.id)
        selectedInvestment.createdAt = existing?.createdAt ?: System.currentTimeMillis()
        selectedInvestment.updatedAt = System.currentTimeMillis()

        if (existing != null) {
            dao.update(selectedInvestment)
        } else {
            dao.insert(selectedInvestment)
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

}
