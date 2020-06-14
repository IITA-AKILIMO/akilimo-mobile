package com.iita.akilimo.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Transient

@Entity
class CurrentFieldYield {
    @Id
    var id: Long = 0
    var yieldAmount = 0.0
    var fieldYieldLabel: String? = null

    @Transient
    var imageId = 0


}