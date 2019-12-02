package com.iita.akilimo.utils.enums;


import com.iita.akilimo.utils.MathHelper;

public enum EnumUnitPrice {
    UNKNOWN {
        double unitPriceLower = -1;
        double unitPriceUpper = -1;

        @Override
        public double convertToLocal(String toCurrency) {
            return -1;
        }

        @Override
        public double unitPriceLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPriceUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_20TO30 {
        double unitPriceLower = 20;
        double unitPriceUpper = 30;

        @Override
        public double convertToLocal(String toCurrency) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency);
        }

        @Override
        public double unitPriceLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPriceUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_30TO50 {
        double unitPriceLower = 31;
        double unitPriceUpper = 50;

        @Override
        public double convertToLocal(String toCurrency) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency);
        }

        @Override
        public double unitPriceLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPriceUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_50TO100 {
        double unitPriceLower = 51;
        double unitPriceUpper = 100;

        @Override
        public double convertToLocal(String toCurrency) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency);
        }

        @Override
        public double unitPriceLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPriceUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_100TO150 {
        double unitPriceLower = 101;
        double unitPriceUpper = 150;

        @Override
        public double convertToLocal(String toCurrency) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency);
        }

        @Override
        public double unitPriceLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPriceUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_150TO200 {
        double unitPriceLower = 151;
        double unitPriceUpper = 200;

        @Override
        public double convertToLocal(String toCurrency) {
            double usd = (unitPriceLower + unitPriceUpper) / 2;
            return EnumUnitPrice.convertCurrency(usd, toCurrency);
        }

        @Override
        public double unitPriceLower() {
            return unitPriceLower;
        }

        @Override
        public double unitPriceUpper() {
            return unitPriceUpper;
        }
    },
    PRICE_EXACT {
        @Override
        public double convertToLocal(String toCurrency) {
            return 0.0;
        }

        @Override
        public double unitPriceLower() {
            return 0;
        }

        @Override
        public double unitPriceUpper() {
            return 0;
        }
    };

    public abstract double convertToLocal(String toCurrency);

    public abstract double unitPriceLower();

    public abstract double unitPriceUpper();

    private static double convertCurrency(double amount, String toCurrency) {
        MathHelper mathHelper = new MathHelper();
        return mathHelper.convertToLocalCurrency(amount, toCurrency);
    }
}
