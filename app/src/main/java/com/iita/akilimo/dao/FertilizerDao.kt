package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.Fertilizer

@Dao
interface FertilizerDao {

    @Query("SELECT * FROM fertilizer")
    fun listAll(): List<Fertilizer>

    @Query("SELECT * FROM fertilizer LIMIT 1")
    fun findOne(): Fertilizer?

    @Query("select * from fertilizer where fertilizerType=:fertilizerType and countryCode=:countryCode limit 1")
    fun findOneByTypeAndCountry(fertilizerType: String?, countryCode: String): Fertilizer?

    @Query("SELECT * FROM fertilizer where countryCode=:countryCode and selected=1")
    fun findAllSelectedByCountry(countryCode: String): List<Fertilizer>

    @Query("SELECT * FROM fertilizer where countryCode=:countryCode")
    fun findAllByCountry(countryCode: String): List<Fertilizer>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(fieldYield: Fertilizer)


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(availableFertilizersList: List<Fertilizer>)

    @Update
    fun update(fieldYield: Fertilizer?)

    @Delete
    fun delete(fieldYield: Fertilizer?)
}