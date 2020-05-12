package com.gardle.service.exception;

public class NotLoggedInServiceException extends RuntimeException {

    public NotLoggedInServiceException() {

    }


    public NotLoggedInServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
