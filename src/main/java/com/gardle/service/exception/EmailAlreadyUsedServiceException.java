package com.gardle.service.exception;

public class EmailAlreadyUsedServiceException extends RuntimeException {

    public EmailAlreadyUsedServiceException() {
        super("Email is already in use!");
    }

}
