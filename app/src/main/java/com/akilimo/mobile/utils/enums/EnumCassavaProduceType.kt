package com.akilimo.mobile.utils.enums

enum class EnumCassavaProduceType {
    GARI {
        override fun produce(): String {
            return "gari"
        }
    },
    FLOUR {
        override fun produce(): String {
            return "flour"
        }
    },
    ROOTS {
        override fun produce(): String {
            return "roots"
        }
    },
    CHIPS {
        override fun produce(): String {
            return "chips"
        }
    };

    @Deprecated("Use name instead")
    abstract fun produce(): String
}
