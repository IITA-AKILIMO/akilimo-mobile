package com.akilimo.mobile.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "currency",
    indices = [Index(value = ["currencyCode"], unique = true)]
)

open class Currency {

    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    
    var country: String? = null

    
    var currencyName: String? = null

    
    var currencyCode: String? = null

    
    var currencySymbol: String? = null

    
    var currencyNativeSymbol: String? = null

    
    var namePlural: String? = null
}
