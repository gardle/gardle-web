package com.gardle.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStripeAccountVerifiedUserDTO {
    private Boolean verified;
    private String stripeVerificationKey;
}
