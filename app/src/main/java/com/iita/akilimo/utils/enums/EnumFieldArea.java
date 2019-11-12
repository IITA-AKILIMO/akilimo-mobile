package com.iita.akilimo.utils.enums;

public enum EnumFieldArea {

    UNKNOWN {
        @Override
        public double areaValue() {
            return 0;
        }
    },
    QUARTER_ACRE {
        @Override
        public double areaValue() {
            return 0.25;
        }
    },
    HALF_ACRE {
        @Override
        public double areaValue() {
            return 0.5;
        }
    },
    ONE_ACRE {
        @Override
        public double areaValue() {
            return 1.0;
        }
    },
    ONE_HALF_ACRE {
        @Override
        public double areaValue() {
            return 1.5;
        }
    },
    TWO_HALF_ACRE {
        @Override
        public double areaValue() {
            return 2.5;
        }
    },
    FIVE_ACRE {
        @Override
        public double areaValue() {
            return 5;
        }
    }, EXACT_AREA {
        @Override
        public double areaValue() {
            return -1;
        }
    };

    public abstract double areaValue();
}
