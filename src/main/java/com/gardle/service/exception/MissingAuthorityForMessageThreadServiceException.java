package com.gardle.service.exception;

import lombok.Getter;

@Getter
public class MissingAuthorityForMessageThreadServiceException extends RuntimeException {
    public MissingAuthorityForMessageThreadServiceException() {

    }

    public MissingAuthorityForMessageThreadServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
