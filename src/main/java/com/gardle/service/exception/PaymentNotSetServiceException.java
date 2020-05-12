package com.gardle.service.exception;

public class PaymentNotSetServiceException extends RuntimeException {
    public PaymentNotSetServiceException() {
        super("Payment of leasing not set");
    }

    public PaymentNotSetServiceException(String message) {
        super(message);
    }

    public PaymentNotSetServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
