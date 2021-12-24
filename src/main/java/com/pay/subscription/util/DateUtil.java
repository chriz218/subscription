package com.pay.subscription.util;

import com.pay.subscription.enums.Day;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.Set;

public final class DateUtil {

    private DateUtil() {}

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static LocalDate stringToDate(String dateString) {
        return LocalDate.parse(dateString, formatter);
    }

    public static String dateToString(LocalDate date) {
        return formatter.format(date);
    }

    public static Long numberOfMonthsBetween(String startDate, String endDate) {
        return ChronoUnit.MONTHS.between(stringToDate(startDate), stringToDate(endDate));
    }

    public static Long numberOfWeeksBetween(String startDate, String endDate) {
        return ChronoUnit.WEEKS.between(stringToDate(startDate), stringToDate(endDate));
    }

    public static Set<String> datesFromStartToEnd(LocalDate startDate, LocalDate endDate, long differenceInDays) {
        Set<String> dates = new LinkedHashSet<>();
        while (!startDate.isAfter(endDate)) {
            String dateString = dateToString(startDate);
            dates.add(dateString);
            startDate = startDate.plusDays(differenceInDays);
        }
        return dates;
    }

    public static Set<String> datesWithSameDayFromStartToEnd(LocalDate startDate, LocalDate endDate, Integer day) {
        Set<String> dates = new LinkedHashSet<>();
        LocalDate newDate = getDateWithSameDayOrGetLastDateOfMonth(startDate, day);
        while (!newDate.isAfter(endDate)) {
            String dateString = dateToString(newDate);
            if (!newDate.isBefore(startDate)) {
                dates.add(dateString);
            }
            newDate = getDateWithSameDayOrGetLastDateOfMonth(newDate.plusMonths(1), day);
        }
        return dates;
    }

    public static boolean startDateIsAfterEndDate(LocalDate startDate, LocalDate endDate) {
        return startDate.isAfter(endDate);
    }

    public static LocalDate closestDateForDayOfWeek(LocalDate date, Day day) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        while (!dayOfWeek.name().equals(day.name())) {
            date = date.plusDays(1);
            dayOfWeek = date.getDayOfWeek();
        }
        return date;
    }

    public static LocalDate getDateWithSameDayOrGetLastDateOfMonth(LocalDate date, Integer day) {
        LocalDate newDate = null;
        try {
            newDate = LocalDate.of(date.getYear(), date.getMonthValue(), day);
        } catch (DateTimeException e) {
            newDate = LocalDate.of(date.getYear(), date.getMonthValue(), date.lengthOfMonth());
        } finally {
            return newDate;
        }
    }
}
