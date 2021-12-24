package com.pay.subscription.request;

import com.pay.subscription.enums.Day;
import com.pay.subscription.enums.SubscriptionType;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class SubscriptionRequest {
    private BigDecimal amount;
    private SubscriptionType type;
    private String startDate;
    private String endDate;
    private Integer monthlyInvoiceDay;
    private Day weeklyInvoiceDay;
}
