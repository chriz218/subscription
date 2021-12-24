package com.pay.subscription.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pay.subscription.enums.Day;
import com.pay.subscription.enums.SubscriptionType;
import com.pay.subscription.request.SubscriptionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SubscriptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Failed to convert object to json.");
            return null;
        }
    }

    @Test
    void itShouldSucceedForDaily() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.DAILY,
                "25/01/2021",
                "27/01/2021",
                null,
                null
        );
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.amountPerInvoice").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.type").value(SubscriptionType.DAILY.name()))
                .andExpect(jsonPath("$.invoiceDates", hasSize(3)))
                .andExpect(jsonPath("$.invoiceDates", hasItem("25/01/2021")))
                .andExpect(jsonPath("$.invoiceDates", hasItem("26/01/2021")))
                .andExpect(jsonPath("$.invoiceDates", hasItem("27/01/2021")));
    }

    @Test
    void itShouldSucceedForWeekly() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.WEEKLY,
                "01/01/2021",
                "15/01/2021",
                null,
                Day.TUESDAY
        );
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.amountPerInvoice").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.type").value(SubscriptionType.WEEKLY.name()))
                .andExpect(jsonPath("$.invoiceDates", hasSize(2)))
                .andExpect(jsonPath("$.invoiceDates", hasItem("05/01/2021")))
                .andExpect(jsonPath("$.invoiceDates", hasItem("12/01/2021")));
    }

    @Test
    void itShouldSucceedForMonthly() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.MONTHLY,
                "01/01/2021",
                "01/03/2021",
                14,
                null
        );
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.amountPerInvoice").value(BigDecimal.TEN))
                .andExpect(jsonPath("$.type").value(SubscriptionType.MONTHLY.name()))
                .andExpect(jsonPath("$.invoiceDates", hasSize(2)))
                .andExpect(jsonPath("$.invoiceDates", hasItem("14/01/2021")))
                .andExpect(jsonPath("$.invoiceDates", hasItem("14/02/2021")));
    }

    @Test
    void itShouldThrowIllegalArgumentException() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                null,
                "01/01/2021",
                "01/03/2021",
                null,
                null
        );
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String content = resultActions.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
        assertThat(content).isEqualTo("Subscription type is mandatory - DAILY, WEEKLY or MONTHLY.");
    }

    @Test // Date format must be dd/MM/2021
    void itShouldThrowDateTimeParseException() throws Exception {
        SubscriptionRequest request = new SubscriptionRequest(
                BigDecimal.TEN,
                SubscriptionType.MONTHLY,
                "1/01/2021",
                "01/03/2021",
                20,
                null
        );
        ResultActions resultActions = this.mockMvc.perform(post("/api/v1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectToJson(request)));
        String content = resultActions.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
        assertThat(content).isEqualTo(request.getStartDate() + " does not have dd/MM/yyyy format.");
    }
}