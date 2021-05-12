package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.Fertilizer
import com.iita.akilimo.entities.InterCropFertilizer

@Deprecated("To be removed when app is stable")
@Dao
interface InterCropFertilizerDao {

    @Query("SELECT * FROM intercrop_fertilizer")
    fun listAll(): List<InterCropFertilizer>

    @Query("SELECT * FROM intercrop_fertilizer LIMIT 1")
    fun findOne(): InterCropFertilizer?

    @Query("SELECT * FROM intercrop_fertilizer where countryCode=:countryCode")
    fun findAllByCountry(countryCode: String): List<InterCropFertilizer>

    @Query("SELECT * FROM intercrop_fertilizer where fertilizerType=:fertilizerType and countryCode=:countryCode and useCase=:useCase")
    fun findByTypeCountryAndUseCase(
        fertilizerType: String,
        countryCode: String,
        useCase: String
    ): InterCropFertilizer?

    @Query("SELECT * FROM intercrop_fertilizer where countryCode=:countryCode and useCase=:useCase")
    fun findAllByCountryAndUseCase(
        countryCode: String,
        useCase: String
    ): List<InterCropFertilizer>

    @Query("SELECT * FROM intercrop_fertilizer where countryCode=:countryCode and selected=1")
    fun findAllSelectedByCountry(countryCode: String): List<InterCropFertilizer>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg interCropFertilizer: InterCropFertilizer)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(availableFertilizersList: List<InterCropFertilizer>)

    @Update
    fun update(vararg interCropFertilizer: InterCropFertilizer)

    @Delete
    fun delete(interCropFertilizer: InterCropFertilizer)

    @Query("SELECT * FROM intercrop_fertilizer where countryCode=:countryCode and useCase=:useCase and selected=1")
    fun findAllSelectedByCountryAndUseCase(
        countryCode: String,
        useCase: String
    ): List<InterCropFertilizer>

    @Query("select * from intercrop_fertilizer where fertilizerType=:fertilizerType")
    fun findByType(fertilizerType: String?): InterCropFertilizer?

    fun deleteFertilizerByList(deletionList: List<InterCropFertilizer>)

}
