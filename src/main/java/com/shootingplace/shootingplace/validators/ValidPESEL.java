package com.shootingplace.shootingplace.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PeselValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPESEL {
    String message() default "Musisz podać prawidłowy numer PESEL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
