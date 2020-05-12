package com.gardle.service.exception;

public class UsernameAlreadyUsedServiceException extends RuntimeException {

    public UsernameAlreadyUsedServiceException() {
        super("Login name already used!");
    }

}
