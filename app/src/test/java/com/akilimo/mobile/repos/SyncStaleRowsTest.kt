package com.akilimo.mobile.repos

import com.akilimo.mobile.dao.CassavaUnitDao
import com.akilimo.mobile.dao.FertilizerDao
import com.akilimo.mobile.dao.MaizeUnitDao
import com.akilimo.mobile.dao.SavedFactoryPick
import com.akilimo.mobile.dao.StarchFactoryDao
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.MaizeUnit
import com.akilimo.mobile.entities.StarchFactory
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Rows the API stops serving must leave the app too, and saved picks must survive id changes. */
class SyncStaleRowsTest {

    @Test
    fun `fertilizers keep only the ids just fetched`() = runTest {
        val dao = mockk<FertilizerDao>(relaxed = true)
        FertilizerRepo(dao).pruneStale(listOf(Fertilizer(id = 1), Fertilizer(id = 5)))
        coVerify { dao.deleteStale(listOf(1, 5)) }
    }

    @Test
    fun `cassava units and maize units keep only the ids just fetched`() = runTest {
        val cassava = mockk<CassavaUnitDao>(relaxed = true)
        val maize = mockk<MaizeUnitDao>(relaxed = true)
        CassavaUnitRepo(cassava).pruneStale(listOf(CassavaUnit(id = 2, label = "ONE_KG", description = null)))
        MaizeUnitRepo(maize).pruneStale(listOf(MaizeUnit(7, "ONE_KG", 1.0, 1, false, true)))
        coVerify { cassava.pruneStale(listOf(2)) }
        coVerify { maize.deleteStale(listOf(7)) }
    }

    @Test
    fun `an empty fetch never wipes the cache`() = runTest {
        val fertilizer = mockk<FertilizerDao>(relaxed = true)
        val starch = mockk<StarchFactoryDao>(relaxed = true)
        FertilizerRepo(fertilizer).pruneStale(emptyList())
        StarchFactoryRepo(starch).replaceAll(emptyList())
        coVerify(exactly = 0) { fertilizer.deleteStale(any()) }
        coVerify(exactly = 0) { starch.replaceAll(any()) }
    }

    @Test
    fun `renumbered starch factories keep the saved pick by name and country`() = runTest {
        val dao = FakeStarchDao(
            factories = mutableListOf(
                StarchFactory(id = 1, name = "AyensuStarch", countryCode = "GH"),
                StarchFactory(id = 3, name = "AlliedAtlantic", countryCode = "NG"),
            ),
            picks = mutableMapOf(100 to 3 as Int?, 101 to 1 as Int?),
        )

        // the server renumbers: Allied Atlantic is now 1, Ayensu is gone
        dao.replaceAll(
            listOf(
                StarchFactory(id = 1, name = "AlliedAtlantic", countryCode = "NG"),
                StarchFactory(id = 2, name = "Greentech", countryCode = "NG"),
            )
        )

        assertEquals(1, dao.picks[100]) // re-pointed to the new id
        assertEquals(null, dao.picks[101]) // its factory is gone: the pick keeps its other answers
        assertEquals(listOf(1, 2), dao.factories.map { it.id })
    }

    private class FakeStarchDao(
        val factories: MutableList<StarchFactory>,
        val picks: MutableMap<Int, Int?>,
    ) : StarchFactoryDao {
        override suspend fun getEvery() = factories.toList()
        override suspend fun savedFactoryPicks() =
            picks.filterValues { it != null }.map { SavedFactoryPick(it.key, it.value!!) }
        override suspend fun detachSavedPicks() { picks.keys.toList().forEach { picks[it] = null } }
        override suspend fun pointPickAt(pickId: Int, factoryId: Int) { picks[pickId] = factoryId }
        override suspend fun clear() = factories.clear()
        override suspend fun insertAll(factories: List<StarchFactory>) { this.factories += factories }

        override fun observeAll(): Flow<List<StarchFactory>> = emptyFlow()
        override fun observeAllByCountry(countryCode: String): Flow<List<StarchFactory>> = emptyFlow()
        override suspend fun getAll() = factories.toList()
        override suspend fun findOne(): StarchFactory? = null
        override suspend fun findOneByCountry(countryCode: String): StarchFactory? = null
        override suspend fun findAllByCountry(countryCode: String) = emptyList<StarchFactory>()
        override suspend fun insert(starchFactory: StarchFactory) = -1L
        override suspend fun update(starchFactory: StarchFactory) = Unit
        override suspend fun delete(starchFactory: StarchFactory) = Unit
    }
}
