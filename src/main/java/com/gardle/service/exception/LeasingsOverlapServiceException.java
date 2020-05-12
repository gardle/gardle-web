package com.gardle.service.exception;

public class LeasingsOverlapServiceException extends RuntimeException {

    public LeasingsOverlapServiceException() {
        super("Leasings cannot overlap");
    }
}
