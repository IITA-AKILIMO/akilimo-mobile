package com.akilimo.mobile.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<Entity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: Entity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<Entity>): List<Long>

    @Update
    suspend fun update(entity: Entity): Int

    @Update
    suspend fun updateAll(entities: List<Entity>): Int

    @Delete
    suspend fun delete(entity: Entity): Int

    @Delete
    suspend fun deleteAll(entities: List<Entity>): Int
}
