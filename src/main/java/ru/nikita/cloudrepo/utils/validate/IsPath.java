package ru.nikita.cloudrepo.utils.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.PARAMETER, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = PathValidator.class)
@Documented
public @interface IsPath {
    String message() default "Invalid path";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
