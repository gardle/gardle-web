package com.gardle.service.exception;

public class GardenFieldNotFoundServiceException extends RuntimeException {
    public GardenFieldNotFoundServiceException() {
        super("Gardenfield not found!");
    }

    public GardenFieldNotFoundServiceException(String message) {
        super(message);
    }

    public GardenFieldNotFoundServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
