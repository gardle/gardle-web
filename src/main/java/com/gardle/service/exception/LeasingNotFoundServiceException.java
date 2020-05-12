package com.gardle.service.exception;

public class LeasingNotFoundServiceException extends RuntimeException {

    public LeasingNotFoundServiceException() {
        super("Gardenfield not found!");
    }

    public LeasingNotFoundServiceException(String message) {
        super(message);
    }

    public LeasingNotFoundServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
