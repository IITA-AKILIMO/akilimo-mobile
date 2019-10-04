package com.iita.akilimo.utils.enums;

public enum EnumProduceType {
    GARI {
        @Override
        public String produce() {
            return "Gari";
        }
    },
    FLOUR {
        @Override
        public String produce() {
            return "Flour";
        }
    },
    ROOTS {
        @Override
        public String produce() {
            return "Roots";
        }
    },
    CHIPS {
        @Override
        public String produce() {
            return "Chips";
        }
    };

    public abstract String produce();
}