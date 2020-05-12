package com.gardle.service.exception;

public class BankAccountCreationIbanEmptyServiceException extends RuntimeException {
    public BankAccountCreationIbanEmptyServiceException() {
        super("Bank account IBAN emtpy");
    }

    public BankAccountCreationIbanEmptyServiceException(String message) {
        super(message);
    }

    public BankAccountCreationIbanEmptyServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
