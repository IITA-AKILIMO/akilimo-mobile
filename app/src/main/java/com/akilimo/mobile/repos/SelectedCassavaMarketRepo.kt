package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.SelectedCassavaMarketDao
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.relations.SelectedCassavaMarketWithDetails
import kotlinx.coroutines.flow.Flow

class SelectedCassavaMarketRepo(private val dao: SelectedCassavaMarketDao) {

    /**
     * Insert a selection and return true when insert succeeded.
     * This method does not read or update existing rows; it only inserts.
     */
    fun saveOrUpdate(selectedInvestment: SelectedCassavaMarket) {
        val existing = dao.getSelectedByUser(selectedInvestment.userId)
        val selectedCassavaMarket = existing?.selectedCassavaMarket
        selectedInvestment.createdAt =
            selectedCassavaMarket?.createdAt ?: System.currentTimeMillis()
        selectedInvestment.updatedAt = System.currentTimeMillis()

        if (existing != null) {
            dao.update(selectedInvestment)
        } else {
            dao.insert(selectedInvestment)
        }
    }

    suspend fun select(selected: SelectedCassavaMarket) {
        val existing = dao.getSelectedByUser(selected.userId)
        val selectedCassavaMarket = existing?.selectedCassavaMarket

        val updated = selectedCassavaMarket?.copy(
            starchFactoryId = selected.starchFactoryId,
            cassavaUnitId = selected.cassavaUnitId,
            unitOfSale = selected.unitOfSale,
            unitPrice = selected.unitPrice,
            marketPriceId = selected.marketPriceId,
            produceType = selected.produceType,
        ) ?: selected.copy()

        updated.createdAt = selectedCassavaMarket?.createdAt ?: System.currentTimeMillis()
        updated.updatedAt = System.currentTimeMillis()

        dao.upsert(updated)
    }

    suspend fun deselect(userId: Int) {
        dao.deleteSelected(userId)
    }

    suspend fun getSelectedByUser(userId: Int): SelectedCassavaMarketWithDetails? =
        dao.getSelectedByUser(userId)

    fun getSelectedByMarketPriceId(marketPriceId: Int): SelectedCassavaMarket? =
        dao.getSelectedByMarketPriceId(marketPriceId)

    fun getSelectedByStarchFactoryId(starchFactoryId: Int): SelectedCassavaMarket? =
        dao.getSelectedByStarchFactoryId(starchFactoryId)

    fun observeSelected(userId: Int): Flow<SelectedCassavaMarketWithDetails?> =
        dao.observeByUser(userId)

    fun observeByUserAndMarket(
        userId: Int,
        marketPriceId: Int
    ): Flow<SelectedCassavaMarketWithDetails?> =
        dao.observeByUserAndMarket(userId, marketPriceId)

    fun observeByUserAndFactory(
        userId: Int,
        factoryId: Int
    ): Flow<SelectedCassavaMarketWithDetails?> =
        dao.observeByUserAndFactory(userId, factoryId)


}
