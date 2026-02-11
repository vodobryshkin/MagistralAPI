package ru.rtkmagistral.magistralapi.validation.formats.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять формату: "Пароль пользователя".
 */
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "MUST_CONTAIN_ONLY_ASCII_SYMBOLS";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}