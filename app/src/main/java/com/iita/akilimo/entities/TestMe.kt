package com.iita.akilimo.entities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class TestMe(
    @Id var id: Long = 0,
    var name: String? = null
)