package com.iita.akilimo.utils.enums;

public enum EnumProduceType {
    GARI {
        @Override
        public String produce() {
            return "gari";
        }
    },
    FLOUR {
        @Override
        public String produce() {
            return "flour";
        }
    },
    ROOTS {
        @Override
        public String produce() {
            return "roots";
        }
    },
    CHIPS {
        @Override
        public String produce() {
            return "chips";
        }
    };

    public abstract String produce();
}