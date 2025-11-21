package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumUseCase
import kotlinx.coroutines.flow.Flow

@Dao
interface FertilizerDao {

    // Observable queries
    @Query("SELECT * FROM fertilizers")
    fun observeAll(): Flow<List<Fertilizer>>

    @Query("SELECT * FROM fertilizers WHERE country_code = :countryCode AND available = 1 ORDER BY sort_order ASC")
    fun observeAllByCountry(countryCode: EnumCountry): Flow<List<Fertilizer>>

    @Query("SELECT * FROM fertilizers WHERE country_code = :countryCode AND use_case=:useCase AND available = 1 ORDER BY sort_order ASC")
    fun observeAllByCountryAndUseCase(
        countryCode: EnumCountry,
        useCase: EnumUseCase
    ): Flow<List<Fertilizer>>


    @Query("SELECT * FROM fertilizers WHERE available = 1")
    fun getAll(): List<Fertilizer>

    @Query("SELECT * FROM fertilizers LIMIT 1")
    fun findOne(): Fertilizer?

    @Query("select * from fertilizers where type=:fertilizerType AND available = 1")
    fun findByType(fertilizerType: String): Fertilizer?

    @Query("select * from fertilizers where type=:fertilizerType and country_code=:countryCode AND available = 1 limit 1")
    fun findOneByTypeAndCountry(fertilizerType: String?, countryCode: EnumCountry): Fertilizer?

    @Query("SELECT * FROM fertilizers where country_code=:countryCode AND available = 1 ORDER BY sort_order ASC")
    fun findAllSelectedByCountry(countryCode: EnumCountry): List<Fertilizer>

    @Query("SELECT * FROM fertilizers where country_code=:countryCode AND available = 1 ORDER BY sort_order ASC")
    fun findAllByCountry(countryCode: EnumCountry): List<Fertilizer>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(fertilizer: Fertilizer): Long


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(availableFertilizersList: List<Fertilizer>)

    @Update
    fun update(fertilizer: Fertilizer)

    @Delete
    fun delete(fertilizer: Fertilizer)

    @Query("DELETE FROM fertilizers")
    fun deleteAll()
}
