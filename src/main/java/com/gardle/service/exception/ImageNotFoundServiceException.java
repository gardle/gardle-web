package com.gardle.service.exception;

public class ImageNotFoundServiceException extends RuntimeException {
    public ImageNotFoundServiceException() {
        super("Image not found!");
    }

    public ImageNotFoundServiceException(String message) {
        super(message);
    }

    public ImageNotFoundServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
