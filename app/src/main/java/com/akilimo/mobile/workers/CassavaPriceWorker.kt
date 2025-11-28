package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import timber.log.Timber

class CassavaPriceWorker(
    appContext: Context,
    params: WorkerParameters,
) : SafePagedWorker<AkilimoApi, CassavaMarketPrice>(appContext, params) {

    private val repo by lazy { CassavaMarketPriceRepo(db.cassavaMarketPriceDao()) }
    private val perPage: Int = inputData.getInt("perPage", 100)

    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO
    
    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)


    override suspend fun fetchPage(page: Int): PageResult<CassavaMarketPrice> {
        val response = api.getCassavaMarketPrices(page = page, perPage = perPage)

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

    override suspend fun updateLocalDatabase(items: List<CassavaMarketPrice>) {
        if (items.isEmpty()) {
            Timber.w("No valid cassava market items to save")
            return
        }
        Timber.d("Saving ${items.size} cassava market prices")
        repo.saveAll(items)
    }
}
