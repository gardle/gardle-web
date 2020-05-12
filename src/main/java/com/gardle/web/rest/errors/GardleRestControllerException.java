package com.gardle.web.rest.errors;

import lombok.Getter;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.StatusType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Base class for all exceptions for the controller layer. Wraps the error key that is sent to the client.
 */
@Getter
public abstract class GardleRestControllerException extends AbstractThrowableProblem {

    public GardleRestControllerException(@NotNull GardleErrorKey errorKey, @NotNull StatusType statusType, @Nullable String details) {
        super(null, errorKey.name(), statusType, details);
    }
}
