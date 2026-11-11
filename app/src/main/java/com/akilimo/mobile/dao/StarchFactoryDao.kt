package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.akilimo.mobile.entities.StarchFactory
import kotlinx.coroutines.flow.Flow

@Dao
interface StarchFactoryDao {

    @Query("SELECT * FROM starch_factories WHERE is_active = 1")
    fun observeAll(): Flow<List<StarchFactory>>

    @Query("SELECT * FROM starch_factories WHERE country_code = :countryCode AND is_active = 1 ORDER BY sort_order ASC")
    fun observeAllByCountry(countryCode: String): Flow<List<StarchFactory>>


    @Query("SELECT * FROM starch_factories WHERE is_active = 1 ")
    suspend fun getAll(): List<StarchFactory>

    @Query("SELECT * FROM starch_factories WHERE is_active = 1  LIMIT 1")
    suspend fun findOne(): StarchFactory?


    @Query("select * from starch_factories where country_code=:countryCode AND is_active = 1  limit 1")
    suspend fun findOneByCountry(countryCode: String): StarchFactory?


    @Query("SELECT * FROM starch_factories where country_code=:countryCode AND is_active = 1  ORDER BY sort_order ASC")
    suspend fun findAllByCountry(countryCode: String): List<StarchFactory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(starchFactory: StarchFactory): Long

    @Update
    suspend fun update(starchFactory: StarchFactory)

    @Delete
    suspend fun delete(starchFactory: StarchFactory)

    @Query("SELECT * FROM starch_factories")
    suspend fun getEvery(): List<StarchFactory>

    @Query("SELECT id, starch_factory_id AS factoryId FROM selected_cassava_markets WHERE starch_factory_id IS NOT NULL")
    suspend fun savedFactoryPicks(): List<SavedFactoryPick>

    @Query("UPDATE selected_cassava_markets SET starch_factory_id = NULL WHERE starch_factory_id IS NOT NULL")
    suspend fun detachSavedPicks()

    @Query("UPDATE selected_cassava_markets SET starch_factory_id = :factoryId WHERE id = :pickId")
    suspend fun pointPickAt(pickId: Int, factoryId: Int)

    @Query("DELETE FROM starch_factories")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(factories: List<StarchFactory>)

    /**
     * Replaces the cached factories with the fetched list. The server can renumber factories, so a
     * plain upsert by id can clash with the unique name-and-country rule; saved picks are re-pointed
     * by name and country instead, and a pick whose factory is gone keeps its other answers.
     */
    @Transaction
    suspend fun replaceAll(fetched: List<StarchFactory>) {
        val oldById = getEvery().associateBy { it.id }
        val picks = savedFactoryPicks()
        detachSavedPicks()
        clear()
        insertAll(fetched)
        val newIdByKey = fetched.associate { (it.name to it.countryCode) to it.id }
        picks.forEach { pick ->
            val old = oldById[pick.factoryId] ?: return@forEach
            val newId = newIdByKey[old.name to old.countryCode] ?: return@forEach
            pointPickAt(pick.id, newId)
        }
    }
}

/** A saved cassava market answer and the starch factory it points at. */
data class SavedFactoryPick(val id: Int, val factoryId: Int)
