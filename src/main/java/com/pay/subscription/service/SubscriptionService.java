package com.pay.subscription.service;

import com.pay.subscription.enums.SubscriptionType;
import com.pay.subscription.request.SubscriptionRequest;
import com.pay.subscription.response.SubscriptionResponse;
import com.pay.subscription.util.DateUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
public class SubscriptionService {

    private Long maxDuration = 3L;

    private Long weeklyMinDuration = 1L;

    private Long monthlyMinDuration = 1L;

    private void checkDates(String startDate, String endDate, SubscriptionType type) {
        if (Strings.isEmpty(startDate) || Strings.isEmpty(endDate)) {
            throw new IllegalArgumentException("Please fill in startDate and endDate fields.");
        }
        if (DateUtil.startDateIsAfterEndDate(DateUtil.stringToDate(startDate), DateUtil.stringToDate(endDate))) {
            throw new IllegalArgumentException("startDate cannot be after endDate.");
        }
        Long numberOfMonths = DateUtil.numberOfMonthsBetween(startDate, endDate);
        if (type == SubscriptionType.WEEKLY) {
            Long numberOfWeeks = DateUtil.numberOfWeeksBetween(startDate, endDate);
            if (numberOfWeeks < this.weeklyMinDuration) {
                throw new IllegalArgumentException(
                        "Weekly subscription period must be at least " + this.weeklyMinDuration + " week(s)."
                );
            }
        }
        if (type == SubscriptionType.MONTHLY) {
            if (numberOfMonths < this.monthlyMinDuration) {
                throw new IllegalArgumentException(
                        "Monthly subscription period must be at least " + this.monthlyMinDuration + " month(s)."
                );
            }
        }
        if (numberOfMonths > this.maxDuration) {
            throw new IllegalArgumentException(
                    "Subscription period cannot be more than " + this.maxDuration + " months."
            );
        }
    }

    public SubscriptionResponse subscribe(SubscriptionRequest request) {
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("Please input amount field.");
        }
        this.checkDates(request.getStartDate(), request.getEndDate(), request.getType());
        if (request.getType() == SubscriptionType.DAILY) {
            return this.handleDailySubscription(request);
        } else if (request.getType() == SubscriptionType.WEEKLY) {
            return this.handleWeeklySubscription(request);
        } else if (request.getType() == SubscriptionType.MONTHLY) {
            return this.handleMonthlySubscription(request);
        } else {
            throw new IllegalArgumentException("Subscription type is mandatory - DAILY, WEEKLY or MONTHLY.");
        }
    }

    private SubscriptionResponse handleDailySubscription(SubscriptionRequest request) {
        Set<String> invoiceDates = DateUtil.datesFromStartToEnd(
                DateUtil.stringToDate(request.getStartDate()),
                DateUtil.stringToDate(request.getEndDate()),
                1
        );
        SubscriptionResponse response = new SubscriptionResponse(request.getAmount(), request.getType(), invoiceDates);
        return response;
    }

    private SubscriptionResponse handleWeeklySubscription(SubscriptionRequest request) {
        if (request.getWeeklyInvoiceDay() == null) {
            throw new IllegalArgumentException("Please input a valid value for weeklyInvoiceDay, e.g. MONDAY or TUESDAY.");
        }
        LocalDate firstInvoiceDate = DateUtil.closestDateForDayOfWeek(
                DateUtil.stringToDate(request.getStartDate()),
                request.getWeeklyInvoiceDay()
        );
        Set<String> invoiceDates = DateUtil.datesFromStartToEnd(
                firstInvoiceDate,
                DateUtil.stringToDate(request.getEndDate()),
                7
        );
        SubscriptionResponse response = new SubscriptionResponse(request.getAmount(), request.getType(), invoiceDates);
        return response;
    }

    private SubscriptionResponse handleMonthlySubscription(SubscriptionRequest request) {
        if (request.getMonthlyInvoiceDay() == null ||
            request.getMonthlyInvoiceDay() > 31 ||
            request.getMonthlyInvoiceDay() < 1) {
            throw new IllegalArgumentException("Please input a proper value for the monthlyInvoiceDay field (1 to 31).");
        }
        Set<String> invoiceDates = DateUtil.datesWithSameDayFromStartToEnd(
                DateUtil.stringToDate(request.getStartDate()),
                DateUtil.stringToDate(request.getEndDate()),
                request.getMonthlyInvoiceDay()
        );
        SubscriptionResponse response = new SubscriptionResponse(request.getAmount(), request.getType(), invoiceDates);
        return response;
    }
}
