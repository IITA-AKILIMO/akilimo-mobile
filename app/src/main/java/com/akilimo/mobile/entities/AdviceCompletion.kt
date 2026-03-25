package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.akilimo.mobile.entities.BaseEntity
import com.akilimo.mobile.enums.EnumAdviceTask
import com.akilimo.mobile.enums.EnumStepStatus

@Entity(tableName = "advice_completions")
data class AdviceCompletion(
    @PrimaryKey
    @ColumnInfo(name = "task_name") val taskName: EnumAdviceTask,
    @ColumnInfo(name = "step_status") val stepStatus: EnumStepStatus,
    @ColumnInfo(name = "competed_at") val completedAtMillis: Long? = null
) : BaseEntity()


