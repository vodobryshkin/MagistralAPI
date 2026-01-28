package ru.rtkmagistral.magistralapi.validation.formats.inn;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять формату: "ИНН".
 */
@Constraint(validatedBy = INNValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface INN {
    String message() default "INN does not match the format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
