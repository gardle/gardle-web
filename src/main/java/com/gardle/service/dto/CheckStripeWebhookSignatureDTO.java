package com.gardle.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO used for checking requests from stirpe, received over our stripe webhooks
 */
@Data
@AllArgsConstructor
public class CheckStripeWebhookSignatureDTO {
    final String payload;
    final String stripeSignatureHeader;
    final String endpointSecret;
}
