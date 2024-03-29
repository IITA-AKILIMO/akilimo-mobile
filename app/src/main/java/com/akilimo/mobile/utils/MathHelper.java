package com.akilimo.mobile.utils;

import android.app.Activity;
import android.util.Log;


import com.google.android.gms.common.util.Strings;

import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;

import io.sentry.Sentry;

public class MathHelper {
    private static String TAG = MathHelper.class.getSimpleName();
    private double ngnRate = 360;
    private double tzsRate = 2250;
    private double ghsRate = 6.11;

    private double baseAcre = 2.471;
    private double baseSqm = 4046.86;
    private double baseAre = 40.469;


    public MathHelper() {
    }

    /**
     * @param baseAcre
     * @param baseSqm
     * @deprecated
     */
    public MathHelper(double baseAcre, double baseSqm) {
        this.baseAcre = baseAcre;
        this.baseSqm = baseSqm;
    }

    /**
     * @param baseAcre
     * @param baseSqm
     * @param baseAre
     * @deprecated
     */
    public MathHelper(double baseAcre, double baseSqm, double baseAre) {
        this.baseAcre = baseAcre;
        this.baseSqm = baseSqm;
        this.baseAre = baseAre;
    }

    public MathHelper(Activity activity) {
        SessionManager sessionManager = new SessionManager(activity);
        this.ngnRate = sessionManager.getNgnRate();
        this.tzsRate = sessionManager.getTzsRate();
        this.ghsRate = sessionManager.getGhsRate();
    }

    private String convertCurrency(String stringToSplit, String toCurrency) {
        String joined = "";
        String splitRegex = "TO";
        String[] bands;

        stringToSplit = Tools.replaceCharacters(stringToSplit, "Dola", joined);
        try {
            if (stringToSplit.contains(toCurrency)) {
                return stringToSplit;
            }

            double rate1, rate2;
            double band1 = 0;
            double band2 = 0;

            String str = Tools.replaceNonNumbers(stringToSplit, splitRegex);

            bands = str.contains("TO") ? str.split(splitRegex) : new String[]{str, "0"};

            String textBand1 = bands[0];
            if (!Strings.isEmptyOrWhitespace(textBand1)) {
                band1 = Double.parseDouble(bands[0]);
            }
            if (bands.length == 2) {
                String textBand2 = bands[1];
                if (!Strings.isEmptyOrWhitespace(textBand2)) {
                    band2 = Double.parseDouble(bands[1]);
                }
            }
            if (band1 > 0 && band2 > 0) {
                rate1 = convertToLocalCurrency(band1, toCurrency);
                rate2 = convertToLocalCurrency(band2, toCurrency);
                joined = formatNumber(rate1, null) + " - " + formatNumber(rate2, toCurrency);
            } else {
                rate1 = convertToLocalCurrency(band1, toCurrency);
                joined = formatNumber(rate1, toCurrency);
            }
        } catch (Exception ex) {
            Sentry.captureException(ex);
        }
        return joined;
    }

    public String convertCurrency(String stringToSplit, String toCurrency, String currencySymbol, String unitType, String fieldSize, String selectedField, String separator) {
        String data = convertCurrency(stringToSplit, toCurrency);
        try {
            if (unitType != null) {
                if (data.contains(unitType) || data.contains(separator)) {
                    return data;
                }

                String cleaned = Tools.replaceNonNumbers(data, "");
                double amount = Double.parseDouble(cleaned);
                double myFieldSize = Double.parseDouble(fieldSize);
                double investmentAmount = amount * myFieldSize;
                String formattedNumber = formatNumber(roundToNearestSpecifiedValue(investmentAmount, 1000), null);
                data = String.format("%s %s %s %s", formattedNumber, currencySymbol, separator, selectedField);
            }
        } catch (Exception ex) {
            Sentry.captureException(ex);
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
        int nearestSpecifiedValue = 100;
        if (nearestRounding.length > 0) {
            nearestSpecifiedValue = nearestRounding[0];
        }

        try {
            switch (toCurrency) {
                case "NGN":
                    converted = amount * ngnRate;
                    break;
                case "TZS":
                    converted = amount * tzsRate;
                    break;
                case "GHS":
                    converted = amount * ghsRate;
                    break;
            }
        } catch (Exception ex) {
            Sentry.captureException(ex);
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
                    converted = currencyToConvert / ngnRate;
                    break;
                case "TZS":
                    converted = currencyToConvert / tzsRate;
                    break;
                case "GHS":
                    converted = currencyToConvert / ghsRate;
                    break;
                case "USD":
                    return currencyToConvert;
            }
        } catch (Exception ex) {
            Sentry.captureException(ex);
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
        return Math.round(numberToRound * decimalPlaces) / decimalPlaces;
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
            Sentry.captureException(ex);
        }
        return fieldYieldAmount;
    }

    public double convertToDouble(String numberText) {
        if (!Strings.isEmptyOrWhitespace(numberText)) {
            try {
                return Double.parseDouble(numberText.trim());
            } catch (Exception ex) {
                Sentry.captureException(ex);
            }
        }
        return 0.0;
    }

    public double convertFromAcreToSpecifiedArea(double areaSizeAcre, @NotNull String toAreaUnit) {
        double convertedAreaSize = 0.0;
        switch (toAreaUnit) {
            default:
            case "acre":
                return areaSizeAcre;
            case "ha":
                convertedAreaSize = areaSizeAcre / baseAcre;
                break;
            case "are":
                convertedAreaSize = areaSizeAcre * baseAre;
                break;
            case "sqm":
                convertedAreaSize = areaSizeAcre * baseSqm;
                break;
        }

        return roundToNDecimalPlaces(convertedAreaSize, 10);
    }

    public double convertToUnitWeightPrice(double selectedPrice, int unitWeight) {
        return (selectedPrice * unitWeight) / 1000;
    }

    /**
     * @param numberValue 0.0
     * @return String
     */
    public String removeLeadingZero(double numberValue) {
        DecimalFormat format = new DecimalFormat("0.#");
        return format.format(numberValue);
    }

    /**
     * @param numberValue 0.000
     * @param pattern     #.#####
     * @return String
     */
    public String removeLeadingZero(double numberValue, String pattern) {
        DecimalFormat format = new DecimalFormat(pattern);
        format.setRoundingMode(RoundingMode.CEILING);
        return format.format(numberValue);
    }

    public double computeInvestmentForSpecifiedAreaUnit(double acreInvestmentAmount, double areaSizeAcre, String areaUnit) {
        double areaSizeInvestment;
        switch (areaUnit) {
            default:
            case "acre":
                areaSizeInvestment = acreInvestmentAmount * areaSizeAcre;
                break;
            case "ha":
                areaSizeInvestment = (acreInvestmentAmount * baseAcre) * areaSizeAcre;
                break;
            case "sqm":
                areaSizeInvestment = (areaSizeAcre * baseSqm) * acreInvestmentAmount;
                break;
        }
        return areaSizeInvestment;
    }

}
