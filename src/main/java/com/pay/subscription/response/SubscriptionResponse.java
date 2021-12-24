package com.pay.subscription.response;

import com.pay.subscription.enums.SubscriptionType;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Setter
@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class SubscriptionResponse {
    private BigDecimal amountPerInvoice;
    private SubscriptionType type;
    private Set<String> invoiceDates;
}
