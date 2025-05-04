package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.UseCase

@Dao
interface UseCaseDao {

    @Query("SELECT * FROM use_cases")
    fun getAll(): List<UseCase>

    @Query("SELECT * FROM use_cases LIMIT 1")
    fun findOne(): UseCase?


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(useCases: UseCase)

    @Update
    fun update(useCase: UseCase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg useCases: UseCase)

    @Update
    fun updateAll(vararg useCases: UseCase)

    @Delete
    fun delete(useCase: UseCase?)
}
