package com.iita.akilimo.utils.enums;

public enum EnumOperationType {
    MANUAL {
        @Override
        public String operationName() {
            return "manual";
        }
    },
    MECHANICAL {
        @Override
        public String operationName() {
            return "tractor";
        }
    },
    NONE {
        @Override
        public String operationName() {
            return "NA";
        }
    };


    public abstract String operationName();
}
