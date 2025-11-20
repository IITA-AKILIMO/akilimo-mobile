package com.akilimo.mobile.entities


import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "potato_price")
open class PotatoPrice {

    @PrimaryKey(autoGenerate = false)
    
    var priceIndex: Long = 0

    
    var priceId: Long = 0

    
    var country: String? = null

    
    var countryPrice: String? = null

    
    var minLocalPrice = 0.0

    
    var maxLocalPrice = 0.0

    
    var minUsd = 0.0

    
    var maxUsd = 0.0

    
    var active = false

    
    var averagePrice = 0.0

//    
//    var createdAt: Date? = null
//
//    
//    var updatedAt: Date? = null
}
