package com.gardle.service.exception;

public class ImageStorageServiceDeletionException extends RuntimeException {
    public ImageStorageServiceDeletionException(String message) {
        super(message);
    }

    public ImageStorageServiceDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}
