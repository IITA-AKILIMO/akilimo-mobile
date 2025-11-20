package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.StarchFactoryRepo
import timber.log.Timber

class StarchFactoryWorker(
    appContext: Context,
    params: WorkerParameters,
) : SafePagedWorker<AkilimoApi, StarchFactory>(appContext, params) {

    private val repo by lazy { StarchFactoryRepo(db.starchFactoryDao()) }
    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO

    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)

    private val perPage = inputData.getInt("perPage", 200)

    override suspend fun fetchPage(page: Int): PageResult<StarchFactory> {
        val response = api.getStarchFactories(page = page, perPage = perPage)

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

    override suspend fun updateLocalDatabase(items: List<StarchFactory>) {
        if (items.isEmpty()) {
            Timber.w("No valid factory items to save")
            return
        }
        Timber.d("Saving ${items.size} factories")
        repo.saveAll(items)
    }
}
