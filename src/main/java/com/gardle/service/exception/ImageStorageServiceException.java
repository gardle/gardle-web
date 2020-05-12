package com.gardle.service.exception;

public class ImageStorageServiceException extends RuntimeException {
    public ImageStorageServiceException(String message) {
        super(message);
    }

    public ImageStorageServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
