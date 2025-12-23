package com.akilimo.mobile.enums

import com.akilimo.mobile.interfaces.IProduceType

enum class EnumCassavaProduceType : IProduceType {
    GARI {
        override fun produce() = "gari"
    },
    FLOUR {
        override fun produce() = "flour"
    },
    ROOTS {
        override fun produce() = "roots"
    },
    CHIPS {
        override fun produce() = "chips"
    };

}
