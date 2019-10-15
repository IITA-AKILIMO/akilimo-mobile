package com.iita.akilimo.interfaces;

public interface IDateSelection {

    void checkSavedDates();

    String selectedDateLabel(int dateWindow);

    void updateLabel(String selectedDate, int dateDayNumber, int plantingWindow);

    void fetchDateRangeData();

    long saveDateData();
}
