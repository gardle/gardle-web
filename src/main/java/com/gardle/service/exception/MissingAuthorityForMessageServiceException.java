package com.gardle.service.exception;

public class MissingAuthorityForMessageServiceException extends RuntimeException {
    public MissingAuthorityForMessageServiceException() {

    }

    public MissingAuthorityForMessageServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
