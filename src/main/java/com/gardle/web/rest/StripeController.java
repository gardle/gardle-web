package com.gardle.web.rest;

import com.gardle.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class StripeController {

    private final StripeWebhookService stripeWebhookService;

    private final Logger log = LoggerFactory.getLogger(StripeController.class);

    @PostMapping("/leasings")
    public ResponseEntity<Object> createLeasing(@RequestBody String eventJson,
                                                @RequestHeader(value = "Stripe-Signature") String stripeSignature) {
        log.debug("REST request from Stripe to create leasing : {}", eventJson);
        stripeWebhookService.handleLeasingWebhook(eventJson, stripeSignature);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/accounts")
    public ResponseEntity<Object> updateAccount(@RequestBody String eventJson,
                                                @RequestHeader(value = "Stripe-Signature") String stripeSignature) {
        log.debug("REST request from Stripe to notify about updated account : {}", eventJson);
        stripeWebhookService.handleAccountUpdateWebhook(eventJson, stripeSignature);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
