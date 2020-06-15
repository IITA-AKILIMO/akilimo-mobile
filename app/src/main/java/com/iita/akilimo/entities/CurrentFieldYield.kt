package com.iita.akilimo.entities

import com.orm.SugarRecord


class CurrentFieldYield : SugarRecord<CurrentFieldYield>() {
    var id: Long = 0
    var yieldAmount = 0.0
    var fieldYieldLabel: String? = null

    @Transient
    var imageId = 0


}