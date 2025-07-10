package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.CassavaPrice

@Dao
interface CassavaPriceDao: BaseDao<CassavaPrice> {

    @Query("SELECT * FROM cassava_prices")
    fun listAll(): List<CassavaPrice>

    @Query("SELECT * FROM cassava_prices LIMIT 1")
    fun findOne(): CassavaPrice?

    @Query("SELECT * FROM cassava_prices where country_code=:countryCode")
    fun findAllByCountryCode(countryCode: String): List<CassavaPrice>


    @Query("SELECT * FROM cassava_prices where item_tag=:itemTag")
    fun findByItemTag(itemTag: String): CassavaPrice?
}
