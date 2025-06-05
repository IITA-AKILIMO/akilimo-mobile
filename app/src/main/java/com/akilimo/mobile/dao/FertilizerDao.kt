package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.Fertilizer

@Dao
interface FertilizerDao {

    @Query("SELECT * FROM fertilizers")
    fun listAll(): MutableList<Fertilizer>

    @Query("SELECT * FROM fertilizers LIMIT 1")
    fun findOne(): Fertilizer?

    @Query("select * FROM fertilizers where type=:fertilizerType")
    fun findByType(fertilizerType: String?): Fertilizer?

    @Query("select * FROM fertilizers where type=:fertilizerType and country_code=:countryCode limit 1")
    fun findOneByTypeAndCountry(fertilizerType: String, countryCode: String): Fertilizer?

    @Query("SELECT * FROM fertilizers where country_code=:countryCode and selected=1")
    fun findAllSelectedByCountry(countryCode: String): MutableList<Fertilizer>

    @Query("SELECT * FROM fertilizers where country_code=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<Fertilizer>


    @Query("SELECT * FROM fertilizers where country_code=:countryCode and use_case=:useCase")
    fun findAllByCountryAndUseCase(
        countryCode: String,
        useCase: String
    ): MutableList<Fertilizer>

    @Query("SELECT * FROM fertilizers where country_code=:countryCode and use_case=:useCase and selected=1")
    fun findAllSelectedByCountryAndUseCase(
        countryCode: String,
        useCase: String
    ): MutableList<Fertilizer>

    @Query("SELECT * FROM fertilizers WHERE country_code = :countryCode AND use_case IN (:useCases) AND selected = 1")
    fun findAllSelectedByCountryAndUseCases(
        countryCode: String,
        useCases: List<String>
    ): List<Fertilizer>

    @Query("SELECT * FROM fertilizers where type=:fertilizerType and country_code=:countryCode and use_case=:useCase")
    fun findOneByTypeCountryAndUseCase(
        fertilizerType: String,
        countryCode: String,
        useCase: String
    ): Fertilizer?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(fertilizer: Fertilizer)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(availableFertilizersList: List<Fertilizer>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSelected(selectedList: List<Fertilizer>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(fertilizer: Fertilizer?)

    @Delete
    fun delete(fertilizer: Fertilizer?)

    @Delete
    fun deleteFertilizerByList(fertilizerList: MutableList<Fertilizer>)

    @Query("DELETE FROM fertilizers")
    fun deleteAll()
}
