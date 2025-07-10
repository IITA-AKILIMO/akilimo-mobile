package com.akilimo.mobile.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update

interface BaseDao<Entity> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: Entity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(entities: List<Entity>): List<Long>

    @Update
    fun update(entity: Entity): Int

    @Update
    fun updateAll(entities: List<Entity>): Int

    @Delete
    fun delete(entity: Entity): Int

    @Delete
    fun deleteAll(entities: List<Entity>): Int
}
