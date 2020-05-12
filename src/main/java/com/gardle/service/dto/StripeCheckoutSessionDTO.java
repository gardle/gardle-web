package com.gardle.service.dto;

import com.stripe.model.checkout.Session;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel()
@Data
public class StripeCheckoutSessionDTO extends AbstractCheckoutDTO {
    private final Session session;

    public StripeCheckoutSessionDTO(final Session session) {
        this.session = session;
    }
}
