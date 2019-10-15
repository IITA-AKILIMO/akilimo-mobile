package com.iita.akilimo.utils.enums

enum class EnumUnitOfSale {
    UNIT_ONE_KG {
        override fun unitWeight(): Int {
            return 1
        }

        override fun unitOfSale(): String {
            return "kg"
        }

        override fun unitOfSaleText(): String {
            return "a 1 kg bag"
        }
    },
    UNIT_FIFTY_KG {
        override fun unitWeight(): Int {
            return 50
        }

        override fun unitOfSale(): String {
            return "50 kg bag"
        }

        override fun unitOfSaleText(): String {
            return "a 50 kg bag"
        }
    },
    UNIT_HUNDRED_KG {
        override fun unitWeight(): Int {
            return 100
        }

        override fun unitOfSale(): String {
            return "100 kg bag"
        }

        override fun unitOfSaleText(): String {
            return "a 100 kg bag"
        }
    },
    UNIT_THOUSAND_KG {
        override fun unitWeight(): Int {
            return 1000
        }

        override fun unitOfSale(): String {
            return "tonne"
        }

        override fun unitOfSaleText(): String {
            return "1 tonne"
        }
    };

    abstract fun unitWeight(): Int
    abstract fun unitOfSale(): String
    abstract fun unitOfSaleText(): String
}