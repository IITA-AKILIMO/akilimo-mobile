package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.StarchFactory

@Dao
interface StarchFactoryDao: BaseDao<StarchFactory> {

    @Query("SELECT * FROM starch_factories")
    fun listAll(): List<StarchFactory>

    @Query("SELECT * FROM starch_factories LIMIT 1")
    fun findOne(): StarchFactory?

    @Query("SELECT * FROM starch_factories where country_code=:countryCode and factory_selected=1")
    fun findOneByCountryCode(countryCode: String): StarchFactory?

    @Query("SELECT * FROM starch_factories where factory_name=:factoryName LIMIT 1")
    fun findStarchFactoryByName(factoryName: String): StarchFactory?


    @Query("SELECT * FROM starch_factories where factory_name_country=:factoryNameCountry LIMIT 1")
    fun findStarchFactoryByNameCountry(factoryNameCountry: String): StarchFactory?

    @Query("select * from starch_factories where country_code=:countryCode")
    fun findStarchFactoriesByCountry(countryCode: String): List<StarchFactory>

}
