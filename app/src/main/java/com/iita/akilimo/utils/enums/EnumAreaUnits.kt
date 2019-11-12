package com.iita.akilimo.utils.enums

enum class EnumAreaUnits {
    UNKNOWN {
        override fun unitString(): String {
            return "NA"
        }
    },
    ACRE {
        override fun unitString(): String {
            return "acre"
        }
    },
    HA {
        override fun unitString(): String {
            return "ha"
        }
    },
    SQM {
        override fun unitString(): String {
            return "m2"
        }
    };

    abstract fun unitString(): String

}
