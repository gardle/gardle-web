package com.gardle.service.exception;

public class LeasingTooShortServiceException extends RuntimeException {

    public LeasingTooShortServiceException() {
        super("Leasing is too short");
    }

    public LeasingTooShortServiceException(String message) {
        super(message);
    }

    public LeasingTooShortServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
