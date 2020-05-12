package com.gardle.service.exception;

public class GardenFieldWithoutOwnerServiceException extends RuntimeException {

    public GardenFieldWithoutOwnerServiceException() {
        super("Garden field without owner!");
    }
}
