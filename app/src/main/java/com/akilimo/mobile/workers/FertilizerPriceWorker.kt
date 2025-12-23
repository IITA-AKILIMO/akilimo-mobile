package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.config.AppConfig
import com.akilimo.mobile.entities.FertilizerPrice
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.FertilizerPriceRepo
import timber.log.Timber

/**
 * Worker that pages through fertilizer prices and persists them locally.
 *
 * Notes:
 * - Uses the generic SafePagedWorker<TApi, TItem> contract.
 * - Sets serviceType to AKILIMO and creates a typed AkilimoApi via createApi.
 * - fetchPage returns PageResult<FertilizerPrice> and updateLocalDatabase accepts List<FertilizerPrice>.
 */
class FertilizerPriceWorker(
    appContext: Context,
    params: WorkerParameters,
) : SafePagedWorker<AkilimoApi, FertilizerPrice>(appContext, params) {

    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO

    // create a typed API client for this worker (kept cheap, uses ApiClient helper)
    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)

    private val repo by lazy { FertilizerPriceRepo(db.fertilizerPriceDao()) }

    private val perPage: Int = inputData.getInt("perPage", 100)

    override suspend fun fetchPage(page: Int): PageResult<FertilizerPrice> {
        val response = api.getFertilizerPrices(page = page, perPage = perPage)

        val items = response.data.map { it.toEntity() }

        val current = response.meta?.currentPage ?: page
        val lastPage = response.meta?.lastPage ?: current
        val totalPages = lastPage // when API provides lastPage, use it as totalPages

        return PageResult(
            items = items,
            isLastPage = current >= lastPage,
            totalPages = totalPages
        )
    }

    override suspend fun updateLocalDatabase(items: List<FertilizerPrice>) {
        if (items.isEmpty()) {
            Timber.w("No valid price items to save")
            return
        }
        Timber.d("Saving ${items.size} fertilizer prices to local DB")
        repo.saveAll(items)
    }
}