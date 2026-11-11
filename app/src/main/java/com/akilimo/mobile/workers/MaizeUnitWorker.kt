package com.akilimo.mobile.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.akilimo.mobile.base.workers.SafePagedWorker
import com.akilimo.mobile.entities.MaizeUnit
import com.akilimo.mobile.enums.EnumServiceType
import com.akilimo.mobile.network.AkilimoApi
import com.akilimo.mobile.network.ApiClient
import com.akilimo.mobile.repos.MaizeUnitRepo
import timber.log.Timber

/** Fetches the units maize grain is sold in. The endpoint is a single, unpaged list. */
class MaizeUnitWorker(
    appContext: Context,
    params: WorkerParameters,
) : SafePagedWorker<AkilimoApi, MaizeUnit>(appContext, params) {

    private val repo by lazy { MaizeUnitRepo(db.maizeUnitDao()) }

    override val serviceType: EnumServiceType = EnumServiceType.AKILIMO

    override fun createApi(context: Context, baseUrl: String): AkilimoApi =
        ApiClient.createService(context, baseUrl)

    override suspend fun fetchPage(page: Int): PageResult<MaizeUnit> =
        PageResult(items = api.getMaizeUnits().data.map { it.toEntity() }, isLastPage = true)

    override suspend fun updateLocalDatabase(items: List<MaizeUnit>) {
        Timber.d("Saving ${items.size} maize units")
        repo.saveAll(items)
    }

    override suspend fun afterAllPagesSaved(allItems: List<MaizeUnit>) = repo.pruneStale(allItems)
}
