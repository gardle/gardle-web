package com.gardle.service.exception;

public class LeasingCreateNotAllowedServiceException extends RuntimeException {
    public LeasingCreateNotAllowedServiceException() {
        super("Leasing create is not allowed in this period!");
    }

    public LeasingCreateNotAllowedServiceException(String message) {
        super(message);
    }

    public LeasingCreateNotAllowedServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
