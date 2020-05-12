package com.gardle.web.rest;

import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.PaymentService;
import com.gardle.service.UserService;
import com.gardle.service.dto.AbstractCheckoutDTO;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for managing payments and related functionality.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    private final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/payments/checkoutSession")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<AbstractCheckoutDTO> createCheckoutSession(@RequestBody @Valid CreatorLeasingDTO creatorLeasingDTO) {
        return new ResponseEntity<>(this.paymentService.createCheckoutSession(creatorLeasingDTO), HttpStatus.CREATED);
    }

    @GetMapping("/payments/accountLinkUrl")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<String> getAccountLinkUrl() {
        return new ResponseEntity<>(this.userService.getAccountLinkUrl(), HttpStatus.OK);
    }
}
