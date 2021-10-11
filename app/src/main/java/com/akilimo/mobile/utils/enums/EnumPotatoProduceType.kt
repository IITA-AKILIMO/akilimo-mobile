package com.akilimo.mobile.utils.enums

enum class EnumPotatoProduceType {
    TUBERS {
        override fun produce(): String {
            return "tubers"
        }
    },
    FLOUR {
        override fun produce(): String {
            return "flour"
        }
    };

    abstract fun produce(): String
}
