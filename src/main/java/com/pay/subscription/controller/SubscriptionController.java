package com.pay.subscription.controller;

import com.pay.subscription.request.SubscriptionRequest;
import com.pay.subscription.response.SubscriptionResponse;
import com.pay.subscription.service.SubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;

@RestController
@RequestMapping(path = "api/v1/subscription")
@AllArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity subscribe(@RequestBody SubscriptionRequest request) {
        try {
            SubscriptionResponse response = this.subscriptionService.subscribe(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException illegalArgumentException) {
            return new ResponseEntity<>(
                    illegalArgumentException.getMessage(),
                    HttpStatus.BAD_REQUEST
            );
        } catch (DateTimeParseException dateTimeParseException) {
            return new ResponseEntity<>(
                    dateTimeParseException.getParsedString() + " does not have dd/MM/yyyy format.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}
