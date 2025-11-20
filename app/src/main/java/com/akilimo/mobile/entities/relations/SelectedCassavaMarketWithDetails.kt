package com.akilimo.mobile.entities.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.CassavaYield
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.StarchFactory

data class SelectedCassavaMarketWithDetails(
    @Embedded val selectedCassavaMarket: SelectedCassavaMarket,

    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: AkilimoUser? = null,

    @Relation(
        parentColumn = "market_price_id",
        entityColumn = "id"
    )
    val marketPrice: CassavaMarketPrice? = null,

    @Relation(
        parentColumn = "cassava_unit_id",
        entityColumn = "id"
    )
    val cassavaUnit: CassavaUnit? = null,

    @Relation(
        parentColumn = "starch_factory_id",
        entityColumn = "id"
    )
    val starchFactory: StarchFactory? = null,

    @Relation(
        parentColumn = "yield_id",
        entityColumn = "id"
    )
    val cassavaYield: CassavaYield? = null
)
