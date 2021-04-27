package com.iita.akilimo.utils.enums

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

    abstract fun produce(): String
}
