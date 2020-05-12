package com.gardle.service.exception;

public class InvalidLeasingStateServiceException extends RuntimeException {
    public InvalidLeasingStateServiceException() {
        super("Leasing state is not allowed!");
    }

    public InvalidLeasingStateServiceException(String message) {
        super(message);
    }

    public InvalidLeasingStateServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
