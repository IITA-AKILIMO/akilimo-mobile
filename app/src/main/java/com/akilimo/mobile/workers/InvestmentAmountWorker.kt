package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.InvestmentRepo
import timber.log.Timber

class InvestmentAmountWorker(
    appContext: Context,
    params: WorkerParameters,
) : SafePagedWorker<AkilimoApi, InvestmentAmount>(appContext, params) {
    private val repo by lazy { InvestmentRepo(db.investmentAmountDao()) }

    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO

    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)

    private val perPage = inputData.getInt("perPage", 100)

    override suspend fun fetchPage(page: Int): PageResult<InvestmentAmount> {
        val response = api.getInvestments(page = page, perPage = perPage)

        val items = response.data.map { it.toEntity() }
        val current = response.meta?.currentPage ?: page
        val last = response.meta?.lastPage ?: current
        val total = response.meta?.total ?: 0

        return PageResult(
            items = items,
            isLastPage = current >= last,
            totalPages = total
        )
    }

    override suspend fun updateLocalDatabase(items: List<InvestmentAmount>) {
        if (items.isEmpty()) {
            Timber.w("No valid Investments items to save")
            return
        }
        Timber.d("Saving ${items.size} investments to local DB")
        repo.saveAll(items)
    }
}
