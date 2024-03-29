package com.akilimo.mobile.utils;

import android.util.Log;

import androidx.annotation.NonNull;



import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.sentry.Sentry;

public class DateHelper {

    private static String LOG_TAG = DateHelper.class.getSimpleName();
    public static String format = "dd/MM/yyyy";
    public static String dateTimeFormat = "yyyy-MM-dd";
    private static SimpleDateFormat simpleDateFormat;

    @NonNull
    public static SimpleDateFormat getSimpleDateFormatter() {
        return new SimpleDateFormat(format, Locale.UK);
    }

    public static DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormat.forPattern(format);
    }

    @Deprecated
    public static int getWeekNumber(String dateString) {
        int weekNumber = 0;
        try {
            Date date = getSimpleDateFormatter().parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            weekNumber = cal.get(Calendar.WEEK_OF_YEAR);
        } catch (ParseException ex) {
            Sentry.captureException(ex);
        }
        return weekNumber;
    }

    public static int getDayNumber(String dateString) {
        int weekNumber = 0;
        try {
            Date date = getSimpleDateFormatter().parse(dateString);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            weekNumber = cal.get(Calendar.DAY_OF_YEAR);
        } catch (ParseException ex) {
            Sentry.captureException(ex);
        }
        return weekNumber;
    }

    @Deprecated
    public static String getDateFromWeeks(int weekNumber, String fromDate) {
        Date refDate = null;
        try {
            refDate = getSimpleDateFormatter().parse(fromDate);
        } catch (ParseException ex) {
            Sentry.captureException(ex);
        }

        DateTime dt = new DateTime(refDate);
        DateTime computedDate = dt.plusWeeks(weekNumber); //dt.minusWeeks(weekNumber);

        return getDateTimeFormatter().print(computedDate);//.format(computedDate);
    }

    public static String getDateFromDays(int dayNumber, String fromDate) {
        Date refDate = null;
        try {
            refDate = getSimpleDateFormatter().parse(fromDate);
        } catch (ParseException ex) {
            Sentry.captureException(ex);
        }

        DateTime dt = new DateTime(refDate);
        DateTime computedDate = dt.plusDays(dayNumber);

        return getDateTimeFormatter().print(computedDate);
    }

    @Deprecated
    public static String getDateFromWeekOfYear(int weekNumber) {
        simpleDateFormat = getSimpleDateFormatter();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.WEEK_OF_YEAR, weekNumber);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        return simpleDateFormat.format(cal.getTime());
    }

    public static String getDateFromDayOfYear(int dayOfYear) {
        simpleDateFormat = getSimpleDateFormatter();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, dayOfYear);
        return simpleDateFormat.format(cal.getTime());
    }


    public static int currentDayNumber() {
        Date today = new Date();
        simpleDateFormat = getSimpleDateFormatter();
        String currDate = simpleDateFormat.format(today.getTime());

        return getDayNumber(currDate);
    }

    public static int getCurrentYear() {
        return getCalendarNumber(Calendar.YEAR);
    }

    public static int getCurrentMonth() {
        return getCalendarNumber(Calendar.MONTH);
    }

    public static int getCurrentDay() {
        return getCalendarNumber(Calendar.DAY_OF_MONTH);
    }

    private static int getCalendarNumber(int calendarValueType) {
        final Calendar c = Calendar.getInstance();
        return c.get(calendarValueType);
    }

    public static Calendar getMaxDate(int maxMonths) {
        return getFutureOrPastMonth(maxMonths);
    }

    public static Calendar getMinDate(int minMonths) {
        return getFutureOrPastMonth(minMonths);
    }

    private static Calendar getFutureOrPastMonth(int maxMonth) {
        simpleDateFormat = getSimpleDateFormatter();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, maxMonth);
        return cal;
    }

    public static Calendar getFutureOrPastMonth(String referenceDate, int maxMonth) {
        Calendar cal = Calendar.getInstance();
        try {
            if (referenceDate != null) {
                Date refDate = getSimpleDateFormatter().parse(referenceDate);
                if (refDate != null) {
                    cal.setTime(refDate);// all done
                }
                cal.add(Calendar.MONTH, maxMonth);
            }
        } catch (ParseException ex) {
            Sentry.captureException(ex);
        }
        return cal;
    }

    public static LocalDateTime formatToLocalDateTime(String dateString) {
        return LocalDateTime.parse(dateString, DateTimeFormat.forPattern(dateTimeFormat));
    }

    public static LocalDate formatToLocalDate(String dateString) {
        return LocalDate.parse(dateString, DateTimeFormat.forPattern(dateTimeFormat));
    }

    public static DateTime unixTimeStampToDate(long unixTimestamp, DateTimeZone timeZone) {
        DateTime parse = new DateTime();
        try {
            DateTime dateTime = new DateTime(unixTimestamp);
            DateTime zoneTime = dateTime.withZone(timeZone);
            String dateString = zoneTime.toString();
            parse = DateTime.parse(dateString);
        } catch (Exception ex) {
            Sentry.captureException(ex);
        }
        return parse;
    }

    public static boolean olderThanCurrent(String dateOne) {
        LocalDate d1 = formatToLocalDate(dateOne);
        LocalDate currentDate = LocalDate.now();

        boolean dateOlderThanCurrent = false;
        if (d1.compareTo(currentDate) > 0) {
            dateOlderThanCurrent = false;
        } else if (d1.compareTo(currentDate) < 0) {
            dateOlderThanCurrent = true;
        } else if (d1.compareTo(currentDate) == 0) {
            dateOlderThanCurrent = false;
        }
        return dateOlderThanCurrent;
    }
}
