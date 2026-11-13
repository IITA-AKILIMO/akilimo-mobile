package com.akilimo.mobile.repos

import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Everything a farmer has chosen or completed in the recommendation steps: fertilizers, the cassava
 * market and yield, the investment amount, maize performance, produce markets, field operation costs
 * and each step's completion status. These depend on the farmer's country (prices, currency,
 * fertilizers), so they are all cleared when the country changes. The profile itself is kept.
 */
@Singleton
class UserSelectionResetter @Inject constructor(
    private val selectedFertilizerRepo: SelectedFertilizerRepo,
    private val selectedCassavaMarketRepo: SelectedCassavaMarketRepo,
    private val selectedInvestmentRepo: SelectedInvestmentRepo,
    private val maizePerformanceRepo: MaizePerformanceRepo,
    private val produceMarketRepo: ProduceMarketRepo,
    private val fieldOperationCostsRepo: FieldOperationCostsRepo,
    private val adviceCompletionRepo: AdviceCompletionRepo,
) {

    /** True when the farmer has any choice or completed step that a country change would remove. */
    suspend fun hasAny(userId: Int): Boolean =
        selectedFertilizerRepo.getSelectedByUser(userId).isNotEmpty() ||
            selectedCassavaMarketRepo.getSelectedByUser(userId) != null ||
            selectedInvestmentRepo.getSelectedByUser(userId) != null ||
            maizePerformanceRepo.getPerformanceForUser(userId) != null ||
            fieldOperationCostsRepo.getCostForUser(userId) != null ||
            adviceCompletionRepo.getAllCompletions().first().isNotEmpty()

    /** Removes every choice and completion status of [userId]. */
    suspend fun clear(userId: Int) {
        selectedFertilizerRepo.deleteByUserId(userId)
        selectedCassavaMarketRepo.deselect(userId)
        selectedInvestmentRepo.clearForUser(userId)
        maizePerformanceRepo.deletePerformanceForUser(userId)
        produceMarketRepo.clearForUser(userId)
        fieldOperationCostsRepo.clearForUser(userId)
        adviceCompletionRepo.clearAll()
    }
}
