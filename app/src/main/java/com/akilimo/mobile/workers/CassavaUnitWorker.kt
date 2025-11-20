package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.CassavaUnitRepo
import timber.log.Timber

class CassavaUnitWorker(
    appContext: Context,
    params: WorkerParameters,
) : SafePagedWorker<AkilimoApi, CassavaUnit>(appContext, params) {

    private val repo by lazy { CassavaUnitRepo(db.cassavaUnitDao()) }

    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO

    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)

    private val perPage = inputData.getInt("perPage", 200)

    override suspend fun fetchPage(page: Int): PageResult<CassavaUnit> {
        val response = api.getCassavaUnits(page = page, perPage = perPage)

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

    override suspend fun updateLocalDatabase(items: List<CassavaUnit>) {
        if (items.isEmpty()) {
            Timber.w("No valid cassava unit items to save")
            return
        }
        Timber.d("Saving ${items.size} cassava units")
        repo.saveAll(items)
    }
}
