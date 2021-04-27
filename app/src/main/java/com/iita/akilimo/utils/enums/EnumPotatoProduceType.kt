package com.iita.akilimo.utils.enums

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
