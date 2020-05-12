package com.gardle.service.exception;

import lombok.Getter;

@Getter
public class MissingAuthorityForGardenFieldServiceException extends RuntimeException {
    public MissingAuthorityForGardenFieldServiceException() {

    }

    public MissingAuthorityForGardenFieldServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
