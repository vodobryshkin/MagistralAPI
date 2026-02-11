package ru.rtkmagistral.magistralapi.validation.formats.phone;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять формату: "Номер мобильного телефона".
 */
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    String message() default "MUST_MATCH_FORMAT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}