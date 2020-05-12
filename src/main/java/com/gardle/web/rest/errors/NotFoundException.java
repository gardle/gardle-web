package com.gardle.web.rest.errors;

import org.zalando.problem.Status;

public class NotFoundException extends GardleRestControllerException {
    private static final long serialVersionUID = 1L;

    public NotFoundException(GardleErrorKey errorKey, String details) {
        super(errorKey, Status.NOT_FOUND, details);
    }

    public NotFoundException(GardleErrorKey errorKey) {
        this(errorKey, null);
    }
}
