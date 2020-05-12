package com.gardle.service.exception;

public class StripeServiceException extends PaymentProviderServiceException {

    public StripeServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
