package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.akilimo.mobile.base.BaseEntity

/**
 * CassavaYield persisted entity.
 *
 * - id is Long to avoid overflow and for consistency with other repos.
 * - label is non-null and expected to be unique (indexed).
 * - createdAt/updatedAt are stored as epoch millis to help auditing and sync decisions.
 * - transient UI-only fields are annotated with @Ignore so Room will skip them.
 */
@Entity(
    tableName = "cassava_yields",
    indices = [Index(value = ["yield_label"], unique = true)]
)
data class CassavaYield(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "yield_amount")
    val yieldAmount: Double = 0.0,
    @ColumnInfo(name = "image_res")
    var imageRes: Int,
    @ColumnInfo(name = "yield_label")
    val yieldLabel: String = "",
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
) : BaseEntity() {

    // UI / runtime only fields
    @Ignore
    var description: String? = null

    @Ignore
    var amountLabel: String? = null

    /**
     * Return a copy with updated selection flag (useful for optimistic UI updates).
     */
    fun copyWithSelection(selected: Boolean): CassavaYield {
        val copy = this.copy()
        copy.isSelected = selected
        return copy
    }

    companion object {
        /**
         * Simple factory to build a CassavaYield from resources / UI mapping.
         * Use this when seeding default yields from drawables.
         */
        fun create(
            yieldAmount: Double,
            yieldLabel: String,
            imageRes: Int = 0,
            description: String? = null
        ): CassavaYield {
            return CassavaYield(
                id = 0,
                imageRes = imageRes,
                yieldAmount = yieldAmount,
                yieldLabel = yieldLabel.trim(),
            ).also {
                it.description = description
                it.createdAt = System.currentTimeMillis()
                it.updatedAt = System.currentTimeMillis()
            }
        }
    }
}
