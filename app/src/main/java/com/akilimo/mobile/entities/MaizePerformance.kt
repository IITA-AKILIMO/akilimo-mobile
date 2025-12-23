package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity
import com.akilimo.mobile.enums.EnumMaizePerformance

@Entity(
    tableName = "maize_performance",
    indices = [Index(value = ["user_id"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = AkilimoUser::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
    ]
)
data class MaizePerformance(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int? = null,
    @ColumnInfo(name = "user_id")
    val userId: Int,
    @ColumnInfo(name = "maize_performance")
    val maizePerformance: EnumMaizePerformance,
) : BaseEntity()
