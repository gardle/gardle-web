package com.gardle.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO used for checking requests from stirpe, received over our stripe webhooks
 */
@Data
@AllArgsConstructor
public class GetStripeAccountLinkUrlDTO {
    final String stripeAccountId;
    final String stripeVerificationKey;
}
