package com.akilimo.mobile.enums

import com.akilimo.mobile.interfaces.IProduceType

enum class EnumMaizeProduceType : IProduceType {
    GRAIN {
        override fun produce() = "grain"
    },
    DRY_COB {
        override fun produce() = "dry_cob"
    },
    FRESH_COB {
        override fun produce() = "fresh_cob"
    };
}
