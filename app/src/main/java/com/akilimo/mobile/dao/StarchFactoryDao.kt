package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.StarchFactory

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
    fun insert(vararg starchFactories: StarchFactory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(starchFactoriesList: List<StarchFactory>)

    @Update
    fun update(vararg starchFactories: StarchFactory)

    @Delete
    fun delete(starchFactory: StarchFactory?)

    @Query("select * from starch_factory where countryCode=:countryCode")
    fun findStarchFactoriesByCountry(countryCode: String): List<StarchFactory>
    @Query("DELETE FROM starch_factory")
    fun deleteAll()

}
