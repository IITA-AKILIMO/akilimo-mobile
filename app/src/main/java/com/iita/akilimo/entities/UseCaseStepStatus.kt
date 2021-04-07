package com.iita.akilimo.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "use_case_step_status", indices = [Index(value = ["step_name"], unique = true)])
open class UseCaseStepStatus {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @ColumnInfo(name = "step_name")
    var stepName: String = ""

    var completed: Boolean = false
}