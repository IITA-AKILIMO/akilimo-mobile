package com.akilimo.mobile.utils.enums

enum class EnumMaizeProduceType {
    GRAIN {
        override fun produce(): String {
            return "grain"
        }
    },
    DRY_COB {
        override fun produce(): String {
            return "dry_cob"
        }
    },
    FRESH_COB {
        override fun produce(): String {
            return "fresh_cob"
        }
    };

    abstract fun produce(): String
}
