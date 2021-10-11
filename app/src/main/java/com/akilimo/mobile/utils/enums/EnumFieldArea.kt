package com.akilimo.mobile.utils.enums

enum class EnumFieldArea {
    UNKNOWN {
        override fun areaValue(): Double {
            return 0.0
        }
    },
    QUARTER_ACRE {
        override fun areaValue(): Double {
            return 0.25
        }
    },
    HALF_ACRE {
        override fun areaValue(): Double {
            return 0.5
        }
    },
    ONE_ACRE {
        override fun areaValue(): Double {
            return 1.0
        }
    },
    ONE_HALF_ACRE {
        override fun areaValue(): Double {
            return 1.5
        }
    },
    TWO_HALF_ACRE {
        override fun areaValue(): Double {
            return 2.5
        }
    },
    FIVE_ACRE {
        override fun areaValue(): Double {
            return 5.0
        }
    },
    EXACT_AREA {
        override fun areaValue(): Double {
            return (-1).toDouble()
        }
    };

    abstract fun areaValue(): Double
}
