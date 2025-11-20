package com.akilimo.mobile.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.FieldOperationCost

class UserWithDetails {
    @Embedded
    val user: AkilimoUser? = null

    @Relation(
        parentColumn = "id",
        entityColumn = "user_id"
    )
    val fieldOperationCosts: List<FieldOperationCost> = emptyList()
}