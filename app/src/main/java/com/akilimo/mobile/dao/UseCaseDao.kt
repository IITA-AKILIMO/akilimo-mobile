package com.akilimo.mobile.dao

import androidx.room.*
import com.akilimo.mobile.entities.UseCases

@Dao
interface UseCaseDao {

    @Query("SELECT * FROM use_case")
    fun listAll(): List<UseCases>

    @Query("SELECT * FROM use_case LIMIT 1")
    fun findOne(): UseCases?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg users: UseCases)

    @Update
    fun update(vararg users: UseCases)

    @Delete
    fun delete(user: UseCases?)
}
