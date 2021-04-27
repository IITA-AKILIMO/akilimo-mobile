package com.iita.akilimo.dao

import androidx.room.*
import com.iita.akilimo.entities.StarchFactory

@Dao
interface StarchFactoryDao {

    @Query("SELECT * FROM starch_factory")
    fun listAll(): List<StarchFactory>

    @Query("SELECT * FROM starch_factory LIMIT 1")
    fun findOne(): StarchFactory?

    @Query("SELECT * FROM starch_factory where countryCode=:countryCode and factorySelected=1")
    fun findOneByCountry(countryCode: String): StarchFactory?

    @Query("SELECT * FROM starch_factory where factoryName=:factoryName LIMIT 1")
    fun findStarchFactoryByName(factoryName: String): StarchFactory?


    @Query("SELECT * FROM starch_factory where factoryNameCountry=:factoryNameCountry LIMIT 1")
    fun findStarchFactoryByNameCountry(factoryNameCountry: String): StarchFactory?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: StarchFactory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(starchFactoriesList: List<StarchFactory>)

    @Update
    fun update(vararg users: StarchFactory)

    @Delete
    fun delete(user: StarchFactory?)

    @Query("select * from starch_factory where countryCode=:countryCode")
    fun findStarchFactoriesByCountry(countryCode: String): List<StarchFactory>

}
