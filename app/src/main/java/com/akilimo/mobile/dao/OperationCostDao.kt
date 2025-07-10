package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Query
import com.akilimo.mobile.entities.OperationCost

@Dao
interface OperationCostDao : BaseDao<OperationCost> {

    @Query("SELECT * FROM operation_costs")
    fun findAll(): List<OperationCost>

    @Query("SELECT * FROM operation_costs WHERE operation_name = :operationName AND operation_type = :operationType AND country_code = :countryCode")
    fun findAllFiltered(
        operationName: String,
        operationType: String,
        countryCode: String
    ): List<OperationCost>

    @Query("SELECT * FROM operation_costs LIMIT 1")
    fun findOne(): OperationCost?

    @Query("SELECT * FROM operation_costs where item_tag = :itemTag")
    fun findOneByItemTag(itemTag: String): OperationCost?

}
