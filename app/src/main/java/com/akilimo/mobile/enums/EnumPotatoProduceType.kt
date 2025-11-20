package com.akilimo.mobile.enums

import com.akilimo.mobile.interfaces.IProduceType

enum class EnumPotatoProduceType : IProduceType {
    TUBERS {
        override fun produce() = "tubers"
    },
    FLOUR {
        override fun produce() = "flour"
    };


}
