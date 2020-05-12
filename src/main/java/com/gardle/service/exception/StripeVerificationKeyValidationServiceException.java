package com.gardle.service.exception;

public class StripeVerificationKeyValidationServiceException extends RuntimeException {
    public StripeVerificationKeyValidationServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
