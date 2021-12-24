package com.pay.subscription.service;

import com.pay.subscription.enums.Day;
import com.pay.subscription.enums.SubscriptionType;
import com.pay.subscription.request.SubscriptionRequest;
import com.pay.subscription.response.SubscriptionResponse;
import com.pay.subscription.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class SubscriptionServiceTest {

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        this.subscriptionService = new SubscriptionService();
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenAmountIsNull() {
        SubscriptionRequest request = new SubscriptionRequest(
                null,
                SubscriptionType.DAILY,
                "23/12/2021",
                "24/12/2021",
                null,
                null
        );
        assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Please input amount field.");
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenDateIsNull() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.DAILY,
                null,
                "24/12/2021",
                null,
                null
        );
        assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Please fill in startDate and endDate fields.");
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenStartDateIsAfterEndDate() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.DAILY,
                "25/12/2021",
                "24/12/2021",
                null,
                null
        );
        assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startDate cannot be after endDate.");
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenWeeklySubscriptionIsLessThanOneWeek() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.WEEKLY,
                "25/12/2021",
                "26/12/2021",
                null,
                Day.TUESDAY
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            mock.when(() -> DateUtil.numberOfWeeksBetween("25/12/2021", "26/12/2021"))
                    .thenReturn(0L);
            mock.when(() -> DateUtil.numberOfMonthsBetween("25/12/2021", "26/12/2021"))
                    .thenReturn(0L);
            assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "Weekly subscription period must be at least 1 week(s)."
                    );
        }
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenMonthlySubscriptionIsLessThanOneMonth() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.MONTHLY,
                "25/12/2021",
                "26/12/2021",
                20,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            mock.when(() -> DateUtil.numberOfMonthsBetween("25/12/2021", "26/12/2021"))
                    .thenReturn(0L);
            assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "Monthly subscription period must be at least 1 month(s)."
                    );
        }
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenSubscriptionPeriodIsMoreThan3Months() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.DAILY,
                "25/01/2021",
                "25/12/2021",
                null,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            mock.when(() -> DateUtil.numberOfMonthsBetween("25/01/2021", "25/12/2021"))
                    .thenReturn(11L);
            assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "Subscription period cannot be more than 3 months."
                    );
        }
    }

    @Test
    void itShouldHandleDailySubscriptionCorrectly() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.DAILY,
                "25/01/2021",
                "27/01/2021",
                null,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            LocalDate start = LocalDate.of(2021, 1, 25);
            LocalDate end = LocalDate.of(2021, 1, 27);
            Set<String> dates = new LinkedHashSet<>();
            dates.add("25/01/2021");
            dates.add("26/01/2021");
            dates.add("27/01/2021");
            mock.when(() -> DateUtil.stringToDate(request.getStartDate()))
                    .thenReturn(start);
            mock.when(() -> DateUtil.stringToDate(request.getEndDate()))
                    .thenReturn(end);
            mock.when(() -> DateUtil.startDateIsAfterEndDate(start, end))
                    .thenReturn(false);
            mock.when(() -> DateUtil.numberOfMonthsBetween("25/01/2021", "27/12/2021"))
                    .thenReturn(0L);
            mock.when(() -> DateUtil.datesFromStartToEnd(start, end, 1))
                    .thenReturn(dates);
            SubscriptionResponse response = this.subscriptionService.subscribe(request);
            assertThat(response.getAmountPerInvoice()).isEqualTo(BigDecimal.TEN);
            assertThat(response.getType()).isEqualTo(SubscriptionType.DAILY);
            assertThat(response.getInvoiceDates()).contains("25/01/2021", "26/01/2021", "27/01/2021");
            assertThat(response.getInvoiceDates().size()).isEqualTo(3);
        }
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionIfWeeklyInvoiceDayIsMissingForWeeklySubscription() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.WEEKLY,
                "01/01/2021",
                "22/01/2021",
                null,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            LocalDate start = LocalDate.of(2021, 1, 1);
            LocalDate end = LocalDate.of(2021, 1, 22);
            mock.when(() -> DateUtil.stringToDate(request.getStartDate()))
                    .thenReturn(start);
            mock.when(() -> DateUtil.stringToDate(request.getEndDate()))
                    .thenReturn(end);
            mock.when(() -> DateUtil.startDateIsAfterEndDate(start, end))
                    .thenReturn(false);
            mock.when(() -> DateUtil.numberOfMonthsBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(0L);
            mock.when(() -> DateUtil.numberOfWeeksBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(3L);
            assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "Please input a valid value for weeklyInvoiceDay, e.g. MONDAY or TUESDAY."
                    );
        }
    }

    @Test
    void itShouldHandleWeeklySubscriptionCorrectly() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.WEEKLY,
                "01/01/2021",
                "22/01/2021",
                null,
                Day.MONDAY
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            LocalDate start = LocalDate.of(2021, 1, 1);
            LocalDate end = LocalDate.of(2021, 1, 22);
            LocalDate firstInvoiceDate = LocalDate.of(2021, 1, 5);
            Set<String> invoiceDates = new LinkedHashSet<>();
            invoiceDates.add("05/01/2021");
            invoiceDates.add("12/01/2021");
            invoiceDates.add("19/01/2021");
            mock.when(() -> DateUtil.stringToDate(request.getStartDate()))
                    .thenReturn(start);
            mock.when(() -> DateUtil.stringToDate(request.getEndDate()))
                    .thenReturn(end);
            mock.when(() -> DateUtil.startDateIsAfterEndDate(start, end))
                    .thenReturn(false);
            mock.when(() -> DateUtil.numberOfMonthsBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(0L);
            mock.when(() -> DateUtil.numberOfWeeksBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(3L);
            mock.when(() -> DateUtil.closestDateForDayOfWeek(start, request.getWeeklyInvoiceDay()))
                    .thenReturn(firstInvoiceDate);
            mock.when(() -> DateUtil.datesFromStartToEnd(firstInvoiceDate, end, 7))
                    .thenReturn(invoiceDates);
            SubscriptionResponse response = this.subscriptionService.subscribe(request);
            assertThat(response.getAmountPerInvoice()).isEqualTo(BigDecimal.TEN);
            assertThat(response.getType()).isEqualTo(SubscriptionType.WEEKLY);
            assertThat(response.getInvoiceDates()).contains("05/01/2021", "12/01/2021", "19/01/2021");
            assertThat(response.getInvoiceDates().size()).isEqualTo(3);
        }
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionIfMonthlyInvoiceDayIsNotValidForMonthlySubscription() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.MONTHLY,
                "01/01/2021",
                "01/03/2021",
                0,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            LocalDate start = LocalDate.of(2021, 1, 1);
            LocalDate end = LocalDate.of(2021, 3, 1);
            mock.when(() -> DateUtil.stringToDate(request.getStartDate()))
                    .thenReturn(start);
            mock.when(() -> DateUtil.stringToDate(request.getEndDate()))
                    .thenReturn(end);
            mock.when(() -> DateUtil.startDateIsAfterEndDate(start, end))
                    .thenReturn(false);
            mock.when(() -> DateUtil.numberOfMonthsBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(2L);
            assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "Please input a proper value for the monthlyInvoiceDay field (1 to 31)."
                    );
        }
    }

    @Test
    void itShouldHandleMonthlySubscriptionCorrectly() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.MONTHLY,
                "01/01/2021",
                "01/03/2021",
                1,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            LocalDate start = LocalDate.of(2021, 1, 1);
            LocalDate end = LocalDate.of(2021, 3, 1);
            Set<String> invoiceDates = new LinkedHashSet<>();
            invoiceDates.add("01/01/2021");
            invoiceDates.add("01/02/2021");
            invoiceDates.add("01/03/2021");
            mock.when(() -> DateUtil.stringToDate(request.getStartDate()))
                    .thenReturn(start);
            mock.when(() -> DateUtil.stringToDate(request.getEndDate()))
                    .thenReturn(end);
            mock.when(() -> DateUtil.startDateIsAfterEndDate(start, end))
                    .thenReturn(false);
            mock.when(() -> DateUtil.numberOfMonthsBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(2L);
            mock.when(() -> DateUtil.datesWithSameDayFromStartToEnd(start, end, request.getMonthlyInvoiceDay()))
                    .thenReturn(invoiceDates);
            SubscriptionResponse response = this.subscriptionService.subscribe(request);
            assertThat(response.getAmountPerInvoice()).isEqualTo(BigDecimal.TEN);
            assertThat(response.getType()).isEqualTo(SubscriptionType.MONTHLY);
            assertThat(response.getInvoiceDates()).contains("01/01/2021", "01/02/2021", "01/03/2021");
            assertThat(response.getInvoiceDates().size()).isEqualTo(3);
        }
    }

    @Test
    void itShouldThrowIllegalArgumentExceptionWhenSubscriptionTypeIsNotValid() {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                null,
                "01/01/2021",
                "02/01/2021",
                null,
                null
        );
        try (MockedStatic<DateUtil> mock = Mockito.mockStatic(DateUtil.class)) {
            LocalDate start = LocalDate.of(2021, 1, 1);
            LocalDate end = LocalDate.of(2021, 1, 1);
            mock.when(() -> DateUtil.stringToDate(request.getStartDate()))
                    .thenReturn(start);
            mock.when(() -> DateUtil.stringToDate(request.getEndDate()))
                    .thenReturn(end);
            mock.when(() -> DateUtil.startDateIsAfterEndDate(start, end))
                    .thenReturn(false);
            mock.when(() -> DateUtil.numberOfMonthsBetween(request.getStartDate(), request.getEndDate()))
                    .thenReturn(0L);
            assertThatThrownBy(() -> this.subscriptionService.subscribe(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(
                            "Subscription type is mandatory - DAILY, WEEKLY or MONTHLY."
                    );
        }
    }
}