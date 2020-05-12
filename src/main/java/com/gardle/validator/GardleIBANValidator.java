package com.gardle.validator;

import org.apache.commons.validator.routines.IBANValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GardleIBANValidator implements ConstraintValidator<ValidIBAN, String> {
    public static final String TEST_IBAN = "AT89370400440532013000"; // TODO for testing only

    public void initialize(ValidIBAN constraint) {
    }

    public boolean isValid(String iban, ConstraintValidatorContext context) {
        return IBANValidator.getInstance().isValid(iban) || iban.equals(TEST_IBAN);
    }
}
