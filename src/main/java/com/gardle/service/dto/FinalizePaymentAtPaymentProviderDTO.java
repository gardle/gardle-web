package com.gardle.service.dto;

import com.gardle.domain.Leasing;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Internal DTO used for finalization of a payment with an external payment provider.
 * Contains:
 * 1. the account to transfer the funds to of the payment (receiver)
 * 2. the leasing associated with the payment to finalize
 */
@Data
@AllArgsConstructor
public class FinalizePaymentAtPaymentProviderDTO {
    @NotNull
    final String accountId;
    @NotNull
    final Leasing leasing;
}
