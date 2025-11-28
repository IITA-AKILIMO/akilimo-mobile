package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.CassavaUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface CassavaUnitDao {

    @Query("SELECT * FROM cassava_units ORDER BY sort_order ASC")
    fun observeAll(): Flow<List<CassavaUnit>>

    @Query("SELECT * FROM cassava_units WHERE id = :unitId LIMIT 1")
    fun getById(unitId: Int): CassavaUnit?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(unit: CassavaUnit): Long

    @Update
    fun update(unit: CassavaUnit)

    @Query("DELETE FROM cassava_units")
    fun clear()
}
