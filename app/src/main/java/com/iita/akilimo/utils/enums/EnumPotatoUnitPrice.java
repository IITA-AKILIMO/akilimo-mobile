package com.iita.akilimo.utils.enums;


import com.iita.akilimo.utils.MathHelper;

public enum EnumPotatoUnitPrice {
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
        double unitPriceLower = 47.62;
        double unitPriceUpper = 49.79;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumPotatoUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
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
        double unitPriceLower = 49.79;
        double unitPriceUpper = 51.95;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumPotatoUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
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
        double unitPriceLower = 51.95;
        double unitPriceUpper = 56.28;

        @Override
        public double convertToLocalCurrency(String toCurrency, MathHelper mathHelper) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumPotatoUnitPrice.convertCurrency(usd, toCurrency, mathHelper);
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
        return mathHelper.convertToLocalCurrency(amount, toCurrency);
    }
}
