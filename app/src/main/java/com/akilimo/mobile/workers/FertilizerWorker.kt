package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.FertilizerRepo
import timber.log.Timber

class FertilizerWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : SafePagedWorker<AkilimoApi, Fertilizer>(appContext, workerParams) {
    private val repo by lazy { FertilizerRepo(db.fertilizerDao()) }

    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO

    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)

    private val perPage = inputData.getInt("perPage", 100)

    override suspend fun fetchPage(page: Int): PageResult<Fertilizer> {
        val response = api.getFertilizers(page, perPage)

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

    override suspend fun updateLocalDatabase(items: List<Fertilizer>) {
        if (items.isEmpty()) {
            Timber.w("No valid Fertilizer items to save")
            return
        }
        Timber.d("Saving ${items.size} fertilizers to local DB")
        repo.saveAll(items)
    }

}
