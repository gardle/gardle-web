package com.gardle.service.exception;

public class MissingStripeVerificationServiceException extends RuntimeException {
    public MissingStripeVerificationServiceException() {
    }

    public MissingStripeVerificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
