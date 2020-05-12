package com.gardle.web.rest.errors;

import org.zalando.problem.Status;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

public class InternalServerErrorException extends GardleRestControllerException {
    private static final long serialVersionUID = 1L;


    public InternalServerErrorException(@NotNull GardleErrorKey errorKey, @Nullable String details) {
        super(errorKey, Status.INTERNAL_SERVER_ERROR, details);
    }

    public InternalServerErrorException(GardleErrorKey errorKey) {
        this(errorKey, null);
    }
}
