package com.kotkina.bankrestapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NewUserValidValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NewUserValid {

    String message() default "Fields must be filled in.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
