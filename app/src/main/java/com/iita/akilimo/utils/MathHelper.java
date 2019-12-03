package com.iita.akilimo.utils;

import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class MathHelper {
    private static String TAG = MathHelper.class.getSimpleName();
    private double ngnToUsdRate = 360;
    private double tzsToUsdRate = 2250;

    public MathHelper() {
        Log.i(TAG, "Started currency helper class");
    }

    public String convertCurrency(String stringToSplit, String toCurrency) {
        String joined;
        String splitRegex = "TO";
        String[] bands;

        if (stringToSplit.contains(toCurrency)) {
            return stringToSplit;
        }

        double rate1, rate2;
        double band1, band2 = 0;

        String str = Tools.replaceNonNumbers(stringToSplit, splitRegex);

        bands = str.contains("TO") ? str.split(splitRegex) : new String[]{str, "0"};

        band1 = Double.parseDouble(bands[0]);
        if (bands.length == 2) {
            band2 = Double.parseDouble(bands[1]);
        }
        if (band1 > 0 && band2 > 0) {
            rate1 = convertToLocalCurrency(band1, toCurrency);
            rate2 = convertToLocalCurrency(band2, toCurrency);
            joined = formatNumber(rate1, null) + " - " + formatNumber(rate2, toCurrency);
        } else {
            rate1 = convertToLocalCurrency(band1, toCurrency);
            joined = formatNumber(rate1, toCurrency);
        }

        return joined;
    }

    public double convertCurrency(double amount, String toCurrency) {
        return convertToLocalCurrency(amount, toCurrency);
    }

    public String convertCurrency(String stringToSplit, String toCurrency, String unitType) {
        String data = convertCurrency(stringToSplit, toCurrency);
        if (unitType != null) {
            if (data.contains(unitType)) {
                return data;
            } else if (data.contains("per")) {
                return data;
            }
            return data + " per " + unitType;
        }
        return data;
    }

    public String convertCurrency(String stringToSplit, String toCurrency, String unitType, String fieldSize) {
        String data = convertCurrency(stringToSplit, toCurrency);
        try {
            if (unitType != null) {
                if (data.contains(unitType) || data.contains("per")) {
                    return data;
                }

                String cleaned = Tools.replaceNonNumbers(data, "");
                double amount = Double.parseDouble(cleaned);
                double myFieldSize = Double.parseDouble(fieldSize);
                double computedAmount = amount * myFieldSize;

                data = String.format("%s %s per %s %s", roundToNearestSpecifiedValue(computedAmount, 1000), toCurrency, fieldSize, unitType);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return data;

    }

    public String convertCurrency(String stringToSplit, String toCurrency, String unitType, String fieldSize, String selectedField) {
        String data = convertCurrency(stringToSplit, toCurrency);
        try {
            if (unitType != null) {
                if (data.contains(unitType) || data.contains("per")) {
                    return data;
                }

                String cleaned = Tools.replaceNonNumbers(data, "");
                double amount = Double.parseDouble(cleaned);
                double myFieldSize = Double.parseDouble(fieldSize);
                double investmentAmount = amount * myFieldSize;
                String formattedNumber = formatNumber(roundToNearestSpecifiedValue(investmentAmount, 1000), null);
                data = String.format("%s %s per %s", formattedNumber, toCurrency, selectedField);
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return data;

    }

    public String formatNumber(double number, String toCurrency) {
        return toCurrency == null ? String.format(Locale.US, "%,.0f", number) : String.format("%,.0f " + toCurrency, number);
    }

    /**
     * @param amount          Amount to convert
     * @param toCurrency      From which currency we are converting
     * @param nearestRounding round to the nearest numbers
     * @return Double
     */
    public double convertToLocalCurrency(double amount, String toCurrency, int... nearestRounding) {
        double converted = amount;
        int nearestSpecifiedValue = 0;
        if (nearestRounding.length > 0) {
            nearestSpecifiedValue = nearestRounding[0];
        }

        try {
            switch (toCurrency) {
                case "NGN":
                    converted = amount * ngnToUsdRate;
                    break;
                case "TZS":
                    converted = amount * tzsToUsdRate;
                    break;
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }

        return nearestSpecifiedValue > 0 ? roundToNearestSpecifiedValue(converted, nearestSpecifiedValue) : converted;
    }

    public double convertToUSD(double currencyToConvert, String fromCurrency, int... nearestRounding) {
        int nearestSpecifiedValue = 100;
        if (nearestRounding.length > 0) {
            nearestSpecifiedValue = nearestRounding[0];
        }
        double converted = currencyToConvert;
        try {
            switch (fromCurrency) {
                case "NGN":
                    converted = currencyToConvert / ngnToUsdRate;
                    break;
                case "TZS":
                    converted = currencyToConvert / tzsToUsdRate;
                    break;
                case "USD":
                    return currencyToConvert;
            }
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }

        double convertedTemp = roundToNearestSpecifiedValue(converted, nearestSpecifiedValue);

        return convertedTemp < 1 ? convertedTemp : Math.round(convertedTemp);
    }

    public double roundToNearestSpecifiedValue(double numberToRound, double roundToNearest) {
        double rounded = Math.round(numberToRound / roundToNearest) * roundToNearest;
        if (rounded < 1 || roundToNearest < 1) {
            return roundToNDecimalPlaces(numberToRound, 10.0);
        } else {
            return rounded;
        }
    }

    public double roundToNDecimalPlaces(double numberToRound, double decimalPlaces) {
        double rounded = Math.round(numberToRound * decimalPlaces) / decimalPlaces;
        return rounded;
    }

    public double computeInvestmentAmount(double localCurrencyAmount, double fieldSize, String fromCurrency) {

        double amountToInvest = localCurrencyAmount * fieldSize;
        double convertedToUsd = convertToUSD(amountToInvest, fromCurrency);

        return roundToNearestSpecifiedValue(convertedToUsd, 1000);
    }

    /**
     * @param fieldYield The amount from the field without fertilizer
     * @param currency   The currency to display the amount in
     * @return fieldYieldAmount
     */
    public double computeFieldYield(double fieldYield, String currency) {
        double fieldYieldAmount = 0;
        try {
            fieldYieldAmount = convertToLocalCurrency(fieldYield, currency);
        } catch (Exception ex) {
            Crashlytics.log(Log.ERROR, TAG, ex.getMessage());
            Crashlytics.logException(ex);
        }
        return fieldYieldAmount;
    }
}