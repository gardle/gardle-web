package com.gardle.web.rest.errors;

import org.zalando.problem.Status;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class BadRequestException extends GardleRestControllerException {
    private static final long serialVersionUID = 1L;


    public BadRequestException(@NotNull GardleErrorKey errorKey, @Nullable String details) {
        super(errorKey, Status.BAD_REQUEST, details);
    }

    public BadRequestException(GardleErrorKey errorKey) {
        this(errorKey, null);
    }
}
