package com.gardle.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Internal DTO used for creation of an account with an external payment provider
 */
@Data
@AllArgsConstructor
public class CreatePaymentProviderAccountDTO {
    @NotNull
    private String email;
    @NotNull
    private String bankAccountIBAN;
}
