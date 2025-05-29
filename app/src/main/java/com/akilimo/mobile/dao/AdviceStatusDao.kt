package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akilimo.mobile.entities.AdviceStatus

@Dao
interface AdviceStatusDao {

    @Query("SELECT * FROM advice_status")
    fun listAll(): List<AdviceStatus>

    @Query("SELECT * FROM advice_status where advice_name=:adviceName")
    fun findOne(adviceName: String): AdviceStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(adviceStatus: AdviceStatus)

    @Update
    fun update(adviceStatus: AdviceStatus)

    @Delete
    fun delete(adviceStatus: AdviceStatus?)

    @Query("DELETE FROM advice_status")
    fun deleteAll()
}
