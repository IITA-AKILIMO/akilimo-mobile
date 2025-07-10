package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.CurrentPractice

@Dao
interface CurrentPracticeDao : BaseDao<CurrentPractice> {

    @Query("SELECT * FROM current_practices")
    fun listAll(): List<CurrentPractice>

    @Query("SELECT * FROM current_practices LIMIT 1")
    fun findOne(): CurrentPractice?
}
