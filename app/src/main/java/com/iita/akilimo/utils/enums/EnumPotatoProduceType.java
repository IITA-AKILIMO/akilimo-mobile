package com.iita.akilimo.utils.enums;

public enum EnumPotatoProduceType {
    TUBERS {
        @Override
        public String produce() {
            return "tubers";
        }
    },
    FLOUR {
        @Override
        public String produce() {
            return "flour";
        }
    };
    public abstract String produce();
}