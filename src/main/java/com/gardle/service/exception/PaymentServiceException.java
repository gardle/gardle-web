package com.gardle.service.exception;

public class PaymentServiceException extends RuntimeException {

    public PaymentServiceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
