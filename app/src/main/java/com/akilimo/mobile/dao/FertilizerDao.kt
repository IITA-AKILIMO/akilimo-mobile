package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumCountry
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerDao {

    // Observable queries
    @Query("SELECT * FROM fertilizers")
    fun observeAll(): Flow<List<Fertilizer>>

    @Query("SELECT * FROM fertilizers WHERE country_code = :countryCode AND available = 1 ORDER BY sort_order ASC")
    fun observeAllByCountry(countryCode: EnumCountry): Flow<List<Fertilizer>>

    @Query(
        "SELECT * FROM fertilizers WHERE country_code = :countryCode " +
            "AND cim_available = 1 AND available = 1 ORDER BY sort_order ASC"
    )
    fun observeAllByCimAvailable(countryCode: EnumCountry): Flow<List<Fertilizer>>

    @Query(
        "SELECT * FROM fertilizers WHERE country_code = :countryCode " +
            "AND cis_available = 1 AND available = 1 ORDER BY sort_order ASC"
    )
    fun observeAllByCisAvailable(countryCode: EnumCountry): Flow<List<Fertilizer>>


    @Query("SELECT * FROM fertilizers where country_code=:countryCode AND available = 1 ORDER BY sort_order ASC")
    suspend fun findAllByCountry(countryCode: EnumCountry): List<Fertilizer>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(fertilizer: Fertilizer): Long


    @Update
    suspend fun update(fertilizer: Fertilizer)

    /** Removes fertilizers the API no longer lists; a saved pick of one goes with it. */
    @Query("DELETE FROM fertilizers WHERE id NOT IN (:keepIds)")
    suspend fun deleteStale(keepIds: List<Int>)

}
