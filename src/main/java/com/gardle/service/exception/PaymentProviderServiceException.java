package com.gardle.service.exception;

public class PaymentProviderServiceException extends RuntimeException {
    public PaymentProviderServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
