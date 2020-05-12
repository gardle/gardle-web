package com.gardle.service.exception;

public class NotAnImageServiceException extends RuntimeException {
    public NotAnImageServiceException(String message) {
        super(message);
    }

    public NotAnImageServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
