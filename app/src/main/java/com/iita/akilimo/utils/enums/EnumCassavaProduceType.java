package com.iita.akilimo.utils.enums;

public enum EnumCassavaProduceType {
    @Deprecated
    GARI {
        @Override
        public String produce() {
            return "gari";
        }
    },
    @Deprecated
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
    @Deprecated
    CHIPS {
        @Override
        public String produce() {
            return "chips";
        }
    };

    public abstract String produce();
}