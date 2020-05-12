package com.gardle.service.exception;

public class CoverNotFoundServiceException extends RuntimeException {
    public CoverNotFoundServiceException() {
        super("Cover not found!");
    }

    public CoverNotFoundServiceException(String message) {
        super(message);
    }

    public CoverNotFoundServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
