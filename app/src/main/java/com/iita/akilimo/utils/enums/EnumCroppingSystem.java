package com.iita.akilimo.utils.enums;

public enum EnumCroppingSystem {
    CASSAVA_ONLY {
        @Override
        public String croppingCrop() {
            return "NA";
        }
    },
    CASSAVA_AND_MAIZE {
        @Override
        public String croppingCrop() {
            return "maize";
        }
    },
    CASSAVA_AND_SWEET_POTATO {
        @Override
        public String croppingCrop() {
            return "sweetpotato";
        }
    };

    public abstract String croppingCrop();
}