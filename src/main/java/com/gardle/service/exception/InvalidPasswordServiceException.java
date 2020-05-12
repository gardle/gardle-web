package com.gardle.service.exception;

public class InvalidPasswordServiceException extends RuntimeException {

    public InvalidPasswordServiceException() {
        super("Incorrect password");
    }

}
