package com.gardle.service.exception;

import lombok.Getter;

@Getter
public class MissingAuthorityServiceException extends RuntimeException {
    public MissingAuthorityServiceException() {

    }

    public MissingAuthorityServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
