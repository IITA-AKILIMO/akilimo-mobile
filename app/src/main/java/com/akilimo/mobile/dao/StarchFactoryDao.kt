package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.StarchFactory
import kotlinx.coroutines.flow.Flow

@Dao
interface StarchFactoryDao {

    @Query("SELECT * FROM starch_factories WHERE is_active = 1")
    fun observeAll(): Flow<List<StarchFactory>>

    @Query("SELECT * FROM starch_factories WHERE country_code = :countryCode AND is_active = 1 ORDER BY sort_order ASC")
    fun observeAllByCountry(countryCode: String): Flow<List<StarchFactory>>


    @Query("SELECT * FROM starch_factories WHERE is_active = 1 ")
    fun getAll(): List<StarchFactory>

    @Query("SELECT * FROM starch_factories WHERE is_active = 1  LIMIT 1")
    fun findOne(): StarchFactory?


    @Query("select * from starch_factories where country_code=:countryCode AND is_active = 1  limit 1")
    fun findOneByCountry(countryCode: String): StarchFactory?


    @Query("SELECT * FROM starch_factories where country_code=:countryCode AND is_active = 1  ORDER BY sort_order ASC")
    fun findAllByCountry(countryCode: String): List<StarchFactory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(starchFactory: StarchFactory): Long

    @Update
    fun update(starchFactory: StarchFactory)

    @Delete
    fun delete(starchFactory: StarchFactory)
}
