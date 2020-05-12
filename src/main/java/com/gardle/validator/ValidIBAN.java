package com.gardle.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GardleIBANValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIBAN {
    String message() default "Invalid IBAN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
