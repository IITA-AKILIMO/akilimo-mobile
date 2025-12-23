package com.akilimo.mobile.enums


enum class EnumProduceType {
    MAIZE_GRAIN, MAIZE_FRESH_COB,

    SWEET_POTATO_TUBERS, SWEET_POTATO_FLOUR, UNKNOWN;

    fun produce(): String = when (this) {
        MAIZE_GRAIN -> "grain"
        MAIZE_FRESH_COB -> "fresh_cob"
        SWEET_POTATO_TUBERS -> "tubers"
        SWEET_POTATO_FLOUR -> "flour"
        UNKNOWN -> "Unknown"
    }
}
