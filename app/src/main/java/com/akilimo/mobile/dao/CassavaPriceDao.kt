package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaPrice

@Dao
interface CassavaPriceDao {

    @Query("SELECT * FROM cassava_prices")
    fun listAll(): List<CassavaPrice>

    @Query("SELECT * FROM cassava_prices LIMIT 1")
    fun findOne(): CassavaPrice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg prices: CassavaPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cassavaPriceList: List<CassavaPrice>)

    @Update
    fun update(vararg price: CassavaPrice)

    @Delete
    fun delete(market: CassavaPrice?)

    @Query("SELECT * FROM cassava_prices where country_code=:countryCode")
    fun findAllByCountryCode(countryCode: String): List<CassavaPrice>

    @Query("DELETE FROM cassava_prices")
    fun deleteAll()

    @Query("SELECT * FROM cassava_prices where item_tag=:itemTag")
    fun findByItemTag(itemTag: String): CassavaPrice?
}
