package com.pay.subscription.util;

import com.pay.subscription.enums.Day;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {
    @Test
    void itShouldConvertStringToDate() {
        LocalDate date = LocalDate.of(2021, 12, 23);
        assertThat(DateUtil.stringToDate("23/12/2021")).isEqualTo(date);
    }

    @Test
    void itShouldConvertDateToString() {
        LocalDate date = LocalDate.of(2021, 12, 23);
        assertThat(DateUtil.dateToString(date)).isEqualTo("23/12/2021");
    }

    @Test
    void itShouldGiveCorrectNumberOfMonths() {
        assertThat(DateUtil.numberOfMonthsBetween("23/11/2021", "23/12/2021")).isEqualTo(1);
    }

    @Test
    void itShouldGiveCorrectNumberOfWeeks() {
        assertThat(DateUtil.numberOfWeeksBetween("07/11/2021","21/11/2021")).isEqualTo(2);
    }

    @Test
    void itShouldGiveSameDayOfWeekFromStartDateToEndDate() {
        LocalDate start = LocalDate.of(2021, 11, 7);
        LocalDate end = LocalDate.of(2021, 11, 21);
        Set<String> dates = DateUtil.datesFromStartToEnd(start, end, 7);
        assertThat(dates.size()).isEqualTo(3);
        assertThat(dates).contains("07/11/2021", "14/11/2021", "21/11/2021");
    }

    @Test
    void itShouldGiveAllDatesWithSameDayFromStartDateToEndDate() {
        LocalDate start = LocalDate.of(2021, 2, 20);
        LocalDate end = LocalDate.of(2021, 5, 20);
        Set<String> datesWithSameDay = DateUtil.datesWithSameDayFromStartToEnd(start, end, 21);
        assertThat(datesWithSameDay.size()).isEqualTo(3);
        assertThat(datesWithSameDay).contains("21/02/2021","21/03/2021","21/04/2021");
    }

    @Test
    void itShouldGiveLastDateOfMonthIfDayDoesNotExistInMonth() {
        LocalDate start = LocalDate.of(2021, 2, 20);
        LocalDate end = LocalDate.of(2021, 4, 30);
        Set<String> dates = DateUtil.datesWithSameDayFromStartToEnd(start, end, 31);
        assertThat(dates.size()).isEqualTo(3);
        assertThat(dates).contains("28/02/2021","31/03/2021","30/04/2021");
    }

    @Test
    void itShouldGiveTrueIfStartDateIsAfterEndDate() {
        LocalDate start = LocalDate.of(2021, 2, 20);
        LocalDate end = LocalDate.of(2021, 2, 10);
        assertTrue(DateUtil.startDateIsAfterEndDate(start, end));
    }

    @Test
    void itShouldGiveClosestDateForDay() {
        LocalDate date = LocalDate.of(2021, 12, 24); // 24 Dec 2021 (Friday)
        LocalDate closestSaturdayDate = LocalDate.of(2021, 12, 25); // Next day (Saturday)
        assertThat(DateUtil.closestDateForDayOfWeek(date, Day.SATURDAY)).isEqualTo(closestSaturdayDate);
    }
}