package com.iita.akilimo.utils.enums;


import com.iita.akilimo.utils.MathHelper;

public enum EnumUnitPrice {
    UNKNOWN {
        double unitPriceLower = -1;
        double unitPriceUpper = -1;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            return -1;
        }

        @Override
        public double unitPricePerTonneLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_RANGE_ONE {
        double unitPriceLower = 20;
        double unitPriceUpper = 30;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
        }

        @Override
        public double unitPricePerTonneLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_RANGE_TWO {
        double unitPriceLower = 31;
        double unitPriceUpper = 50;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
        }

        @Override
        public double unitPricePerTonneLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_RANGE_THREE {
        double unitPriceLower = 51;
        double unitPriceUpper = 100;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
        }

        @Override
        public double unitPricePerTonneLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_RANGE_FOUR {
        double unitPriceLower = 101;
        double unitPriceUpper = 150;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
        }

        @Override
        public double unitPricePerTonneLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_RANGE_FIVE {
        double unitPriceLower = 151;
        double unitPriceUpper = 200;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
        }

        @Override
        public double unitPricePerTonneLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_EXACT {
        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            return 0.0;
        }

        @Override
        public double unitPricePerTonneLower() {
            return 0;
        }

        @Override
        public double unitPricePerTonneUpper() {
            return 0;
        }
    };

    public abstract double convertToLocalCurrency(String toCurrency, MathHelper mathHelper);

    public abstract double unitPricePerTonneLower();

    public abstract double unitPricePerTonneUpper();

    private static double convertCurrency(double amount, String toCurrency, MathHelper mathHelper) {
        if (mathHelper == null) {
            mathHelper = new MathHelper();
        }
        return mathHelper.convertToLocalCurrency(amount, toCurrency);
    }
}
