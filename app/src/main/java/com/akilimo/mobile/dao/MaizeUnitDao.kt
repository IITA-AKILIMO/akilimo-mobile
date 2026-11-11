package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.akilimo.mobile.entities.MaizeUnit
import kotlinx.coroutines.flow.Flow

@Dao
interface MaizeUnitDao {

    @Query("SELECT * FROM maize_units WHERE is_active = 1 ORDER BY sort_order ASC")
    fun observeActive(): Flow<List<MaizeUnit>>

    @Upsert
    suspend fun upsertAll(units: List<MaizeUnit>)
}