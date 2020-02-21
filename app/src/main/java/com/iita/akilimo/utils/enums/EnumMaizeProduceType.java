package com.iita.akilimo.utils.enums;

public enum EnumMaizeProduceType {
    GRAIN {
        @Override
        public String produce() {
            return "grain";
        }
    },
    DRY_COB {
        @Override
        public String produce() {
            return "dry_cob";
        }
    },
    FRESH_COB {
        @Override
        public String produce() {
            return "fresh_cob";
        }
    };

    public abstract String produce();
}