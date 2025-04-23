package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.InterCropFertilizer

@Dao
interface FertilizerDao {

    @Query("SELECT * FROM fertilizers")
    fun listAll(): MutableList<Fertilizer>

    @Query("SELECT * FROM fertilizers LIMIT 1")
    fun findOne(): Fertilizer?

    @Query("select * FROM fertilizers where type=:fertilizerType")
    fun findByType(fertilizerType: String?): Fertilizer?

    @Query("select * FROM fertilizers where type=:fertilizerType and country_code=:countryCode limit 1")
    fun findOneByTypeAndCountry(fertilizerType: String?, countryCode: String): Fertilizer?

    @Query("SELECT * FROM fertilizers where country_code=:countryCode and selected=1")
    fun findAllSelectedByCountry(countryCode: String): MutableList<Fertilizer>

    @Query("SELECT * FROM fertilizers where country_code=:countryCode")
    fun findAllByCountry(countryCode: String): MutableList<Fertilizer>


    @Query("SELECT * FROM fertilizers where country_code=:countryCode and use_case=:useCase")
    fun findAllByCountryAndUseCase(
        countryCode: String,
        useCase: String
    ): List<Fertilizer>

    @Query("SELECT * FROM fertilizers where country_code=:countryCode and use_case=:useCase and selected=1")
    fun findAllSelectedByCountryAndUseCase(
        countryCode: String,
        useCase: String
    ): List<Fertilizer>

    @Query("SELECT * FROM fertilizers where type=:fertilizerType and country_code=:countryCode and use_case=:useCase")
    fun findByTypeCountryAndUseCase(
        fertilizerType: String,
        countryCode: String,
        useCase: String
    ): Fertilizer?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(fertilizer: Fertilizer)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(availableFertilizersList: List<Fertilizer>)

    @Update
    fun update(fertilizer: Fertilizer?)

    @Delete
    fun delete(fertilizer: Fertilizer?)

    @Delete
    fun deleteFertilizerByList(fertilizerList: MutableList<Fertilizer>)

    @Query("DELETE FROM fertilizers")
    fun deleteAll()
}
